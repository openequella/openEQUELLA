/*
 * Copyright (c) 2006, 2007 Actuate Corporation. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html Contributors: Actuate
 * Corporation - initial API and implementation
 */
package com.tle.reporting.oda.ui.jdbc.ui.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.design.DataSetDesign;
import org.eclipse.datatools.connectivity.oda.design.DataSourceDesign;
import org.eclipse.datatools.connectivity.oda.design.ui.designsession.DesignSessionUtil;

import com.tle.reporting.Constants;
import com.tle.reporting.oda.Connection;
import com.tle.reporting.oda.Driver;
import com.tle.reporting.schema.Column;
import com.tle.reporting.schema.Table;

public class JdbcMetaDataProvider
{
	private final Connection connection;
	private Map<String, ?> datasourceMetadata;
	private final Properties props;

	private static Logger logger = Logger.getLogger(JdbcMetaDataProvider.class.getName());

	private static JdbcMetaDataProvider instance = null;

	private JdbcMetaDataProvider(Connection ourConnect, Properties props)
	{
		this.connection = ourConnect;
		this.props = props;
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
		instance = new JdbcMetaDataProvider(ourConnect, props);
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

	public static JdbcMetaDataProvider getInstance()
	{
		return instance;
	}

	public String getIdentifierQuoteString()
	{
		Map<String, ?> metadata = getMetadata();
		return (String) metadata.get(Constants.JDBC_IDENTIFIER_QUOTE_STRING);
	}

	public boolean isSupportProcedure()
	{
		return false;
	}

	public boolean isSupportSchema()
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<Column> getTableColumns(String tableName)
	{
		Map<String, ?> metadata = getMetadata();
		Map<String, Table> tables = (Map<String, Table>) metadata.get(Constants.JDBC_TABLE_MAP);
		return tables.get(tableName).getColumns();
	}

	public ResultSet getProcedures(String schemaPattern, String procedureNamePattern)
	{
		return null;
	}

	public ResultSet getProcedureColumns(String schemaPattern, String procedureNamePattern, String columnNamePattern)
	{
		return null;
	}

	private Map<String, ?> getMetadata()
	{
		if( datasourceMetadata == null )
		{
			try
			{
				datasourceMetadata = connection.getDelegate().getDatasourceMetadata("JDBC");
			}
			catch( OdaException e )
			{
				throw new RuntimeException(e);
			}
		}
		return datasourceMetadata;
	}

	@SuppressWarnings({ "unchecked", "nls" })
	public Collection<Table> getFilteredTables(String filterPattern)
	{
		Map<String, ?> metadata = getMetadata();
		Map<String, Table> tables = (Map<String, Table>) metadata.get(Constants.JDBC_TABLE_MAP);
		if( filterPattern == null || filterPattern.equals("%") )
		{
			return tables.values();
		}
		Pattern matcher = Pattern.compile(filterPattern.replace("%", ".*"));
		List<Table> matchingTables = new ArrayList<Table>();
		for( Table table : tables.values() )
		{
			if( matcher.matcher(table.getName()).matches() )
			{
				matchingTables.add(table);
			}
		}
		return matchingTables;
	}

	public ResultSet getAllSchemas()
	{
		return null;
	}

	public String[] getAllSchemaNames()
	{
		return null;
	}
}