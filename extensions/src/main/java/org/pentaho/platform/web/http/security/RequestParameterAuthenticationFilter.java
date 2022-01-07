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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.http.security;

import com.google.common.annotations.VisibleForTesting;
import com.hitachivantara.security.web.impl.service.csrf.servlet.CsrfGateFilter;
import com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.Assert;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper.unwrap;
import static com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper.wrap;

/**
 * Processes Request Parameter authorization, putting the result into the <code>SecurityContextHolder</code>.
 *
 * <p>
 * This filter looks for request parameters with the user and password for authentication.
 * By default, the request parameters are named {@code userid} and {@code password},
 * but can be configured via {@link #setUserNameParameter(String)} and {@link #setPasswordParameter(String)},
 * respectively.
 * <p>
 * The password may be provided in encrypted form,
 * and is decrypted by using {@link Encr#decryptPasswordOptionallyEncrypted(String)}.
 * <p>
 * The request parameters may be given as part of the URL, as query or body parameters
 * (both form or multipart are supported).
 * <p>
 * No authentication is performed, and request handling is immediately delegated to the given filter chain, if either
 * of the authentication parameters is missing or if the currently authenticated user is the same as that specified.
 * Otherwise, the authentication process proceeds.
 * <p>
 * First, a check for the presence of a CSRF token is performed, and, when unsuccessful, a failed response is
 * immediately returned. This process is handled by the configured {@link #setCsrfGateFilter(CsrfGateFilter)}.
 * Otherwise, the authentication process proceeds.
 * <p>
 * The authentication proper is attempted, with the given user and password. When successful,
 * the resulting {@link Authentication} object will be placed into the <code>SecurityContextHolder</code>.
 * <p>
 * If authentication fails and <code>ignoreFailure</code> is <code>false</code> (the default),
 * an {@link AuthenticationEntryPoint} implementation is called.
 * Usually, this is {@link RequestParameterFilterEntryPoint}.
 * <p>
 * <b>Do not use this class directly.</b> Instead configure <code>web.xml</code> to use the
 * {@code org.springframework.security.util.FilterToBeanProxy}.
 */
public class RequestParameterAuthenticationFilter implements Filter, InitializingBean {
  // ~ Static fields/initializers =============================================

  private static final Log logger = LogFactory.getLog( RequestParameterAuthenticationFilter.class );

  // ~ Instance fields ========================================================

  private AuthenticationEntryPoint authenticationEntryPoint;

  private AuthenticationManager authenticationManager;

  private CsrfGateFilter csrfGateFilter;

  private boolean ignoreFailure = false;

  public static final String DEFAULT_USER_NAME_PARAMETER = "userid";
  public static final String DEFAULT_PASSWORD_PARAMETER = "password";

  private String userNameParameter = DEFAULT_USER_NAME_PARAMETER;
  private String passwordParameter = DEFAULT_PASSWORD_PARAMETER;

  private ISystemConfig systemConfig = PentahoSystem.get( ISystemConfig.class );

  private boolean isRequestParameterAuthenticationEnabled;
  private boolean isRequestAuthenticationParameterLoaded = false;

  // ~ Methods ================================================================

  public void init( final FilterConfig arg0 ) throws ServletException {
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull( this.authenticationManager, Messages.getInstance().getErrorString(
      "RequestParameterAuthenticationFilter.ERROR_0001_AUTHMGR_REQUIRED" ) );
    Assert.notNull( this.authenticationEntryPoint, Messages.getInstance().getErrorString(
      "RequestParameterAuthenticationFilter.ERROR_0002_AUTHM_ENTRYPT_REQUIRED" ) );
    Assert.hasText( this.userNameParameter, Messages.getInstance().getString(
      "RequestParameterAuthenticationFilter.ERROR_0003_USER_NAME_PARAMETER_MISSING" ) );
    Assert.hasText( this.passwordParameter, Messages.getInstance().getString(
      "RequestParameterAuthenticationFilter.ERROR_0004_PASSWORD_PARAMETER_MISSING" ) );
  }

  public void destroy() {
  }

  public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
    throws IOException, ServletException {

    if ( !isRequestAuthenticationParameterLoaded ) {
      IConfiguration config = this.systemConfig.getConfiguration( "security" );
      String strParameter = config.getProperties().getProperty( "requestParameterAuthenticationEnabled" );
      isRequestParameterAuthenticationEnabled = Boolean.parseBoolean( strParameter );
      isRequestAuthenticationParameterLoaded = true;
    }

    if ( !isRequestParameterAuthenticationEnabled ) {
      chain.doFilter( request, response );
      return;
    }

    if ( !( request instanceof HttpServletRequest ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
        "RequestParameterAuthenticationFilter.ERROR_0005_HTTP_SERVLET_REQUEST_REQUIRED" ) );
    }

    if ( !( response instanceof HttpServletResponse ) ) {
      throw new ServletException( Messages.getInstance().getErrorString(
        "RequestParameterAuthenticationFilter.ERROR_0006_HTTP_SERVLET_RESPONSE_REQUIRED" ) );
    }

