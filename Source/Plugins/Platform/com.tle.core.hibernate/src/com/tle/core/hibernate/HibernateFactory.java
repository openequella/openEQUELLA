/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.Mapping;

import com.tle.hibernate.dialect.LowercaseImprovedNamingScheme;

@SuppressWarnings("nls")
public class HibernateFactory
{
	private static final String KEY_DATASOURCE = "datasource";
	private Mapping mapping;
	private ExtendedAnnotationConfiguration config;
	private SessionFactory sessionFactory;
	private Class<?>[] clazzes;
	private DataSourceHolder dataSourceHolder;
	private Properties properties = new Properties();
	private ClassLoader classLoader;

	public HibernateFactory(DataSourceHolder dataSourceHolder, Class<?>... clazzes)
	{
		this.clazzes = clazzes;
		this.dataSourceHolder = dataSourceHolder;
		this.classLoader = getClass().getClassLoader();
	}

	public void setProperty(String key, String value)
	{
		properties.setProperty(key, value);
	}

	public synchronized ExtendedAnnotationConfiguration getConfiguration()
	{
		if( config == null )
		{
			ClassLoader oldLoader = oldLoader();
			try
			{
				setContextLoader(classLoader);
				ExtendedDialect dialect = dataSourceHolder.getDialect();
				this.config = new ExtendedAnnotationConfiguration(dialect);
				config.setProperties(properties);
				config.setProperty(Environment.CONNECTION_PROVIDER, DataSourceProvider.class.getName());
				properties.put(KEY_DATASOURCE, dataSourceHolder.getDataSource());
				config.setProperty(Environment.DIALECT, dialect.getClass().getName());
				config.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
				config.setProperty("javax.persistence.validation.mode", "DDL");
				config.setNamingStrategy(new LowercaseImprovedNamingScheme());
				for( Class<?> class1 : clazzes )
				{
					config.addAnnotatedClass(class1);
				}
			}
			finally
			{
				setContextLoader(oldLoader);
			}
		}
		return config;
	}

	public synchronized Mapping getMapping()
	{
		if( mapping == null )
		{
			ClassLoader oldLoader = oldLoader();
			try
			{
				setContextLoader(classLoader);
				mapping = getConfiguration().buildMapping();
			}
			finally
			{
				setContextLoader(oldLoader);
			}
		}
		return mapping;
	}

	public synchronized SessionFactory getSessionFactory()
	{
		if( sessionFactory == null )
		{
			getMapping();
			ClassLoader oldLoader = oldLoader();
			try
			{
				setContextLoader(classLoader);
				sessionFactory = getConfiguration().buildSessionFactory();
			}
			finally
			{
				setContextLoader(oldLoader);
			}
		}
		return sessionFactory;
	}

	private void setContextLoader(ClassLoader loader)
	{
		Thread.currentThread().setContextClassLoader(loader);
	}

	private ClassLoader oldLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	public String getDefaultSchema()
	{
		return dataSourceHolder.getDefaultSchema();
	}

	public static class DataSourceProvider implements ConnectionProvider
	{
		private DataSource dataSource;

		@Override
		public void configure(Properties props) throws HibernateException
		{
			dataSource = (DataSource) props.get(KEY_DATASOURCE);
		}

		@Override
		public Connection getConnection() throws SQLException
		{
			return dataSource.getConnection();
		}

		@Override
		public void closeConnection(Connection conn) throws SQLException
		{
			conn.close();
		}

		@Override
		public void close() throws HibernateException
		{
			// nothing
		}

		@Override
		public boolean supportsAggressiveRelease()
		{
			return true;
		}

	}

	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

}
