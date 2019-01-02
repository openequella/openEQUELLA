/*
 * Copyright 2019 Apereo
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

package com.tle.core.reporting;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.reporting.MetadataBean;

public class SimpleResultSet extends AbstractResultSet
{
	private Object[][] data;
	private int maxRows;
	private int currentRow = -1;
	private boolean wasnull;

	public SimpleResultSet(List<Object[]> data, MetadataBean metadata)
	{
		this(data.toArray(new Object[data.size()][]), metadata);
	}

	public SimpleResultSet(Object[][] data, MetadataBean metadata)
	{
		super(metadata);
		if( data == null )
		{
			data = new Object[0][];
		}
		this.data = data;
		maxRows = data.length;
	}

	@Override
	public void close() throws OdaException
	{
		// Don't care
	}

	@Override
	public BigDecimal getBigDecimal(int i) throws OdaException
	{
		return (BigDecimal) getCol(i);
	}

	@Override
	protected Object getCol(int i)
	{
		Object obj = data[currentRow][i - 1];
		wasnull = obj == null;
		return obj;
	}

	@Override
	public int getRow() throws OdaException
	{
		return currentRow + 1;
	}

	@Override
	public boolean next() throws OdaException
	{
		return !(++currentRow >= maxRows);
	}

	@Override
	public void setMaxRows(int max) throws OdaException
	{
		maxRows = Math.min(data.length, max);
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
