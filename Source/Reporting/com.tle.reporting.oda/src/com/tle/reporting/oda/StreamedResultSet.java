package com.tle.reporting.oda;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;

public class StreamedResultSet implements IResultSetExt
{
	private final MetadataBean metadata;
	private final PostMethod connection;
	private final ObjectInputStream stream;
	private int maxRows = Integer.MAX_VALUE;
	private int currentRow;
	private boolean finished;
	private Object[] row;
	private final Map<String, Integer> columnMappings = new HashMap<String, Integer>();
	private boolean wasNull;

	public StreamedResultSet(PostMethod connection, ObjectInputStream stream) throws IOException,
		ClassNotFoundException
	{
		this.connection = connection;
		metadata = (MetadataBean) stream.readObject();
		this.stream = stream;
		try
		{
			int colcount = metadata.getColumnCount();
			for( int i = 1; i <= colcount; i++ )
			{
				columnMappings.put(metadata.getColumnName(i), i);
			}
		}
		catch( OdaException e )
		{
			throw new RuntimeException(e);
		}
	}

	public Object getObject(int i)
	{
		Object obj = row[i - 1];
		wasNull = obj == null;
		return obj;
	}

	public void close() throws OdaException
	{
		connection.abort();
		connection.releaseConnection();
	}

	public int findColumn(String columnName) throws OdaException
	{
		return columnMappings.get(columnName);
	}

	public BigDecimal getBigDecimal(int i) throws OdaException
	{
		return (BigDecimal) getObject(i);
	}

	public BigDecimal getBigDecimal(String columnName) throws OdaException
	{
		return getBigDecimal(findColumn(columnName));
	}

	public IBlob getBlob(int i) throws OdaException
	{
		return null;
	}

	public IBlob getBlob(String arg0) throws OdaException
	{
		return null;
	}

	public boolean getBoolean(int i) throws OdaException
	{
		return (Boolean) getObject(i);
	}

	public boolean getBoolean(String columnName) throws OdaException
	{
		return getBoolean(findColumn(columnName));
	}

	public IClob getClob(int i) throws OdaException
	{
		return null;
	}

	public IClob getClob(String columnName) throws OdaException
	{
		return null;
	}

	public Date getDate(int i) throws OdaException
	{
		return (Date) getObject(i);
	}

	public Date getDate(String columnName) throws OdaException
	{
		return getDate(findColumn(columnName));
	}

	public double getDouble(int i) throws OdaException
	{
		return (Double) getObject(i);
	}

	public double getDouble(String columnName) throws OdaException
	{
		return getDouble(findColumn(columnName));
	}

	public int getInt(int i) throws OdaException
	{
		return (Integer) getObject(i);
	}

	public int getInt(String columnName) throws OdaException
	{
		return getInt(findColumn(columnName));
	}

	public IResultSetMetaData getMetaData() throws OdaException
	{
		return metadata;
	}

	public int getRow() throws OdaException
	{
		return currentRow;
	}

	public String getString(int i) throws OdaException
	{
		return (String) getObject(i);
	}

	public String getString(String columnName) throws OdaException
	{
		return getString(findColumn(columnName));
	}

	public Time getTime(int i) throws OdaException
	{
		return (Time) getObject(i);
	}

	public Time getTime(String columnName) throws OdaException
	{
		return getTime(findColumn(columnName));
	}

	public Timestamp getTimestamp(int i) throws OdaException
	{
		return (Timestamp) getObject(i);
	}

	public Timestamp getTimestamp(String columnName) throws OdaException
	{
		return getTimestamp(findColumn(columnName));
	}

	public boolean next() throws OdaException
	{
		if( finished )
		{
			return false;
		}
		try
		{
			if( currentRow >= maxRows )
			{
				finished = true;
				return false;
			}
			currentRow++;
			Object nextRow = stream.readObject();
			if( nextRow instanceof String )
			{
				String obj = (String) stream.readObject();
				throw new OdaException(obj);
			}
			row = (Object[]) nextRow;

			if( row.length == 0 )
			{
				finished = true;
				return false;
			}
			return true;
		}
		catch( Exception e )
		{
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			throw new OdaException(e);
		}
	}

	public void setMaxRows(int maxRows) throws OdaException
	{
		this.maxRows = maxRows;
	}

	public boolean wasNull() throws OdaException
	{
		return wasNull;
	}

	public Object getObject(String arg0) throws OdaException
	{
		return getObject(findColumn(arg0));
	}
}
