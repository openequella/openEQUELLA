package com.tle.reporting.oda;

import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class DataSetMetadata implements IDataSetMetaData
{
	private final IConnection connection;

	public DataSetMetadata(IConnection connection)
	{
		this.connection = connection;
	}

	public IConnection getConnection() throws OdaException
	{
		return connection;
	}

	public int getDataSourceMajorVersion() throws OdaException
	{
		return 1;
	}

	public int getDataSourceMinorVersion() throws OdaException
	{
		return 0;
	}

	public IResultSet getDataSourceObjects(String s, String s1, String s2, String s3) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public String getDataSourceProductName() throws OdaException
	{
		return "TLE ODA Driver";
	}

	public String getDataSourceProductVersion() throws OdaException
	{
		return "1.0";
	}

	public int getSQLStateType() throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public int getSortMode()
	{
		return sortModeNone;
	}

	public boolean supportsInParameters() throws OdaException
	{
		return true;
	}

	public boolean supportsMultipleOpenResults() throws OdaException
	{
		return false;
	}

	public boolean supportsMultipleResultSets() throws OdaException
	{
		return false;
	}

	public boolean supportsNamedParameters() throws OdaException
	{
		return false;
	}

	public boolean supportsNamedResultSets() throws OdaException
	{
		return false;
	}

	public boolean supportsOutParameters() throws OdaException
	{
		return false;
	}
}
