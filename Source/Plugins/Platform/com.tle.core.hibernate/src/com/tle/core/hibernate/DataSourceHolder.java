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

import javax.sql.DataSource;

public class DataSourceHolder
{
	private final ExtendedDialect dialect;
	private final DataSource dataSource;

	public DataSourceHolder(DataSource dataSource, ExtendedDialect dialect)
	{
		this.dataSource = dataSource;
		this.dialect = dialect;
	}

	public DataSource getDataSource()
	{
		return dataSource;
	}

	public String getDefaultSchema()
	{
		Connection connection = null;
		final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(DataSourceHolder.class.getClassLoader());
			connection = this.dataSource.getConnection();
			return dialect.getDefaultSchema(connection);
		}
		catch( SQLException e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			Thread.currentThread().setContextClassLoader(oldLoader);
			if( connection != null )
			{
				try
				{
					connection.close();
				}
				catch( SQLException e )
				{
					// ignore
				}
			}
		}
	}

	public ExtendedDialect getDialect()
	{
		return dialect;
	}
}
