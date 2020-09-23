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

import com.tle.hibernate.dialect.LowercasePhysicalNamingStrategy;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.spi.Mapping;

@SuppressWarnings("nls")
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
        config.setProperties(properties);
        // TODO [SpringHib5] opted for the default impl.  Might be some massaging with the props vs
        // map
        config.setProperty(
            Environment.CONNECTION_PROVIDER, DatasourceConnectionProviderImpl.class.getName());
        // TODO [SpringHib5] seems weird to set the properties here.
        properties.put(Environment.DATASOURCE, dataSourceHolder.getDataSource());
        config.setProperty(Environment.DIALECT, dialect.getClass().getName());
        config.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
        config.setProperty(Environment.JPA_VALIDATION_MODE, "DDL");
        config.setImplicitNamingStrategy(new ImplicitNamingStrategyJpaCompliantImpl());
        config.setPhysicalNamingStrategy(new LowercasePhysicalNamingStrategy());
        for (Class<?> class1 : clazzes) {
          LOGGER.trace("Adding annotated class: " + class1.getCanonicalName());
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

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }
}