    doFilterCore( (HttpServletRequest) request, (HttpServletResponse) response, chain );
  }

  private void doFilterCore( @NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull FilterChain chain )
    throws ServletException, IOException {

    // Use a multi-read wrapper for reading parameters.
    HttpServletRequest requestWrapper = wrap( request );
    String userName = requestWrapper.getParameter( this.userNameParameter );
    String encryptedPassword = requestWrapper.getParameter( this.passwordParameter );

    if ( logger.isDebugEnabled() ) {
      logger.debug(
        Messages.getInstance().getString( "RequestParameterAuthenticationFilter.DEBUG_AUTH_USERID", userName ) );
    }

    if ( userName == null || encryptedPassword == null ) {
      chain.doFilter( unwrap( requestWrapper ), response );
      return;
    }

    if ( !isAuthenticationRequired( userName ) ) {
      chain.doFilter( unwrap( requestWrapper ), response );
      return;
    }

    if ( csrfGateFilter == null ) {
      doFilterAuthentication( requestWrapper, response, chain, userName, encryptedPassword );
      return;
    }

    // Performing authentication requires protection against CSRF attacks, for every request (URL, method).
    // Do it BEFORE actually performing authentication.
    csrfGateFilter.doFilterAny( requestWrapper, response, ( requestWrapperInner, responseInner ) ->
      doFilterAuthentication( requestWrapper, response, chain, userName, encryptedPassword ) );
  }

  @VisibleForTesting
  protected Authentication getCurrentAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  @VisibleForTesting
  protected void setCurrentAuthentication( Authentication authentication ) {
    SecurityContextHolder.getContext().setAuthentication( authentication );
  }

  private void doFilterAuthentication( @NonNull HttpServletRequest requestWrapper,
                                       @NonNull HttpServletResponse response,
                                       @NonNull FilterChain chain,
                                       @NonNull String username,
                                       @NonNull String encryptedPassword )
    throws IOException, ServletException {

    String password = Encr.decryptPasswordOptionallyEncrypted( encryptedPassword );
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( username, password );
    authRequest.setDetails( new WebAuthenticationDetails( requestWrapper ) );

    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate( authRequest );
    } catch ( AuthenticationException authException ) {
      // Authentication failed.
      doFilterAuthenticationFailed( requestWrapper, response, chain, username, authException );
      return;
    }

    // Authentication success.
    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString(
        "RequestParameterAuthenticationFilter.DEBUG_AUTH_SUCCESS",
        authentication.toString() ) );
    }

    setCurrentAuthentication( authentication );

    chain.doFilter( unwrap( requestWrapper ), response );
  }

  private void doFilterAuthenticationFailed( @NonNull HttpServletRequest requestWrapper,
                                             @NonNull HttpServletResponse response,
                                             @NonNull FilterChain chain,
                                             @NonNull String userName,
                                             @NonNull AuthenticationException authException )
    throws IOException, ServletException {

    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getString(
        "RequestParameterAuthenticationFilter.DEBUG_AUTHENTICATION_REQUEST",
        userName,
        authException.toString() ) );
    }

    setCurrentAuthentication( null );

    if ( ignoreFailure ) {
      chain.doFilter( unwrap( requestWrapper ), response );
    } else {
      authenticationEntryPoint.commence( requestWrapper, response, authException );
    }
  }

  // Taken from org.springframework.security.web.authentication.www.BasicAuthenticationFilter#authenticationIsRequired
  private boolean isAuthenticationRequired( String userName ) {
    // Only reauthenticate if userName doesn't match SecurityContextHolder and user
    // isn't authenticated (see SEC-53)
    Authentication existingAuth = getCurrentAuthentication();
    if ( existingAuth == null || !existingAuth.isAuthenticated() ) {
      return true;
    }

    // Limit userName comparison to providers which use usernames (ie
    // UsernamePasswordAuthenticationToken) (see SEC-348)
    if ( existingAuth instanceof UsernamePasswordAuthenticationToken && !existingAuth.getName().equals( userName ) ) {
      return true;
    }

    // Handle unusual condition where an AnonymousAuthenticationToken is already
    // present. This shouldn't happen very often, as BasicProcessingFitler is meant to
    // be earlier in the filter chain than AnonymousAuthenticationFilter.
    // Nevertheless, presence of both an AnonymousAuthenticationToken together with a
    // BASIC authentication request header should indicate reauthentication using the
    // BASIC protocol is desirable. This behaviour is also consistent with that
    // provided by form and digest, both of which force re-authentication if the
    // respective header is detected (and in doing so replace/ any existing
    // AnonymousAuthenticationToken). See SEC-610.
    return ( existingAuth instanceof AnonymousAuthenticationToken );
  }

  public AuthenticationEntryPoint getAuthenticationEntryPoint() {
    return authenticationEntryPoint;
  }

  public AuthenticationManager getAuthenticationManager() {
    return authenticationManager;
  }

  public CsrfGateFilter getCsrfGateFilter() {
    return csrfGateFilter;
  }

  public boolean isIgnoreFailure() {
    return ignoreFailure;
  }

  public void setAuthenticationEntryPoint( final AuthenticationEntryPoint authenticationEntryPoint ) {
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  public void setAuthenticationManager( final AuthenticationManager authenticationManager ) {
    this.authenticationManager = authenticationManager;
  }

  public void setCsrfGateFilter( CsrfGateFilter csrfGateFilter ) {
    this.csrfGateFilter = csrfGateFilter;
  }

  public void setIgnoreFailure( final boolean ignoreFailure ) {
    this.ignoreFailure = ignoreFailure;
  }

  public String getUserNameParameter() {
    return userNameParameter;
  }

  public String getPasswordParameter() {
    return passwordParameter;
  }

  public void setUserNameParameter( final String value ) {
    userNameParameter = value;
  }

  public void setPasswordParameter( final String value ) {
    passwordParameter = value;
  }

  public ISystemConfig getSystemConfig() {
    return systemConfig;
  }

  public void setSystemConfig( ISystemConfig systemConfig ) {
    this.systemConfig = systemConfig;
  }
}
