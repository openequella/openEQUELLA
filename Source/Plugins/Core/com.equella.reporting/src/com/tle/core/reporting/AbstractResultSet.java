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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;

public abstract class AbstractResultSet implements IResultSetExt
{
	protected MetadataBean metaData;
	private Map<String, Integer> columnMappings = new HashMap<String, Integer>();

	public AbstractResultSet(MetadataBean metadata)
	{
		this.metaData = metadata;
		try
		{
			int colcount = metaData.getColumnCount();
			for( int i = 1; i <= colcount; i++ )
			{
				columnMappings.put(metaData.getColumnName(i), i);
			}
		}
		catch( OdaException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws OdaException
	{
		// Don't care
	}

	@Override
	public int findColumn(String s) throws OdaException
	{
		return columnMappings.get(s);
	}

	@Override
	public BigDecimal getBigDecimal(int i) throws OdaException
	{
		return (BigDecimal) getCol(i);
	}

	protected abstract Object getCol(int i);

	@Override
	public IBlob getBlob(int i) throws OdaException
	{
		return null;
	}

	@Override
	public IClob getClob(int i) throws OdaException
	{
		return null;
	}

	@Override
	public Date getDate(int i) throws OdaException
	{
		return (Date) getCol(i);
	}

	@Override
	public double getDouble(int i) throws OdaException
	{
		return (Double) getCol(i);
	}

	@Override
	public int getInt(int i) throws OdaException
	{
		return (Integer) getCol(i);
	}

	@Override
	public IResultSetMetaData getMetaData() throws OdaException
	{
		return metaData;
	}

	@Override
	public String getString(int i) throws OdaException
	{
		return (String) getCol(i);
	}

	@Override
	public Time getTime(int i) throws OdaException
	{
		return (Time) getCol(i);
	}

	@Override
	public Timestamp getTimestamp(int i) throws OdaException
	{
		return (Timestamp) getCol(i);
	}

	@Override
	public BigDecimal getBigDecimal(String column) throws OdaException
	{
		return getBigDecimal(findColumn(column));
	}

	@Override
	public IBlob getBlob(String column) throws OdaException
	{
		return getBlob(findColumn(column));
	}

	@Override
	public boolean getBoolean(int i) throws OdaException
	{
		return (Boolean) getCol(i);
	}

	@Override
	public boolean getBoolean(String column) throws OdaException
	{
		return getBoolean(findColumn(column));
	}

	@Override
	public IClob getClob(String column) throws OdaException
	{
		return getClob(findColumn(column));
	}

	@Override
	public Date getDate(String column) throws OdaException
	{
		return getDate(findColumn(column));
	}

	@Override
	public double getDouble(String column) throws OdaException
	{
		return getDouble(findColumn(column));
	}

	@Override
	public int getInt(String column) throws OdaException
	{
		return getInt(findColumn(column));
	}

	@Override
	public String getString(String column) throws OdaException
	{
		return getString(findColumn(column));
	}

	@Override
	public Time getTime(String column) throws OdaException
	{
		return getTime(findColumn(column));
	}

	@Override
	public Timestamp getTimestamp(String column) throws OdaException
	{
		return getTimestamp(findColumn(column));
	}

	@Override
	public Object getObject(int i)
	{
		return getCol(i);
	}

}
