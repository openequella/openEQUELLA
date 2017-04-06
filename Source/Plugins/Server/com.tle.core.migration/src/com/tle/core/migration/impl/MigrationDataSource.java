package com.tle.core.migration.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class MigrationDataSource implements DataSource
{
	private Future<Connection> futureConnection;
	private Connection connection;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private String url;
	private String username;
	private String password;

	public MigrationDataSource(String url, String username, String password)
	{
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public int getLoginTimeout() throws SQLException
	{
		throw new SQLFeatureNotSupportedException();
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
		if( connection == null )
		{
			if( futureConnection == null )
			{
				futureConnection = exec.submit(new Callable<Connection>()
				{
					@Override
					public Connection call() throws Exception
					{
						return DriverManager.getConnection(url, username, password);
					}
				});
			}
			try
			{
				connection = new MigrationConnection(futureConnection.get(15, TimeUnit.SECONDS));
			}
			catch( InterruptedException e )
			{
				throw new RuntimeException(e);
			}
			catch( ExecutionException e )
			{
				throw (SQLException) e.getCause();
			}
			catch( TimeoutException e )
			{
				throw new SQLException(e);
			}
		}
		return connection;
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

	public void close()
	{
		if( connection != null && futureConnection != null )
		{
			try
			{
				Connection actConnection = futureConnection.get(1, TimeUnit.SECONDS);
				actConnection.close();
			}
			catch( Exception e )
			{
				// ignore
			}
		}
		exec.shutdownNow();
	}

}
