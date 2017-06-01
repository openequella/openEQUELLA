/*
 * Copyright 2017 Apereo
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

package com.tle.hibernate.dialect;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;
import org.hibernate.type.CustomType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.util.StringHelper;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.type.HibernateCsvType;
import com.tle.core.hibernate.type.HibernateEscapedString;
import com.tle.core.hibernate.type.ImmutableHibernateXStreamType;

@SuppressWarnings("nls")
public class SQLServerDialect extends org.hibernate.dialect.SQLServer2005Dialect implements ExtendedDialect
{
	private static final CustomType TYPE_BLANKABLE = new CustomType(new HibernateEscapedString(Types.NVARCHAR),
		new String[]{"blankable"});
	private static final CustomType TYPE_XSTREAM = new CustomType(
		new ImmutableHibernateXStreamType(Types.LONGNVARCHAR), new String[]{"xstream_immutable"});
	private static final CustomType TYPE_CSV = new CustomType(new HibernateCsvType(Types.NVARCHAR), new String[]{"csv"});

	private static final String URL_SCHEME = "jdbc:sqlserver://";
	private String dropConstraintsSQL;

	public SQLServerDialect()
	{
		registerColumnType(Types.NVARCHAR, "nvarchar(MAX)");
		registerColumnType(Types.NVARCHAR, 4000, "nvarchar($l)");
		registerColumnType(Types.LONGNVARCHAR, "nvarchar(MAX)");
		registerColumnType(Types.BIGINT, "numeric(19,0)");
		registerColumnType(Types.BIT, "tinyint");
		registerColumnType(Types.BOOLEAN, "tinyint");

		registerHibernateType(Types.NVARCHAR, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.NVARCHAR, 4000, StandardBasicTypes.STRING.getName());
		registerHibernateType(Types.LONGNVARCHAR, StandardBasicTypes.STRING.getName());

		StringBuilder sbuf = new StringBuilder();
		try( InputStream inp = getClass().getResourceAsStream("dropconstraints.sql") )
		{
			// Sonar whinges about the new InputStreamReader not being closed in
			// a finally block, but the existing finally block does that ...?
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(inp, "UTF-8")); // NOSONAR
			String line = null;
			while( (line = bufReader.readLine()) != null )
			{
				sbuf.append(line);
				sbuf.append('\n');
			}
			dropConstraintsSQL = sbuf.toString();
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean canRollbackSchemaChanges()
	{
		return true;
	}

	@Override
	public String getModifyColumnSql(Mapping mapping, Column column, boolean changeNotNull, boolean changeType)
	{
		String columnName = column.getQuotedName(this);
		String nullStr = column.isNullable() ? " NULL" : " NOT NULL";
		String typeStr = ' ' + column.getSqlType(this, mapping);
		return "ALTER COLUMN " + columnName + typeStr + nullStr;
	}

	@Override
	public String getAddNotNullSql(Mapping mapping, Column column)
	{
		String columnName = column.getQuotedName(this);

		String typeStr = ' ' + column.getSqlType(this, mapping);
		return "ALTER COLUMN " + columnName + typeStr + " NOT NULL";
	}

	@Override
	public String getRenameColumnSql(String table, Column column, String name)
	{
		String columnName = column.getQuotedName(this);
		return "EXEC SP_RENAME '" + table + "." + columnName + "', '" + name + "', 'COLUMN'";
	}

	@Override
	public String getDropColumnSql(String table, Column column)
	{
		String dropSQL = dropConstraintsSQL.replaceAll("\\$table", Matcher.quoteReplacement(table));
		dropSQL = dropSQL.replaceAll("\\$column", Matcher.quoteReplacement(column.getName()));
		return dropSQL + "\nalter table " + table + " drop column " + column.getQuotedName(this);
	}

	@Override
	public String getDropConstraintsForColumnSql(String table, String columnName)
	{
		String dropSQL = dropConstraintsSQL.replaceAll("\\$table", Matcher.quoteReplacement(table));
		return dropSQL.replaceAll("\\$column", Matcher.quoteReplacement(columnName));
	}

	@Override
	public String getDropIndexSql(String table, String indexName)
	{
		return "drop index " + StringHelper.qualify(table, quote(indexName));
	}

	@Override
	public boolean supportsModifyWithConstraints()
	{
		return false;
	}

	@Override
	public boolean supportsAutoIndexForUniqueColumn()
	{
		return false;
	}

	@Override
	public String getNameForMetadataQuery(String name, boolean quoted)
	{
		return name;
	}

	@Override
	public boolean requiresNoConstraintsForModify()
	{
		return true;
	}

	@Override
	public boolean requiresAliasOnSubselect()
	{
		return true;
	}

	@Override
	public String getRandomIdentifier()
	{
		return openQuote() + UUID.randomUUID().toString() + closeQuote();
	}

	@Override
	public String getDefaultSchema(Connection connection) throws SQLException
	{
		String username = connection.getMetaData().getUserName();
		PreparedStatement userStatement = connection
			.prepareStatement("SELECT dp.name AS login_name FROM sys.server_principals sp "
				+ "JOIN sys.database_principals dp ON (sp.sid = dp.sid) WHERE sp.name = ?");

		userStatement.setString(1, username);
		ResultSet rs = userStatement.executeQuery();
		String dbUser;
		try
		{
			if( !rs.next() )
			{
				throw new SQLException("Can't find the default_schema for " + username);
			}
			dbUser = rs.getString(1);
		}
		finally
		{
			rs.close();
		}

		PreparedStatement statement = connection
			.prepareStatement("select default_schema_name from sys.database_principals where name = ?");
		statement.setString(1, dbUser);
		ResultSet rs2 = statement.executeQuery();
		try
		{
			if( !rs2.next() )
			{
				throw new SQLException("Can't find the default_schema for " + username);
			}
			return rs2.getString(1);
		}
		finally
		{
			rs2.close();
		}

	}

	@Override
	public String getDisplayNameForUrl(String url)
	{
		if( url.startsWith(URL_SCHEME) )
		{
			return url.substring(URL_SCHEME.length());
		}
		return url;
	}

	@Override
	public Iterable<? extends BasicType> getExtraTypeOverrides()
	{
		return ImmutableList.of(new NStringType(), new LongNStringType(), TYPE_BLANKABLE, TYPE_XSTREAM, TYPE_CSV);
	}

	/**
	 * We don't have the option to create an index on the lower_case value of a
	 * column (as we can in Oracle or Postgres) without going the full distance
	 * of creating a computed column and then indexing on that, hence.<br>
	 * alter table <i>tableName</i> add <i>columnName_indexName</i> as
	 * <i>function</i>(<i>columnName</i>);<br>
	 * create index <i>indexName</i> on
	 * <i>tableName</i>(<i>columnName_indexName</i>); With sqlserver, we're safe
	 * in adding extra characters to the column name, because maximum allowable
	 * length is 128 characters, much longer then the lower length imposed on
	 * EQUELLA by virtue of Oracle (30) or Postgres (63)
	 */
	@Override
	public List<String> getCreateFunctionalIndex(String tableName, String function, String[]... indexes)
	{
		List<String> createIndexStatements = new ArrayList<String>();
		for( String[] indexAndColumns : indexes )
		{
			// 1st element in each indexAndColumns array in the indexes array is
			// the index name which must be followed by one or more column names
			if( indexAndColumns.length < 2 )
			{
				String msg = "Cannot create functional Index on table " + tableName + ", ";
				msg += indexAndColumns.length == 0 ? " no index name or columns"
					: "no columns provided (for index name " + indexAndColumns[0] + ')';
				throw new RuntimeException(msg);
			}
			else
			{
				String indexName = indexAndColumns[0];
				for( int i = 1; i < indexAndColumns.length; ++i )
				{
					String alterTableStatement = "ALTER TABLE " + tableName + " ADD " + indexName + "_" + indexName
						+ " AS " + function + '(' + indexAndColumns[i] + ')';
					createIndexStatements.add(alterTableStatement);
					String createIndexStatement = "CREATE INDEX " + indexName + " ON " + tableName + '('
						+ indexAndColumns[i] + ')';
					createIndexStatements.add(createIndexStatement);
				}
			}
		}
		return createIndexStatements;
	}
}
