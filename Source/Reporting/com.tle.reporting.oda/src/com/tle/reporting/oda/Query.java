package com.tle.reporting.oda;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import com.tle.reporting.LearningEdgeOdaDelegate;

public class Query implements IQuery
{
	private final Connection connection;
	private final LearningEdgeOdaDelegate delegate;
	private final String queryType;

	private String query;
	private IResultSet results;
	private IParameterMetaData parameterMetadata;
	private List<Object> params;
	private int maxRows = Integer.MAX_VALUE;

	@SuppressWarnings("nls")
	public Query(Connection connection, String datasetId, LearningEdgeOdaDelegate delegate) throws OdaException
	{
		this.connection = connection;
		this.delegate = delegate;
		queryType = datasetId.substring(datasetId.lastIndexOf(".") + 1).toUpperCase();
	}

	public void clearInParameters() throws OdaException
	{
		params = null;
	}

	public void close() throws OdaException
	{
		query = null;
		if( results != null )
		{
			results.close();
			results = null;
		}
		parameterMetadata = null;
		clearInParameters();
		connection.queryClosed(this);
	}

	public IResultSet executeQuery() throws OdaException
	{
		if( results == null )
		{
			results = delegate.executeQuery(queryType, query, params, getMaxRows());
			results.setMaxRows(maxRows);
		}
		return results;
	}

	public int findInParameter(String s) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public IResultSetMetaData getMetaData() throws OdaException
	{
		return executeQuery().getMetaData();
	}

	public IParameterMetaData getParameterMetaData() throws OdaException
	{
		if( parameterMetadata == null )
		{
			parameterMetadata = delegate.getParamterMetadata(queryType, query, params);
		}
		return parameterMetadata;
	}

	public void prepare(String query) throws OdaException
	{
		this.query = query;
	}

	public void setBigDecimal(int i, BigDecimal bigdecimal) throws OdaException
	{
		setParam(i, bigdecimal);
	}

	public void setDate(int i, Date date) throws OdaException
	{
		setParam(i, date);
	}

	public void setDouble(int i, double d) throws OdaException
	{
		setParam(i, d);
	}

	public void setInt(int i, int j) throws OdaException
	{
		setParam(i, j);
	}

	public void setString(int i, String s) throws OdaException
	{
		setParam(i, s);
	}

	public void setTime(int i, Time time) throws OdaException
	{
		setParam(i, time);
	}

	public void setTimestamp(int i, Timestamp timestamp) throws OdaException
	{
		setParam(i, timestamp);
	}

	public void setBoolean(int i, boolean b) throws OdaException
	{
		setParam(i, b);
	}

	private void setParam(int index, Object value)
	{
		index--;
		if( params == null )
		{
			params = new ArrayList<Object>();
		}

		final int size = params.size();
		if( index < size )
		{
			params.set(index, value);
		}
		else if( index == size )
		{
			params.add(value);
		}
		else
		{
			for( int i = size; i < index; i++ )
			{
				params.add(null);
			}
			params.add(value);
		}
	}

	public int getMaxRows() throws OdaException
	{
		return maxRows;
	}

	public void setMaxRows(int maxRows) throws OdaException
	{
		if( maxRows == 0 )
		{
			maxRows = Integer.MAX_VALUE;
		}
		this.maxRows = maxRows;
	}

	public SortSpec getSortSpec() throws OdaException
	{
		return null;
	}

	public void setSortSpec(SortSpec sortspec) throws OdaException
	{
		// nothing
	}

	public void setAppContext(Object obj) throws OdaException
	{
		// nothing
	}

	public void setBigDecimal(String s, BigDecimal bigdecimal) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setBoolean(String s, boolean flag) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setDate(String s, Date date) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setDouble(String s, double d) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setInt(String s, int i) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setNull(String s) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setNull(int i) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setProperty(String s, String s1) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setString(String s, String s1) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setTime(String s, Time time) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void setTimestamp(String s, Timestamp timestamp) throws OdaException
	{
		throw new UnsupportedOperationException();
	}

	public void cancel() throws OdaException, UnsupportedOperationException
	{
		// TODO Auto-generated method stub

	}

	public String getEffectiveQueryText()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public QuerySpecification getSpecification()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setObject(String arg0, Object arg1) throws OdaException
	{
		// TODO Auto-generated method stub

	}

	public void setObject(int arg0, Object arg1) throws OdaException
	{
		// TODO Auto-generated method stub

	}

	public void setSpecification(QuerySpecification arg0) throws OdaException, UnsupportedOperationException
	{
		// TODO Auto-generated method stub

	}
}
