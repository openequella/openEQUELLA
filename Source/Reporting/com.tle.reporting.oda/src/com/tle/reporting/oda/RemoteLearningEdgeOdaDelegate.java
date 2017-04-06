package com.tle.reporting.oda;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.thoughtworks.xstream.XStream;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.reporting.oda.ui.TLEOdaPlugin;
import com.tle.reporting.oda.webservice.Constants;

public class RemoteLearningEdgeOdaDelegate implements LearningEdgeOdaDelegate
{
	private static final String STATUS_OK = "OK"; //$NON-NLS-1$
	private final HttpClient httpClient;
	private String url;
	private final XStream xstream;

	public RemoteLearningEdgeOdaDelegate(HttpClient httpClient, Properties properties, XStream xstream)
	{
		this.httpClient = httpClient;
		url = properties.getProperty(Constants.WEBSERVICE_URL);
		if( !url.endsWith("/") ) //$NON-NLS-1$
		{
			url += '/';
		}
		url += "reportingstream.do"; //$NON-NLS-1$
		this.xstream = xstream;
	}

	@SuppressWarnings("nls")
	public IResultSetExt executeQuery(String queryType, String query, List<Object> indexParams, int maxRows)
		throws OdaException
	{
		PostMethod method = new PostMethod(url);
		method.addParameter("method", "query");
		method.addParameter("type", queryType);
		method.addParameter("query", query);
		method.addParameter("maxRows", Integer.toString(maxRows));
		if( indexParams != null && indexParams.size() > 0 )
		{
			method.addParameter("params", xstream.toXML(indexParams));
		}
		try
		{
			httpClient.executeMethod(method);
			InputStream stream = method.getResponseBodyAsStream();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return new StreamedResultSet(method, ois);
		}
		catch( Exception e )
		{
			method.releaseConnection();
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings({ "unchecked", "nls" })
	public Map<String, ?> getDatasourceMetadata(String queryType) throws OdaException
	{
		PostMethod method = new PostMethod(url);
		method.addParameter("method", "metadata");
		method.addParameter("type", queryType);

		try
		{
			httpClient.executeMethod(method);
			InputStream stream = method.getResponseBodyAsStream();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return (Map<String, ?>) ois.readObject();
		}
		catch( Exception e )
		{
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			throw new RuntimeException(e);
		}
		finally
		{
			method.releaseConnection();
		}
	}

	@SuppressWarnings("nls")
	public IParameterMetaData getParamterMetadata(String queryType, String query, List<Object> indexParams)
		throws OdaException
	{
		PostMethod method = new PostMethod(url);
		method.addParameter("method", "paramMetadata");
		method.addParameter("type", queryType);
		method.addParameter("query", query);
		if( indexParams != null && indexParams.size() > 0 )
		{
			method.addParameter("params", xstream.toXML(indexParams));
		}

		try
		{
			httpClient.executeMethod(method);
			InputStream stream = method.getResponseBodyAsStream();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				String errorMsg = (String) ois.readObject();
				throw new OdaException(errorMsg);
			}
			return (IParameterMetaData) ois.readObject();
		}
		catch( Exception e )
		{
			throw new RuntimeException(e);
		}
		finally
		{
			method.releaseConnection();
		}
	}

	@SuppressWarnings("nls")
	public String login(String username, String password) throws OdaException
	{
		PostMethod method = new PostMethod(url);
		method.addParameter("method", "login");
		method.addParameter("username", username);
		method.addParameter("password", password);
		try
		{
			httpClient.executeMethod(method);
			InputStream stream = method.getResponseBodyAsStream();
			ObjectInputStream ois = new ObjectInputStream(stream);
			String status = (String) ois.readObject();
			if( !status.equals(STATUS_OK) )
			{
				throw new OdaException("Failed to login: (" + status + ")");
			}
		}
		catch( Exception e )
		{
			method.releaseConnection();
			if( e instanceof OdaException )
			{
				throw (OdaException) e;
			}
			TLEOdaPlugin plugin = TLEOdaPlugin.getDefault();
			plugin.getLog().log(new Status(IStatus.ERROR, plugin.getBundle().getSymbolicName(), "ERROR", e));
			throw new RuntimeException(e);
		}
		method.releaseConnection();
		return ""; //$NON-NLS-1$
	}

}
