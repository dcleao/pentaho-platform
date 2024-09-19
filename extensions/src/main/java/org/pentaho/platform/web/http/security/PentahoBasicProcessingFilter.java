/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.http.security;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * This class's sole purpose is to defeat the persistence of Basic-Auth credentials in the browser.
 * The mechanism used to accomplish this is to detect an expired (invalid) HttpSession from the client.
 * <p/>
 * If the first request after a session becomes invalid is a Basic-Auth request, we automatically deny, forcing
 * reauthentication.
 * <p/>
 * The second path is if the first request after session invalidation is not a basic-auth (user manually logged out and
 * was presented with the login page), we drop a cookie in the response noting the event. The next request with
 * Basic-Auth and a valid HttpSession checks for this cookie and if present, forces reauthentication.
 * <p/>
 * <p/>
 * User: nbaker Date: 8/15/13
 */
public class PentahoBasicProcessingFilter extends BasicAuthenticationFilter implements ApplicationEventPublisherAware {

  /**
   * The <i>maximum age</i> of a cookie that lasts as long as the client session, <code>-1</code>.
   * The cookie is cleared when the client session ends.
   *
   * @see Cookie#setMaxAge(int)
   */
  private static final int COOKIE_MAX_AGE_CLIENT = -1;

  /**
   * The <i>maximum age</i> of an expired cookie, <code>0</code>.
   * The cookie is cleared in the client when received in a response.
   *
   * @see Cookie#setMaxAge(int)
   */
  private static final int COOKIE_MAX_AGE_PAST = 0;

  /**
   * The name of the cookie that identifies clients which may have cached Basic Authentication credentials for the
   * <code>Pentaho Realm</code> realm.
   * <p>
   * This cookie is set when this filter authenticates a session using Basic Authentication. It is cleared upon a
   * subsequent basic authentication attempt by the same client.
   * <p>
   * It must be ensured that Basic Authentication credentials cached in a web client are not used beyond their original
   * server login session. Web browsers/clients make this hard, given they will provide these cached credentials
   * automatically for any requests for URLs which:
   * <ul>
   *   <li>
   *      have the same folder path of that for which credentials were initially given (preemptively), or
   *   </li>
   *   <li>
   *      the server responds with <code>401</code> and a <code>WWW-Authenticate</code> header for the
   *      <code>Pentaho Realm</code> realm.
   *   </li>
   * </ul>
   * <p>
   * Cached credentials may be automatically provided even beyond the logoff/timeout of the original session which was
   * authenticated with basic authentication. It may happen on the next login session in the same open browser/client,
   * or only on the login session after that one, and so on.
   * <p>
   * The only way to ensure that any cached credentials are not later misused is to reject the first basic
   * authentication attempt in client following a previous successful basic authentication. At this point, this cookie
   * is cleared as well.
   * <p>
   * If a web client wants to authenticate a second time using Basic Authentication, in the same client session, any
   * subsequent attempt will get an initial rejection. If this is not desirable, a new client session should be spawned
   * after logging off from the first server login, or, otherwise, not needing to use that server login anymore.
   * On a web browser, this typically means closing and reopening the browser. Using programmatic HTTP clients, it
   * depends on the API, but will typically require creating a new HTTP client instance.
   */
  @VisibleForTesting
  static final String BASIC_AUTH_SESSION_COOKIE_NAME = "session-basic-auth";

  @VisibleForTesting
  static final String SESSION_ID_COOKIE_NAME = "JSESSIONID";

  private ApplicationEventPublisher applicationEventPublisher;

  // "Clone" of the super class' private #authenticationConverter field, for local use.
  // These two instances are kept with synchronized configurable properties by overriding #setCredentialsCharset() and
  // #setAuthenticationDetailsSource().
  protected final BasicAuthenticationConverter authenticationConverterLocal = new BasicAuthenticationConverter();

  public PentahoBasicProcessingFilter( AuthenticationManager authenticationManager,
                                       AuthenticationEntryPoint authenticationEntryPoint ) {
    super( authenticationManager, authenticationEntryPoint );
  }

  public void setApplicationEventPublisher( ApplicationEventPublisher applicationEventPublisher ) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  // Overridden to keep local authenticationConverter clone in sync with that of the super class.
  public void setCredentialsCharset( String credentialsCharset ) {
    super.setCredentialsCharset( credentialsCharset );

    this.authenticationConverterLocal.setCredentialsCharset( Charset.forName( credentialsCharset ) );
  }

