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
		try
		{
			connection = this.dataSource.getConnection();
			return dialect.getDefaultSchema(connection);
		}
		catch( SQLException e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
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
