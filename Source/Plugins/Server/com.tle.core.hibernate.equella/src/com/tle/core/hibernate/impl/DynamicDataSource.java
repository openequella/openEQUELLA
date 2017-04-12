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
