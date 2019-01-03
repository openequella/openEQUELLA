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
import java.sql.DatabaseMetaData;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.dytech.devlib.Code;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceHolder;
import com.tle.core.institution.InstitutionService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.reporting.Constants;
import com.tle.reporting.IResultSetExt;
import com.tle.reporting.MetadataBean;
import com.tle.reporting.schema.Column;
import com.tle.reporting.schema.Table;

@Bind
@Singleton
@SuppressWarnings("nls")
public class JdbcQueryDelegate implements QueryDelegate
{
	private static final Logger LOGGER = Logger.getLogger(JdbcQueryDelegate.class);

	private final InstitutionService institutionService;
	private final SchemaDataSourceService dataSourceService;

	@Inject
	public JdbcQueryDelegate(InstitutionService institutionService, SchemaDataSourceService dataSourceService)
	{
		this.institutionService = institutionService;
		this.dataSourceService = dataSourceService;
	}

	private DataSourceHolder getDataSourceHolder()
	{
		long schemaId = institutionService.getSchemaIdForInstitution(CurrentInstitution.get());
		return dataSourceService.getReportingDataSourceForId(schemaId);
	}

	@Override
	public Map<String, ?> getDatasourceMetadata() throws OdaException
	{
		DataSourceHolder dataSourceHolder = getDataSourceHolder();

		final String defaultSchema = dataSourceHolder.getDefaultSchema();
		final Map<String, Object> results = new HashMap<String, Object>();
		try
		{
			new JdbcTemplate(dataSourceHolder.getDataSource()).execute(new ConnectionCallback()
			{
				@Override
				public Object doInConnection(Connection connection) throws SQLException, DataAccessException
				{
					DatabaseMetaData metaData = connection.getMetaData();

					results.put(Constants.JDBC_IDENTIFIER_QUOTE_STRING, metaData.getIdentifierQuoteString());
					results.put(Constants.JDBC_TABLE_MAP, getTableMap(metaData));

					return null;
				}

				private Map<String, Table> getTableMap(DatabaseMetaData metaData) throws SQLException
				{
					Map<String, Table> tableMap = new LinkedHashMap<String, Table>();

					ResultSet tables = metaData.getTables(null, defaultSchema, null, new String[]{"TABLE", "VIEW"});
					try
					{
						while( tables.next() )
						{
							String table = tables.getString("TABLE_NAME");
							try
							{
								List<Column> columns = getColumns(metaData, table);
								tableMap.put(table, new Table(table, tables.getString("TABLE_TYPE").equals("VIEW"),
									columns));
							}
							catch( Exception e )
							{
								LOGGER.warn("Error retrieveing columns for table: '" + table + "'", e);
							}
						}
					}
					finally
					{
						tables.close();
					}

					return tableMap;
				}

				private List<Column> getColumns(DatabaseMetaData metaData, String table) throws SQLException
				{
					List<Column> results = new ArrayList<Column>();

					ResultSet cols = metaData.getColumns(null, defaultSchema, table, null);
					try
					{
						while( cols.next() )
						{
							results.add(new Column(cols.getString("COLUMN_NAME"), cols.getString("TYPE_NAME"))); //$NON-NLS-2$
						}
					}
					finally
					{
						cols.close();
					}

					return results;
				}
			});
		}
		catch( BadSqlGrammarException bge )
		{
			throw new OdaException(bge.getSQLException());
		}
		catch( Exception e )
		{
			throw new RuntimeException("com.tle.core.reporting.error.datasource.metadata", e);
		}
		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.core.reporting.QueryDelegate#executeQuery(java.lang.String,
	 * java.util.Map, java.util.Map)
	 */
	@Override
	public IResultSetExt executeQuery(String query, final List<Object> params, final int maxRows) throws OdaException
	{
		query = processQuery(query);

		DataSource dataSource = getDataSourceHolder().getDataSource();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		try
		{
			PreparedStatement statement = connection.prepareStatement(query);
			addParamsToStatement(statement, params);
			int realMaxRows = maxRows;
			if( realMaxRows <= 0 )
			{
				realMaxRows = Integer.MAX_VALUE;
			}
			else
			{
				statement.setMaxRows(maxRows);
			}
			ResultSet set = null;
			ResultSetMetaData metaData = null;
			try
			{
				metaData = statement.getMetaData();
			}
			catch( SQLException sqle )
			{
				// nothing
			}

			if( metaData == null )
			{
				set = statement.executeQuery();
				metaData = statement.getMetaData();
			}

			if( set == null )
			{
				set = statement.executeQuery();
			}
			return new JDBCOdaResultSet(connection, statement, dataSource, set, new MetadataBean(metaData), realMaxRows);
		}
		catch( SQLException sqle )
		{
			// Redmine #6388 - On exception during report building (for example
			// if the report SQL is out-of-date) absence of rollback led to an
			// database error on next dB lookup:
			// 'ERROR: current transaction is aborted, commands ignored until
			// end of transaction block'
			// Such behaviour attributable to the use of bonecp, presumably?
			try
			{
				connection.rollback();
			}
			catch( Exception eeek )
			{
				// Eeek! indeed. Just log this one and continue to attempt to
				// handle original exception
				LOGGER.warn("Failure to rollback after SQLException", eeek);
			}
			DataSourceUtils.releaseConnection(connection, dataSource);
			throw new SQLErrorCodeSQLExceptionTranslator(dataSource).translate("Query", query, sqle);
		}
	}

	@Override
	public MetadataBean getParameterMetadata(String query, final List<Object> params)
	{
		try
		{
			query = processQuery(query);

			return (MetadataBean) new JdbcTemplate(getDataSourceHolder().getDataSource()).execute(query,
				new PreparedStatementCallback()
				{
					@Override
					public Object doInPreparedStatement(PreparedStatement statement) throws SQLException,
						DataAccessException
					{
						addParamsToStatement(statement, params);
						try
						{
							ParameterMetaData parameterMetaData = statement.getParameterMetaData();
							return new MetadataBean(parameterMetaData);
						}
						catch( SQLException sqle )
						{
							return null;
						}
					}
				});
		}
		catch( Exception e )
		{
			String msg = CurrentLocale.get("com.tle.core.reporting.error.parameter.metadata") + query;
			LOGGER.error(msg, e);
			throw new RuntimeException(msg, e);
		}
	}

	protected void addParamsToStatement(PreparedStatement statement, List<Object> params) throws SQLException
	{
		if( params != null )
		{
			final int count = params.size();
			for( int i = 0; i < count; i++ )
			{
				statement.setObject(i + 1, params.get(i));
			}
		}
	}

	private String processQuery(String query)
	{
		query = query.replaceAll("CURRENT_INSTITUTION", Long.toString(CurrentInstitution.get().getDatabaseId()));
		query = query.replaceAll("CURRENT_USER", '\'' + Code.SQL(CurrentUser.getUserID()) + '\'');
		return query;
	}
}
