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

package org.pentaho.test.platform.web.http.security;

import com.hitachivantara.security.web.impl.service.csrf.servlet.CsrfGateFilter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter.DEFAULT_PASSWORD_PARAMETER;
import static org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter.DEFAULT_USER_NAME_PARAMETER;

public class RequestParameterAuthenticationFilterTest {

  private static final String USER_ID = "admin";
  private static final String PLAIN_PASSWORD = "password";
  private static final String ENCRYPTED_PASSWORD = "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde";

  private RequestParameterAuthenticationFilterForTesting filterSpy;

  private AuthenticationManager authManagerMock;

  private CsrfGateFilter csrfGateFilter;

  // region Helpers

  private static class RequestParameterAuthenticationFilterForTesting extends RequestParameterAuthenticationFilter {
    // Promote to public.
    public Authentication getCurrentAuthentication() {
      return super.getCurrentAuthentication();
    }

    // Promote to public.
    public void setCurrentAuthentication( Authentication authentication ) {
      super.setCurrentAuthentication( authentication );
    }
  }

  @NonNull
  private static MockHttpServletRequest createPlainPasswordRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest( "GET", "" );
    request.addParameter( DEFAULT_USER_NAME_PARAMETER, USER_ID );
    request.addParameter( DEFAULT_PASSWORD_PARAMETER, PLAIN_PASSWORD );

