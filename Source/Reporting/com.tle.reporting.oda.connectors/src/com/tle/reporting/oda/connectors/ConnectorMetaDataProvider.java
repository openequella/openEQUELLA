package com.tle.reporting.oda.connectors;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSourceDesign;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;

import com.tle.reporting.LearningEdgeOdaDelegate;
import com.tle.reporting.oda.Connection;
import com.tle.reporting.oda.Driver;

/**
 * @author Aaron
 */
public class ConnectorMetaDataProvider
{
	private static ConnectorMetaDataProvider instance = null;
	private static Logger logger = Logger.getLogger(ConnectorMetaDataProvider.class.getName());

	private final Connection connection;
	private final Properties props;
	private Map<String, ?> datasourceMetadata;

	public static ConnectorMetaDataProvider getInstance()
	{
		return instance;
	}

	public static void createInstance(DataSetDesign dataSetDesign)
	{
		release();
		DataSourceDesign dataSourceDesign = dataSetDesign.getDataSourceDesign();

		Driver driver = new Driver();
		Connection ourConnect = null;
		Properties props = new Properties();
		try
		{
			ourConnect = (Connection) driver.getConnection(dataSourceDesign.getOdaExtensionDataSourceId());

			props = DesignSessionUtil.getEffectiveDataSourceProperties(dataSourceDesign);
		}
		catch( OdaException e )
		{
			logger.log(Level.WARNING, e.getMessage(), e);
		}
		instance = new ConnectorMetaDataProvider(ourConnect, props);
	}

	private ConnectorMetaDataProvider(Connection ourConnect, Properties props)
	{
		this.connection = ourConnect;
		this.props = props;
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAllConnectors()
	{
		return (Map<String, String>) getMetadata().get("connector.connectors");
	}

	private Map<String, ?> getMetadata()
	{
		if( datasourceMetadata == null )
		{
			try
			{
				LearningEdgeOdaDelegate delegate = connection.getDelegate();
				datasourceMetadata = delegate.getDatasourceMetadata("CONNECTOR");
			}
			catch( OdaException e )
			{
				throw new RuntimeException(e);
			}
		}
		return datasourceMetadata;
	}

	public static void release()
	{
		if( instance != null )
		{
			instance.closeConnection();
			instance = null;
		}
	}

	public void reconnect() throws SQLException, OdaException
	{
		closeConnection();
		connection.open(props);
	}

	private void closeConnection()
	{
		if( connection != null )
		{
			try
			{
				connection.close();
			}
			catch( OdaException e )
			{
				// just ignore it
			}
		}
	}
}
