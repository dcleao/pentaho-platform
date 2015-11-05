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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.filters;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.util.messages.LocaleHelper;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * If the request is searching for a appcontext.js, it writes out the content of the appcontext.js
 */
public class PentahoAppContextFilter implements Filter {

  public static final String APP_CONTEXT_JS = "appcontext.js"; //$NON-NLS-1$
  static final String FILTER_APPLIED = "__pentaho_app_context_filter_applied"; //$NON-NLS-1$

  static final String initialComment =
          "/** appcontext.js is created by a PentahoAppContextFilter. This filter searches for an " + //$NON-NLS-1$
          "incoming URI having \"appcontext.js\" in it. **/ \n\n\n"; //$NON-NLS-1$
  static final byte[] initialCommentBytes = initialComment.getBytes();

  static final String appCreateHeader = "require('pentaho/App')\n.create({";
  static final String appCreateFooter = "})\n.init();";
  static final byte[] appCreateHeaderBytes = appCreateHeader.getBytes();
  static final byte[] appCreateFooterBytes = appCreateFooter.getBytes();

  private String serverProtocolValue = null;
  private String fullyQualifiedUrlValue = null;

  private static final String JS = ".js"; //$NON-NLS-1$
  private static final String CSS = ".css"; //$NON-NLS-1$
  private static final String CONTEXT = "context"; //$NON-NLS-1$
  private static final String GLOBAL = "global"; //$NON-NLS-1$
  private static final String REQUIRE_JS = "requirejs"; //$NON-NLS-1$

  // Changed to not do so much work for every request
  private static final ThreadLocal<byte[]> THREAD_LOCAL_LOCATION = new ThreadLocal<byte[]>();
  private static final ThreadLocal<byte[]> THREAD_LOCAL_RESERVED_CHARS = new ThreadLocal<byte[]>();

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestStr = httpRequest.getRequestURI();