    return request;
  }

  @NonNull
  private static MockHttpServletRequest createEncryptedPasswordRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest( "GET", "" );
    request.addParameter( DEFAULT_USER_NAME_PARAMETER, USER_ID );
    request.addParameter( DEFAULT_PASSWORD_PARAMETER, ENCRYPTED_PASSWORD );

    return request;
  }

  @NonNull
  private static MockHttpServletRequest createUserMissingRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest( "GET", "" );
    request.addParameter( DEFAULT_PASSWORD_PARAMETER, PLAIN_PASSWORD );

    return request;
  }

  @NonNull
  private static MockHttpServletRequest createPasswordMissingRequest() {
    MockHttpServletRequest request = new MockHttpServletRequest( "GET", "" );
    request.addParameter( DEFAULT_USER_NAME_PARAMETER, USER_ID );
    return request;
  }

  @NonNull
  private ISystemConfig mockSystemConfig() throws IOException {
    Properties properties = new Properties();
    properties.setProperty( "requestParameterAuthenticationEnabled", "true" );

    IConfiguration config = mock( IConfiguration.class );
    doReturn( properties ).when( config ).getProperties();

    ISystemConfig systemConfigMock = mock( ISystemConfig.class );
    doReturn( config ).when( systemConfigMock ).getConfiguration( "security" );
    return systemConfigMock;
  }

  private void stubCsrfGateFilterDoFilterAnyWithOkCheck( @NonNull CsrfGateFilter csrfGateFilterMock )
    throws IOException, ServletException {

    doAnswer( invocation -> {
      ServletRequest request = invocation.getArgument( 0 );
      ServletResponse response = invocation.getArgument( 1 );
      FilterChain filterChain = invocation.getArgument( 2 );

      // Delegate to filter chain.
      filterChain.doFilter( request, response );
      return null;
    } ).when( csrfGateFilterMock ).doFilterAny( any(), any(), any() );
  }

  private void stubCsrfGateFilterDoFilterAnyWithKoCheck( @NonNull CsrfGateFilter csrfGateFilterMock )
    throws IOException, ServletException {
    doAnswer( invocation -> {
      HttpServletResponse response = invocation.getArgument( 1 );

      // Simulate Permission Denied
      response.setStatus( 403 );
      return null;
    } ).when( csrfGateFilterMock ).doFilterAny( any(), any(), any() );
  }
  // endregion

  @Before
  public void beforeTest() throws KettleException, IOException, ServletException {
    KettleClientEnvironment.init();

    authManagerMock = mock( AuthenticationManager.class );

    csrfGateFilter = mock( CsrfGateFilter.class );
    stubCsrfGateFilterDoFilterAnyWithOkCheck( csrfGateFilter );

    filterSpy = new RequestParameterAuthenticationFilterForTesting();
    filterSpy.setAuthenticationManager( authManagerMock );
    filterSpy.setCsrfGateFilter( csrfGateFilter );
    filterSpy.setSystemConfig( mockSystemConfig() );
    filterSpy = spy( filterSpy );
  }

  @Test
  public void testDoFilterWhenPasswordIsEncrypted() throws IOException, ServletException {
    MockHttpServletRequest request = createEncryptedPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    UsernamePasswordAuthenticationToken authRequest =
      new UsernamePasswordAuthenticationToken( USER_ID, PLAIN_PASSWORD );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );

    verify( authManagerMock ).authenticate( eq( authRequest ) );
  }

  @Test
  public void testDoFilterWhenPasswordIsUnencrypted() throws IOException, ServletException {
    HttpServletRequest request = createPlainPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    UsernamePasswordAuthenticationToken authRequest =
      new UsernamePasswordAuthenticationToken( USER_ID, PLAIN_PASSWORD );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );

    verify( authManagerMock ).authenticate( eq( authRequest ) );
  }

  @Test
  public void testDoFilterWhenCSRFGateNotConfigured() throws IOException, ServletException {
    MockHttpServletRequest request = createEncryptedPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    filterSpy.setCsrfGateFilter( null );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    UsernamePasswordAuthenticationToken authRequest =
      new UsernamePasswordAuthenticationToken( USER_ID, PLAIN_PASSWORD );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );

    verify( authManagerMock ).authenticate( eq( authRequest ) );
  }

  @Test
  public void testDoFilterWhenUserIsNotSpecifiedThenImmediatelyContinuesToFilterChain()
    throws IOException, ServletException {
    MockHttpServletRequest request = createUserMissingRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    verify( csrfGateFilter, never() ).doFilterAny( any(), any(), any() );
    verify( authManagerMock, never() ).authenticate( any() );
    verify( filterChainMock ).doFilter( request, responseMock );
  }

  @Test
  public void testDoFilterWhenPasswordIsNotSpecifiedThenImmediatelyContinuesToFilterChain()
    throws IOException, ServletException {
    MockHttpServletRequest request = createPasswordMissingRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    verify( csrfGateFilter, never() ).doFilterAny( any(), any(), any() );
    verify( authManagerMock, never() ).authenticate( any() );
    verify( filterChainMock ).doFilter( request, responseMock );
  }

  @Test
  public void testDoFilterWhenSameUserIsSpecifiedButIsNotAuthenticatedThenAuthenticates()
    throws IOException, ServletException {
    MockHttpServletRequest request = createPlainPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    Authentication authentication = mock( Authentication.class );
    when( authentication.getName() ).thenReturn( USER_ID );
    when( authentication.isAuthenticated() ).thenReturn( false );

    when( filterSpy.getCurrentAuthentication() ).thenReturn( authentication );

    // ---

    filterSpy.doFilter( request, responseMock, filterChainMock );

    // ---

    verify( csrfGateFilter ).doFilterAny( any(), any(), any() );
    verify( authManagerMock ).authenticate( any() );
    verify( filterChainMock ).doFilter( request, responseMock );
  }

  @Test
  public void testDoFilterWhenSameUserIsSpecifiedThenImmediatelyContinuesToFilterChain()
    throws IOException, ServletException {
    MockHttpServletRequest request = createPlainPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    Authentication authentication = mock( Authentication.class );
    when( authentication.getName() ).thenReturn( USER_ID );
    when( authentication.isAuthenticated() ).thenReturn( true );

    when( filterSpy.getCurrentAuthentication() ).thenReturn( authentication );

    // ---

    filterSpy.doFilter( request, responseMock, filterChainMock );

    // ---

    verify( csrfGateFilter, never() ).doFilterAny( any(), any(), any() );
    verify( authManagerMock, never() ).authenticate( any() );
    verify( filterChainMock ).doFilter( request, responseMock );
  }

  @Test
  public void testDoFilterWhenCsrfCheckFailsThenImmediatelyResponds() throws IOException, ServletException {
    MockHttpServletRequest request = createPlainPasswordRequest();
    HttpServletResponse responseMock = mock( HttpServletResponse.class );
    FilterChain filterChainMock = mock( FilterChain.class );

    stubCsrfGateFilterDoFilterAnyWithKoCheck( csrfGateFilter );

    filterSpy.doFilter( request, responseMock, filterChainMock );

    verify( csrfGateFilter ).doFilterAny( any(), any(), any() );
    verify( authManagerMock, never() ).authenticate( any() );
    verify( filterChainMock, never() ).doFilter( any(), any() );
    verify( responseMock ).setStatus( 403 );
  }
}
