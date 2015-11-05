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
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * Pentaho Web Context Filter checks to see if webcontext.js is being requested.
 * It writes JS code that defines the "pentaho/app" module, sets up AMD and loads external resources.
 * 
 * @author Ramaiz Mansoor
 */
public class PentahoWebContextFilter implements Filter {

  public static final String WEB_CONTEXT_JS = "webcontext.js"; //$NON-NLS-1$
  static final String FILTER_APPLIED = "__pentaho_web_context_filter_applied"; //$NON-NLS-1$

  static final String header =
      "/** webcontext.js is created by a PentahoWebContextFilter. This filter searches for an " + //$NON-NLS-1$
      "incoming URI having \"webcontext.js\" in it.**/ \n\n\n(function() {"; //$NON-NLS-1$
  static final byte[] headerBytes = header.getBytes();

  static final String footer =
      "  // Include RequireJS if it ain't loaded yet.\n" +
      "  if(typeof define === \"undefined\" || !define.amd)\n" +
      "    writeScript(\"js/require.js\");\n" +
      "\n" +
      "  // Define the \"pentaho/App\" class\n" +
      "  writeScript(\"js/browser_sync_app.js\");\n" +
      "\n" +
      "  // Create and initialize the app singleton\n" +
      "  writeScript(\"js/appcontext.js\" + queryString);\n" +
      "\n" +
      "  function writeScript(url) {\n" +
      "    document.write('<script type=\"text/javascript\" src=\"' + webAppUrl + url + '\"></scr' + 'ipt>');\n" +
      "  }\n" +
      "} ());"; //$NON-NLS-1$
  static final byte[] footerBytes = footer.getBytes();

  byte[] webAppUrlVarBytes = null;

  public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain ) throws IOException,
    ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String requestStr = httpRequest.getRequestURI();

    if ( requestStr != null && requestStr.endsWith( WEB_CONTEXT_JS )
        && httpRequest.getAttribute( FILTER_APPLIED ) == null ) {
      httpRequest.setAttribute( FILTER_APPLIED, Boolean.TRUE );
      try {
        response.setContentType( "text/javascript" ); //$NON-NLS-1$

        writeWebContext( response.getOutputStream(), httpRequest );
      } finally {
        httpRequest.removeAttribute( FILTER_APPLIED );
      }
    } else {
      chain.doFilter( httpRequest, httpResponse );
    }
  }

  private void writeWebContext( OutputStream out, HttpServletRequest httpRequest ) throws IOException {

    out.write( headerBytes );
    out.write( webAppUrlVarBytes );

    // queryString variable
    String queryString = getQueryString( httpRequest );
    out.write( ("  var queryString = \"" + StringEscapeUtils.escapeJavaScript(queryString) + "\";\n\n").getBytes() );

    String basicAuthFlag = (String) httpRequest.getSession().getAttribute( "BasicAuth" );
    if ( basicAuthFlag != null && basicAuthFlag.equals( "true" ) ) {
      out.write( ( "writeScript(\"js/postAuth.js\");\n\n" ).getBytes() );
    }

    out.write( footerBytes );

    // Any subclass can add more information to webcontext.js
    addCustomInfo( out );

    out.close();
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

  // TODO: compatibility breaking?
  protected void addCustomInfo( OutputStream out ) throws IOException {

  }

  public void init( FilterConfig filterConfig ) throws ServletException {

    String fullyQualifiedUrlValue = PentahoSystem.getApplicationContext().getFullyQualifiedServerURL();
    if ( !fullyQualifiedUrlValue.endsWith( "/" ) ) { //$NON-NLS-1$
      fullyQualifiedUrlValue += "/"; //$NON-NLS-1$
    }

    webAppUrlVarBytes = ("  var webAppUrl = \"" + fullyQualifiedUrlValue + "\";\n\n").getBytes(); //$NON-NLS-1$ //$NON-NLS-2$
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
