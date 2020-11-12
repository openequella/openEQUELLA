/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.hibernate;

import com.tle.hibernate.dialect.OeqImplicitNamingStrategy;
import com.tle.hibernate.dialect.OeqPhysicalNamingStrategy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.Stoppable;

public class HibernateFactory {
  private static final Logger LOGGER = Logger.getLogger(HibernateFactory.class);

  private Mapping mapping;
  private ExtendedAnnotationConfiguration config;
  private SessionFactory sessionFactory;
  private Class<?>[] clazzes;
  private DataSourceHolder dataSourceHolder;
  private Properties properties = new Properties();
  private ClassLoader classLoader;

  public HibernateFactory(DataSourceHolder dataSourceHolder, Class<?>... clazzes) {
    this.clazzes = clazzes;
    this.dataSourceHolder = dataSourceHolder;
    this.classLoader = getClass().getClassLoader();
  }

  public void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }

  public synchronized ExtendedAnnotationConfiguration getConfiguration() {
    if (config == null) {
      ClassLoader oldLoader = oldLoader();
      try {
        setContextLoader(classLoader);
        ExtendedDialect dialect = dataSourceHolder.getDialect();
        this.config = new ExtendedAnnotationConfiguration(dialect);
        config.setProperty(Environment.CONNECTION_PROVIDER, DataSourceProvider.class.getName());
        properties.put(Environment.DATASOURCE, dataSourceHolder.getDataSource());
        config.addProperties(properties);
        config.setProperty(Environment.DIALECT, dialect.getClass().getName());
        config.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
        config.setProperty(Environment.JPA_VALIDATION_MODE, "DDL");
        // Due to https://hibernate.atlassian.net/browse/HHH-12665 with SpringHib5,
        // certain operations, like importing a institution would fail with a
        // `javax.persistence.TransactionRequiredException: no transaction is in progress`
        config.setProperty(Environment.ALLOW_UPDATE_OUTSIDE_TRANSACTION, "true");
        config.setImplicitNamingStrategy(new OeqImplicitNamingStrategy());
        config.setPhysicalNamingStrategy(new OeqPhysicalNamingStrategy());
        for (Class<?> class1 : clazzes) {
          if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Adding annotated class: " + class1.getCanonicalName());
          }
          config.addAnnotatedClass(class1);
        }
      } finally {
        setContextLoader(oldLoader);
      }
    }
    return config;
  }

  public synchronized Mapping getMapping() {
    if (mapping == null) {
      ClassLoader oldLoader = oldLoader();
      try {
        setContextLoader(classLoader);
        mapping = getConfiguration().buildMapping();
      } finally {
        setContextLoader(oldLoader);
      }
    }
    return mapping;
  }

  public synchronized SessionFactory getSessionFactory() {
    if (sessionFactory == null) {
      ClassLoader oldLoader = oldLoader();
      try {
        setContextLoader(classLoader);
        sessionFactory = getConfiguration().buildSessionFactory();
      } finally {
        setContextLoader(oldLoader);
      }
      getMapping();
    }
    return sessionFactory;
  }

  private void setContextLoader(ClassLoader loader) {
    Thread.currentThread().setContextClassLoader(loader);
  }

  private ClassLoader oldLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public String getDefaultSchema() {
    return dataSourceHolder.getDefaultSchema();
  }

  public static class DataSourceProvider implements ConnectionProvider, Configurable, Stoppable {
    private DataSource dataSource;

    @Override
    public void configure(Map configValues) {
      if (this.dataSource == null) {
        final Object dataSource = configValues.get(Environment.DATASOURCE);
        if (DataSource.class.isInstance(dataSource)) {
          this.dataSource = (DataSource) dataSource;
        } else {
          throw new HibernateException(
              "DataSource to use was not specified by ["
                  + Environment.DATASOURCE
                  + "] configuration property");
        }
      }
    }

    @Override
    public Connection getConnection() throws SQLException {
      if (dataSource == null) {
        throw new HibernateException("DataSource is null.  Unable to retrieve a connection");
      }
      return dataSource.getConnection();
    }

    @Override
    public void closeConnection(Connection conn) throws SQLException {
      conn.close();
    }

    @Override
    public boolean supportsAggressiveRelease() {
      return true;
    }

    @Override
    public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType)
          || DataSourceProvider.class.isAssignableFrom(unwrapType)
          || DataSource.class.isAssignableFrom(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
      if (ConnectionProvider.class.equals(unwrapType)
          || DatasourceConnectionProviderImpl.class.isAssignableFrom(unwrapType)) {
        return (T) this;
      } else if (DataSource.class.isAssignableFrom(unwrapType)) {
        return (T) dataSource;
      } else {
        throw new UnknownUnwrapTypeException(unwrapType);
      }
    }

    @Override
    public void stop() {
      this.dataSource = null;
    }
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
}
