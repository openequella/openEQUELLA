/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.reporting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.tle.reporting.MetadataBean;

public class JDBCOdaResultSet extends AbstractResultSet
{

	private ResultSet resultSet;
	private int currentRow = 0;
	private int numCols;
	private Object[] rowData;
	private int maxRows = Integer.MAX_VALUE;
	private boolean wasnull;
	private Connection connection;
	private PreparedStatement statement;
	private DataSource dataSource;

	public JDBCOdaResultSet(Connection connection, PreparedStatement statement, DataSource dataSource,
		ResultSet resultSet, MetadataBean metadata, int maxRows)
	{
		super(metadata);
		this.connection = connection;
		this.statement = statement;
		this.dataSource = dataSource;
		this.resultSet = resultSet;
		this.maxRows = maxRows;
		try
		{
			numCols = metadata.getColumnCount();
			rowData = new Object[numCols];
		}
		catch( OdaException e )
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public void close() throws OdaException
	{
		try
		{
			if( resultSet != null )
			{
				resultSet.close();
			}
			if( statement != null )
			{
				statement.close();
			}
			if( connection != null )
			{
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
			statement = null;
			connection = null;
			dataSource = null;
			rowData = null;
			resultSet = null;
		}
		catch( SQLException e )
		{
			throw new OdaException(e);
		}
	}

	@Override
	protected Object getCol(int i)
	{
		Object obj = rowData[i - 1];
		wasnull = obj == null;
		return obj;
	}

	@Override
	public int getRow() throws OdaException
	{
		return currentRow;
	}

	@Override
	public boolean next() throws OdaException
	{
		if( currentRow >= maxRows )
		{
			return false;
		}
		try
		{
			if( resultSet.next() )
			{
				currentRow++;
				for( int i = 1; i <= numCols; i++ )
				{
					int type = metaData.getColumnType(i);
					Object obj;
					switch( type )
					{
						case Types.TIMESTAMP:
							obj = resultSet.getTimestamp(i);
							break;
						case Types.FLOAT:
						case Types.REAL:
						case Types.DOUBLE:
							obj = resultSet.getDouble(i);
							break;
						case Types.INTEGER:
						case Types.SMALLINT:
						case Types.TINYINT:
							obj = resultSet.getInt(i);
							break;
						case Types.BIGINT:
						case Types.DECIMAL:
						case Types.NUMERIC:
							obj = resultSet.getBigDecimal(i);
							break;
						case Types.BOOLEAN:
						case Types.BIT:
							obj = resultSet.getBoolean(i) ? 1 : 0;
							break;
						default:
							obj = resultSet.getString(i);
							break;
					}
					rowData[i - 1] = obj;
				}
				return true;
			}
			return false;
		}
		catch( SQLException sqle )
		{
			throw new OdaException(sqle);
		}
	}

	@Override
	public void setMaxRows(int maxRows) throws OdaException
	{
		if( maxRows == 0 )
		{
			maxRows = Integer.MAX_VALUE;
		}
		this.maxRows = maxRows;
	}

	@Override
	public boolean wasNull() throws OdaException
	{
		return wasnull;
	}

	@Override
	public Object getObject(String colName) throws OdaException
	{
		return getObject(findColumn(colName));
	}
}
