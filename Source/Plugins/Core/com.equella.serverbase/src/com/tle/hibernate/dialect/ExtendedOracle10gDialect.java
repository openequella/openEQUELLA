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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.engine.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;
import org.hibernate.type.CustomType;
import org.hibernate.type.StandardBasicTypes;

import com.google.common.collect.ImmutableList;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.type.HibernateCsvType;
import com.tle.core.hibernate.type.HibernateEscapedString;
import com.tle.core.hibernate.type.ImmutableHibernateXStreamType;

@SuppressWarnings("nls")
public class ExtendedOracle10gDialect extends Oracle10gDialect implements ExtendedDialect
{
	private static final CustomType TYPE_BLANKABLE = new CustomType(new HibernateEscapedString(Types.VARCHAR),
		new String[]{"blankable"});
	private static final CustomType TYPE_XSTREAM = new CustomType(new ImmutableHibernateXStreamType(Types.CLOB),
		new String[]{"xstream_immutable"});
	private static final CustomType TYPE_CSV = new CustomType(new HibernateCsvType(Types.VARCHAR), new String[]{"csv"});

	private static final String URL_SCHEME = "jdbc:oracle:thin:"; //$NON-NLS-1$

	public ExtendedOracle10gDialect()
	{
		registerHibernateType(Types.CLOB, StandardBasicTypes.STRING.getName());
	}

	@Override
	public boolean canRollbackSchemaChanges()
	{
		return false;
	}

	@Override
	public boolean supportsAutoIndexForUniqueColumn()
	{
		return true;
	}

	@Override
	public String getModifyColumnSql(Mapping mapping, Column column, boolean changeNotNull, boolean changeType)
	{
		String columnName = column.getQuotedName(this);

		String nullStr = "";
		if( changeNotNull )
		{
			nullStr = column.isNullable() ? " NULL" : " NOT NULL";
		}
		String typeStr = "";
		if( changeType )
		{
			typeStr = ' ' + column.getSqlType(this, mapping);
		}

		return "MODIFY (" + columnName + typeStr + nullStr + ")";
	}

	@Override
	public String getAddNotNullSql(Mapping mapping, Column column)
	{
		String columnName = column.getQuotedName(this);
		return "MODIFY (" + columnName + " NOT NULL)";
	}

	@Override
	public String getRenameColumnSql(String table, Column column, String name)
	{
		String columnName = column.getQuotedName(this);
		return "ALTER TABLE " + table + " RENAME COLUMN " + columnName + " TO " + name;
	}

	@Override
	public String getDropColumnSql(String table, Column column)
	{
		return "ALTER TABLE " + table + " DROP COLUMN " + column.getQuotedName(this) + " CASCADE CONSTRAINTS";
	}

	@Override
	public String getDropIndexSql(String table, String indexName)
	{
		return "DROP INDEX " + quote(indexName);
	}

	@Override
	public boolean supportsModifyWithConstraints()
	{
		return true;
	}

	@Override
	public String getNameForMetadataQuery(String name, boolean quoted)
	{
		if( quoted )
		{
			return name;
		}
		return name.toUpperCase();
	}

	@Override
	public boolean requiresAliasOnSubselect()
	{
		return false;
	}

	@Override
	public String getDropConstraintsForColumnSql(String table, String columnName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean requiresNoConstraintsForModify()
	{
		return false;
	}

	@Override
	public String getRandomIdentifier()
	{
		return openQuote() + UUID.randomUUID().toString().substring(0, 10) + closeQuote();
	}

	@Override
	public String getDefaultSchema(Connection connection) throws SQLException
	{
		return connection.getMetaData().getUserName().toUpperCase();
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
		return ImmutableList.of(TYPE_BLANKABLE, TYPE_CSV, TYPE_XSTREAM);
	}

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
					String createIndexStatement = "CREATE INDEX " + indexName + " ON " + tableName + " (" + function
						+ '(' + indexAndColumns[i] + ") )";
					createIndexStatements.add(createIndexStatement);
				}
			}
		}
		return createIndexStatements;
	}
}