  // Overridden to keep local authenticationConverter clone in sync with that of the super class.
  public void setAuthenticationDetailsSource( AuthenticationDetailsSource<HttpServletRequest, ?> source ) {
    super.setAuthenticationDetailsSource( source );

    this.authenticationConverterLocal.setAuthenticationDetailsSource( source );
  }

  @Override
  public void doFilterInternal( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
    throws IOException, ServletException {

    if ( willAttemptBasicAuthentication( request ) ) {
      // Trying to authenticate using Basic Auth over a) no session, or b) a session from a different user.
      // If an authenticated session currently exists, it may have been authenticated with basic auth or not.
      //
      // But if a previous basic auth session existed in this client which was not cleared yet, must do so now.
      // Rejecting with 401 for Pentaho realm, clears cached credentials on the client.
      if ( clearBasicAuthSessionCookie( request, response ) ) {
        getAuthenticationEntryPoint()
          .commence( request, response, new BadCredentialsException( "Clearing Basic-Auth" ) );
        return;
      }

      // Let super class attempt basic auth with given credentials.
      // When successful, #onSuccessfulAuthentication will mark the client with the basic auth cookie.
    }

    doFilterInternalSuper( request, response, chain );
  }

  @VisibleForTesting
  void doFilterInternalSuper( HttpServletRequest request, HttpServletResponse response, FilterChain chain )
    throws IOException, ServletException {
    super.doFilterInternal( request, response, chain );
  }

  @Override
  protected void onSuccessfulAuthentication( HttpServletRequest request, HttpServletResponse response,
                                             Authentication authResult ) throws IOException {

    response.addCookie( createBasicAuthSessionCookie( request ) );

    if ( applicationEventPublisher != null ) {
      applicationEventPublisher.publishEvent( new AuthenticationSuccessEvent( authResult ) );
    }
  }

  // This duplicates the logic in the super class, in determining whether basic auth will be attempted.
  protected boolean willAttemptBasicAuthentication( HttpServletRequest request ) {
    UsernamePasswordAuthenticationToken authRequest = authenticationConverterLocal.convert( request );
    return authRequest != null && isAuthenticationRequiredFor( authRequest.getName() );
  }

  // Clone of the super class' #authenticationIsRequired( String ) private method.
  protected boolean isAuthenticationRequiredFor( String username ) {
    Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
    if ( existingAuth == null || !existingAuth.isAuthenticated() ) {
      return true;
    }

    if ( existingAuth instanceof UsernamePasswordAuthenticationToken && !existingAuth.getName().equals( username ) ) {
      return true;
    }

    return ( existingAuth instanceof AnonymousAuthenticationToken );
  }

  // region Cookie Helpers
  @NonNull
  protected Cookie createBasicAuthSessionCookie( @NonNull HttpServletRequest request ) {
    // The cookie value is actually ignored elsewhere. Simple presence of the cookie is considered a basic auth session.
    Cookie cookie = new Cookie( BASIC_AUTH_SESSION_COOKIE_NAME, "true" );

    configureBasicAuthSessionCookie( cookie, request, COOKIE_MAX_AGE_CLIENT );
    return cookie;
  }

  protected static void configureBasicAuthSessionCookie( @NonNull Cookie cookie,
                                                         @NonNull HttpServletRequest request,
                                                         int maxAge ) {
    SessionCookieConfig sessionCookieConfig = request.getServletContext().getSessionCookieConfig();
    cookie.setPath( request.getContextPath() != null ? request.getContextPath() : "/" );
    cookie.setHttpOnly( sessionCookieConfig.isHttpOnly() );
    cookie.setSecure( sessionCookieConfig.isSecure() );
    cookie.setMaxAge( maxAge );
  }

  // Used by PentahoBasicAuthenticationEntryPoint
  static boolean clearBasicAuthSessionCookie( @NonNull HttpServletRequest request,
                                              @NonNull HttpServletResponse response ) {
    Cookie[] cookies = request.getCookies();
    if ( cookies != null ) {
      for ( Cookie cookie : cookies ) {
        if ( BASIC_AUTH_SESSION_COOKIE_NAME.equals( cookie.getName() ) ) {
          configureBasicAuthSessionCookie( cookie, request, COOKIE_MAX_AGE_PAST );
          response.addCookie( cookie );
          return true;
        }
      }
    }

    return false;
  }
  // endregion
}
