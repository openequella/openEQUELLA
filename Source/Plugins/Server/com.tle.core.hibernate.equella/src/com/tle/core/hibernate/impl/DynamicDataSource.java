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

package com.tle.core.hibernate.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.sql.DataSource;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.hibernate.DataSourceHolder;

@Bind
@Singleton
public class DynamicDataSource implements DataSource
{
	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		return getCurrentDataSource().getLogWriter();
	}

	private DataSource getCurrentDataSource()
	{
		DataSourceHolder dataSourceHolder = CurrentDataSource.get();
		if( dataSourceHolder == null )
		{
			throw new NullPointerException("No current data source");
		}
		return dataSourceHolder.getDataSource();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		getCurrentDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
		getCurrentDataSource().setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException
	{
		return getCurrentDataSource().getLoginTimeout();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException
	{
		return getCurrentDataSource().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("all")
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{
		throw new SQLFeatureNotSupportedException();
	}
}