    if ( requestStr != null &&
         requestStr.endsWith( APP_CONTEXT_JS ) &&
         httpRequest.getAttribute( FILTER_APPLIED ) == null ) {
      httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );
      try {
        response.setContentType( "text/javascript" ); //$NON-NLS-1$
        OutputStream out = response.getOutputStream();

        writeAppContext(request, out, httpRequest);
      } finally {
        httpRequest.removeAttribute( FILTER_APPLIED );
      }
    } else {
      chain.doFilter( httpRequest, httpResponse );
    }
  }

  private void writeAppContext( ServletRequest request, OutputStream out, HttpServletRequest httpRequest )
          throws IOException {

    out.write( initialCommentBytes );
    out.write( appCreateHeaderBytes );

    // Split out a fully qualified url, guaranteed to have a trailing slash.
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    String contextPath = requestContext.getContextPath();
    String queryString = getQueryString( httpRequest );
    boolean requireJsOnly = "true".equals( request.getParameter( "requireJsOnly" ) );

    // Compute the effective locale and set it in the global scope.
    // TODO: Also provide it as a module if the RequireJs system is available.
    String localeParam = request.getParameter("locale");
    Locale effectiveLocale = StringUtils.isNotEmpty( localeParam )
            ? new Locale( localeParam )
            : LocaleHelper.getLocale();

    printLocation(out, contextPath );
    printSession( effectiveLocale, out );
    printReservedChars( out );

    printContextRequireJs( out, contextPath, queryString );
    printPlugins( out );

    if ( !requireJsOnly ) {
      String contextName = request.getParameter( CONTEXT );
      boolean contextCssOnly = "true".equals( request.getParameter( "cssOnly" ) );

      printContextScripts( out, contextName, contextPath, queryString, contextCssOnly );
      printContextStyles( out, contextName, contextPath, queryString );
    }

    out.write( appCreateFooterBytes );
    out.close();
  }

  private void printLocation( OutputStream out, String contextPath ) throws IOException {
    byte[] locationBytes = THREAD_LOCAL_LOCATION.get();
    if ( locationBytes == null ) {
      locationBytes = getLocationBytes( contextPath );
      THREAD_LOCAL_LOCATION.set( locationBytes );
    }

    out.write( locationBytes );
  }

  private byte[] getLocationBytes( String contextPath ) throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append( "  location: {\n" ); //$NON-NLS-1$
    sb.append( "    protocol: '" + serverProtocolValue + "',\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "    host: '" + "" + "',\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "    pathname: '" + contextPath + "',\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "    href: '" + fullyQualifiedUrlValue + "'\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "  },\n" ); //$NON-NLS-1$

    return sb.toString().getBytes();
  }

  private void printSession( Locale effectiveLocale, OutputStream out ) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append( "  session: {\n" ); //$NON-NLS-1$

    if ( PentahoSessionHolder.getSession() != null ) {
      String name = StringEscapeUtils.escapeJavaScript(PentahoSessionHolder.getSession().getName());
      String home = ClientRepositoryPaths.getUserHomeFolderPath( name );

      sb.append( "    name: '" + name + "',\n" ); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append( "    home: '" + home + "',\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      sb.append( "    name: null,\n" ); //$NON-NLS-1$
      sb.append( "    home: null,\n" ); //$NON-NLS-1$
    }

    sb.append( "    locale: '" + effectiveLocale.toString()  + "'\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    sb.append( "  },\n" ); //$NON-NLS-1$

    out.write( sb.toString().getBytes() );
  }

  private void printReservedChars( OutputStream out ) throws IOException {
    byte[] reservedCharsBytes = THREAD_LOCAL_RESERVED_CHARS.get();
    if ( reservedCharsBytes == null ) {
      reservedCharsBytes = getReservedCharsBytes();
      THREAD_LOCAL_RESERVED_CHARS.set( reservedCharsBytes );
    }

    out.write( reservedCharsBytes );
  }

  private byte[] getReservedCharsBytes( ) {
    StringBuilder sbChars = new StringBuilder();
    StringBuffer sbCharsDisplay = new StringBuffer();

    // escape all reserved characters as they may have special meaning to regex engine
    StringBuilder sbCharsPattern = new StringBuilder();
    sbCharsPattern.append("/.*["); //$NON-NLS-1$

    List<Character> reservedCharacters = JcrRepositoryFileUtils.getReservedChars();

    for ( int i = 0; i < reservedCharacters.size(); i++ ) {
      char c = reservedCharacters.get( i );
      String cs = "" + c;

      sbChars.append(c);

      if ( c >= 0x07 && c <= 0x0d ) {
        sbCharsDisplay.append( StringEscapeUtils.escapeJava( cs ) );
      } else {
        sbCharsDisplay.append( c );
      }

      if ( i + 1 < reservedCharacters.size() ) {
        sbCharsDisplay.append( ", " );
      }

      sbCharsPattern.append( StringEscapeUtils.escapeJavaScript( cs ) );
    }

    sbCharsPattern.append("]+.*/"); //$NON-NLS-1$

    StringBuilder sb = new StringBuilder();
    sb.append( "  reservedChars: {\n" ); //$NON-NLS-1$

    sb.append( "    chars: \"" ); //$NON-NLS-1$
    sb.append( StringEscapeUtils.escapeJavaScript( sbChars.toString() ) );
    sb.append( "\",\n" ); //$NON-NLS-1$

    sb.append( "    display: \"" ); //$NON-NLS-1$
    sb.append( StringEscapeUtils.escapeJavaScript( sbCharsDisplay.toString() ) );
    sb.append( "\",\n" ); //$NON-NLS-1$

    sb.append( "    regex: " ); //$NON-NLS-1$
    sb.append( sbCharsPattern.toString() );
    sb.append( "\n" ); //$NON-NLS-1$

    sb.append( "  },\n" ); //$NON-NLS-1$

    return sb.toString().getBytes();
  }

  private void printContextRequireJs( OutputStream out, String contextPath, String queryString ) throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append( "  requireCfgs: [\n" ); //$NON-NLS-1$

    printResourcesForContext( REQUIRE_JS, contextPath, out, queryString, /*printJS*/true, /*isLastInBlock*/true );

    sb.append("  ],\n");

    out.write( sb.toString().getBytes() );
  }

  private void printPlugins(OutputStream out) throws IOException {
    StringBuilder sb = new StringBuilder();

    sb.append( "  plugins: [\n" ); //$NON-NLS-1$

    // TODO: !

    sb.append("  ],\n");

    out.write( sb.toString().getBytes() );
  }

  private void printContextScripts( OutputStream out, String contextName, String contextPath, String queryString,
        boolean contextCssOnly ) throws IOException {

    StringBuilder sb = new StringBuilder();

    sb.append( "  scripts: [\n" ); //$NON-NLS-1$

    boolean writeContext = !contextCssOnly && StringUtils.isEmpty( contextName );

    printResourcesForContext( GLOBAL, contextPath, out, queryString, /*printJS*/true, /*isLastInBlock*/!writeContext );

    if ( writeContext ) {
      printResourcesForContext( contextName, contextPath, out, queryString, /*printJS*/true, /*isLastInBlock*/true );
    }

    sb.append("  ],\n");

    out.write( sb.toString().getBytes() );
  }

  private void printContextStyles( OutputStream out, String contextName, String contextPath, String queryString)
          throws IOException {

    StringBuilder sb = new StringBuilder();

    sb.append( "  styles: [\n" ); //$NON-NLS-1$

    boolean writeContext = StringUtils.isEmpty( contextName );

    printResourcesForContext( GLOBAL, contextPath, out, queryString, /*printJS*/false, /*isLastInBlock*/!writeContext );

    if ( writeContext ) {
      printResourcesForContext( contextName, contextPath, out, queryString, /*printJS*/false, /*isLastInBlock*/true );
    }

    sb.append("  ],\n");

    out.write( sb.toString().getBytes() );
  }

  private void printResourcesForContext( String contextName, String contextPath, OutputStream out,
    String queryString, boolean printJS, boolean isLastInBlock) throws IOException {

    StringBuilder sb = new StringBuilder();

    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

    Encoder encoder = ESAPI.encoder();
    sb.append( "// Web resources defined by plugins as external-resources for: "
            + encoder.encodeForHTML( contextName ) + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    List<String> externalResources = pluginManager.getExternalResourcesForContext( contextName );
    if ( externalResources != null ) {
      for ( int i = 0 ; i < externalResources.size() ; i++ ) {
        String res = externalResources.get( i );
        if ( res != null ) {
          if ( printJS ? res.endsWith(JS) : res.endsWith(CSS) ) {
            sb.append( "'" + contextPath + res.trim() + queryString + "'" ); //$NON-NLS-1$ //$NON-NLS-2$

            if( !isLastInBlock || (i + 1 < externalResources.size()) ) {
              sb.append( "," ); //$NON-NLS-1$
            }

            sb.append( "\n" ); //$NON-NLS-1$
          }
        }
      }
    }

    out.write( sb.toString().getBytes() );
  }

  private static String getQueryString( HttpServletRequest request ) {
    Encoder encoder = ESAPI.encoder();

    String reqStr = "";
    Map paramMap = request.getParameterMap();

    // Fix for BISERVER-7613, BISERVER-7614, BISERVER-7615
    // Make sure that parameters in the URL are encoded for Javascript safety since they'll be
    // added to Javascript fragments that get executed.
    if ( paramMap.size() > 0 ) {
      StringBuilder sb = new StringBuilder();
      Map.Entry<String, String[]> me;
      char sep = '?'; // first separator is '?'
      Iterator<Map.Entry<String, String[]>> it = paramMap.entrySet().iterator();
      int i;
      while ( it.hasNext() ) {
        me = it.next();
        for ( i = 0; i < me.getValue().length; i++ ) {
          sb.append( sep ).append( encoder.encodeForJavaScript( me.getKey().toString() ) ).append( "=" ).append(
                  encoder.encodeForJavaScript( me.getValue()[ i ] ) );
        }
        if ( sep == '?' ) {
          sep = '&'; // change the separator
        }
      }
      reqStr = sb.toString(); // get the request string.
    }

    return reqStr;
  }

  public void init( FilterConfig filterConfig ) throws ServletException {
    // split out a fully qualified url, guaranteed to have a trailing slash
    fullyQualifiedUrlValue = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    if ( !fullyQualifiedUrlValue.endsWith( "/" ) ) { //$NON-NLS-1$
      fullyQualifiedUrlValue += "/"; //$NON-NLS-1$
    }
    if ( fullyQualifiedUrlValue.startsWith( "http" ) ) {
      serverProtocolValue = fullyQualifiedUrlValue.substring( 0, fullyQualifiedUrlValue.indexOf( ":" ) );
    } else {
      serverProtocolValue = "http";
    }
  }

  public void destroy() {
    // TODO Auto-generated method stub
  }

  protected void close( OutputStream out ) {
    try {
      out.close();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
