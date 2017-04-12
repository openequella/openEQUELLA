package com.tle.reporting.oda;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;

import com.ibm.icu.util.ULocale;
import com.thoughtworks.xstream.XStream;
import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.reporting.oda.webservice.Constants;

public class Connection implements IConnection
{
	private Map<Object, Object> appContext;
	private List<Query> openQueries = new ArrayList<Query>();
	private LearningEdgeOdaDelegate delegate;

	private static Map<String, LearningEdgeOdaDelegate> loggedInProxies = Collections
		.synchronizedMap(new HashMap<String, LearningEdgeOdaDelegate>());
	private static HttpClient httpClient;
	private static XStream xstream;

	public void close() throws OdaException
	{
		List<Query> removeList = new ArrayList<Query>(openQueries);
		for( Query closeable : removeList )
		{
			closeable.close();
		}
	}

	public void queryClosed(Query query)
	{
		openQueries.remove(query);
	}

	public void commit() throws OdaException
	{
		// nothing
	}

	public int getMaxQueries() throws OdaException
	{
		return 0;
	}

	public IDataSetMetaData getMetaData(String arg0) throws OdaException
	{
		return new DataSetMetadata(this);
	}

	public boolean isOpen() throws OdaException
	{
		return delegate != null;
	}

	public IQuery newQuery(String datasetId) throws OdaException
	{
		Query query = new Query(this, datasetId, delegate);
		openQueries.add(query);
		return query;
	}

	public void open(Properties properties) throws OdaException
	{
		if( delegate == null && appContext != null )
		{
			delegate = (LearningEdgeOdaDelegate) appContext.get(com.tle.reporting.Constants.DELEGATE_APP_CONTEXT_KEY);
		}

		if( delegate == null && properties != null )
		{
			String username = properties.getProperty(Constants.WEBSERVICE_USER);
			String url = properties.getProperty(Constants.WEBSERVICE_URL);
			String key = username + url;
			delegate = loggedInProxies.get(key);

			if( delegate == null )
			{
				if( httpClient == null )
				{
					setupRemote();
				}
				delegate = new RemoteLearningEdgeOdaDelegate(httpClient, properties, xstream);
			}
			delegate.login(username, properties.getProperty(Constants.WEBSERVICE_PASSWORD));
			loggedInProxies.put(key, delegate);
		}

		if( delegate == null )
		{
			throw new OdaException("Both the application context, and the given properties, are null!"); //$NON-NLS-1$
		}

	}

	public void rollback() throws OdaException
	{
		// do nothing
	}

	@SuppressWarnings("unchecked")
	public void setAppContext(Object appContext) throws OdaException
	{
		if( appContext instanceof Map )
		{
			this.appContext = (Map<Object, Object>) appContext;
		}
	}

	@SuppressWarnings({"deprecation", "nls"})
	private static synchronized void setupRemote()
	{
		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(10);
		connectionManager.setParams(params);
		BlindSSLSocketFactory.register();
		final SSLSocketFactory defaultSSL = BlindSSLSocketFactory.getDefaultSSL();
		Protocol.registerProtocol("https", new Protocol("https", new SecureProtocolSocketFactory()
		{
			public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException,
				UnknownHostException
			{
				return defaultSSL.createSocket(socket, s, i, flag);
			}

			public Socket createSocket(String s, int i) throws IOException, UnknownHostException
			{
				return defaultSSL.createSocket(s, i);
			}

			public Socket createSocket(String s, int i, InetAddress inetaddress, int j) throws IOException,
				UnknownHostException
			{
				return defaultSSL.createSocket(s, i, inetaddress, j);
			}

			public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3, HttpConnectionParams arg4)
				throws IOException, UnknownHostException, ConnectTimeoutException
			{
				return defaultSSL.createSocket(arg0, arg1, arg2, arg3);
			}
		}, 443));
		xstream = new XStream();
		httpClient = new HttpClient(connectionManager);
	}

	public LearningEdgeOdaDelegate getDelegate()
	{
		return delegate;
	}

	public void setLocale(ULocale arg0) throws OdaException
	{
		// whatevs
	}

}
