/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.web.servlet;

import mondrian.spi.DataSourceResolver;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * This class provides SPI functionality to Mondrian. It resolves relational data sources by their name. It uses the
 * {@link PentahoSessionHolder}.
 *
 * @author Luc Boudreau
 */
public class PentahoDataSourceResolver implements DataSourceResolver {

  private static final String UTF_8 = "utf-8";

  private static final Log logger = LogFactory.getLog( PentahoDataSourceResolver.class );

  public DataSource lookup( String dataSourceName ) throws Exception {
    javax.sql.DataSource datasource = null;

    // Is it an HTTP(S) URL metadata data source?
    if ( dataSourceName.startsWith( "http" ) ) {
      datasource = getHttpDataSource( dataSourceName );
    } else {
      String unboundDsName = null;
      IDBDatasourceService datasourceSvc = null;
      try {
        datasourceSvc =
            PentahoSystem.getObjectFactory().get( IDBDatasourceService.class, PentahoSessionHolder.getSession() );
        unboundDsName = datasourceSvc.getDSUnboundName( dataSourceName );
        datasource = datasourceSvc.getDataSource( unboundDsName );
      } catch ( ObjectFactoryException e ) {
        logger.error( Messages.getInstance().getErrorString( "PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE" ), e ); //$NON-NLS-1$
        throw e;
      } catch ( DBDatasourceServiceException e ) {
        /* We tried to find the datasource using unbound name.
        ** Now as a fall back we will attempt to find this datasource as it is.
        ** For example jboss/datasource/Hibernate. The unbound name ends up to be Hibernate
        ** We will first look for Hibernate and if we fail then look for jboss/datasource/Hibernate */

        logger.warn( Messages.getInstance().getString(
            "PentahoXmlaServlet.WARN_0001_UNABLE_TO_FIND_UNBOUND_NAME", dataSourceName, unboundDsName ), e ); //$NON-NLS-1$
        try {
          datasource = datasourceSvc.getDataSource( dataSourceName );
        } catch ( DBDatasourceServiceException dbse ) {
          logger
              .error( Messages.getInstance().getErrorString( "PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE" ), e ); //$NON-NLS-1$
          throw dbse;
        }
      }
    }

    return datasource;
  }

  protected DataSource getHttpDataSource( final String dsName ) throws DBDatasourceServiceException {

    Domain domain = null;
    try {
      InputStream inStream;
      XmiParser parser = new XmiParser();
      File file = new File( dsName );
      if ( file.exists() ) {
        inStream = new FileInputStream( file );
      } else {
        // execute the HTTP call
        GetMethod getMethod = new GetMethod( dsName );
        getMethod.setRequestHeader( "Content-Encoding", UTF_8 );
        addHttpAuthentication( getMethod );

        HttpClient httpClient = new HttpClient();
        int status = httpClient.executeMethod( getMethod );
        if ( status != HttpStatus.SC_OK ) {
          throw new RuntimeException( "Get data source XMI HTTP call failed with code " + status  );
        }

        inStream = getMethod.getResponseBodyAsStream();
      }

      domain = parser.parseXmi(inStream);
    } catch (Exception e) {
      throw new DBDatasourceServiceException(e);
    }

    if (domain.getPhysicalModels().size() == 0 ||
        !(domain.getPhysicalModels().get(0) instanceof SqlPhysicalModel)) {
      throw new DBDatasourceServiceException("No SQL Physical Model Available");

    }

    SqlPhysicalModel model = (SqlPhysicalModel)domain.getPhysicalModels().get(0);

    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());
    return new DatabaseMetaDataSource(databaseMeta);
  }

  private void addHttpAuthentication( HttpMethodBase method ) throws RuntimeException {
    // Pass-through HTTP authentication
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    if ( pentahoSession != null ) {

      String sessionCookie = pentahoSession.getHttpId();
      if ( sessionCookie != null ) {
        method.setRequestHeader( "Cookie", sessionCookie );
      }
    }
  }

  class DatabaseMetaDataSource implements DataSource {

    DatabaseMeta databaseMeta;

    public DatabaseMetaDataSource(DatabaseMeta databaseMeta) {
      this.databaseMeta = databaseMeta;
    }

    public Connection getConnection() throws SQLException {
      Database database = new Database(databaseMeta);
      try {
        database.connect();
      } catch (KettleException e) {
        e.printStackTrace();
        throw new SQLException(e.getMessage());
      }
      return database.getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
      return null;
    }

    public PrintWriter getLogWriter() throws SQLException {
      return null;
    }

    public int getLoginTimeout() throws SQLException {
      return 0;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    public void setLoginTimeout(int seconds) throws SQLException {
    }

    public boolean  isWrapperFor(Class<?> iface) {
      return false;
    }

    public <T> T unwrap(Class<T> iface) {
      return null;
    }

    public Logger getParentLogger() {
      return null;
    }

  }
}
