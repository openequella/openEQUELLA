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

package com.tle.core.hibernate.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.classic.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.Mapping;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

import com.tle.core.hibernate.ExtendedAnnotationConfiguration;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.HibernateFactory;

@SuppressWarnings("nls")
public class HibernateMigrationHelper
{
	private final Dialect dialect;
	private final ExtendedDialect extDialect;
	private final String defaultCatalog;
	private final String defaultSchema;
	private final Mapping mapping;
	private final ExtendedAnnotationConfiguration configuration;
	private final SessionFactoryImpl factory;

	private static final Log LOGGER = LogFactory.getLog(HibernateMigrationHelper.class);

	public HibernateMigrationHelper(HibernateFactory factory)
	{
		this(factory, factory.getDefaultSchema());
	}

	public HibernateMigrationHelper(HibernateFactory factory, String defaultSchema)
	{
		this.factory = (SessionFactoryImpl) factory.getSessionFactory();
		dialect = this.factory.getDialect();
		extDialect = (ExtendedDialect) dialect;
		this.configuration = factory.getConfiguration();
		mapping = configuration.buildMapping();
		this.defaultSchema = defaultSchema;
		defaultCatalog = null;
	}

	public List<String> getDropTableSql(String... tables)
	{
		List<String> drops = new ArrayList<String>();
		for( String tableName : tables )
		{
			Table table = findTable(tableName);
			drops.add(table.sqlDropString(dialect, defaultCatalog, defaultSchema));
		}
		return drops;
	}

	@SuppressWarnings("unchecked")
	public List<String> getCreationSql(HibernateCreationFilter filter)
	{
		List<String> sqlStrings = new ArrayList<String>();
		if( LOGGER.isDebugEnabled() )
		{
			LOGGER.debug("Tables registered:" + configuration.getTableMap().keySet());
		}
		for( Table table : configuration.getTableMap().values() )
		{
			if( table.isPhysicalTable() && filter.includeTable(table) )
			{
				sqlStrings.add(table.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
			}
		}

		for( Table table : configuration.getTableMap().values() )
		{
			if( table.isPhysicalTable() )
			{

				Iterator<Index> subIter = table.getIndexIterator();
				while( subIter.hasNext() )
				{
					Index index = subIter.next();
					if( filter.includeIndex(table, index)
						&& (!extDialect.supportsAutoIndexForUniqueColumn() || !hasUniqueIndex(index, table)) )
					{
						sqlStrings.add(index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
					}
				}

				if( dialect.hasAlterTable() )
				{
					Iterator<ForeignKey> forIter = table.getForeignKeyIterator();
					while( forIter.hasNext() )
					{
						ForeignKey fk = forIter.next();
						if( fk.isPhysicalConstraint() && filter.includeForeignKey(table, fk) )
						{
							sqlStrings.add(fk.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
						}
					}
				}

			}
		}
		Collection<PersistentIdentifierGenerator> generators = configuration.getGenerators(dialect, defaultCatalog,
			defaultSchema);
		for( PersistentIdentifierGenerator pig : generators )
		{
			if( filter.includeGenerator(pig) )
			{
				String[] lines = pig.sqlCreateStrings(dialect);
				Collections.addAll(sqlStrings, lines);
			}
		}

		for( AuxiliaryDatabaseObject object : configuration.getAuxiliaryDatabaseObjects() )
		{
			if( object.appliesToDialect(dialect) && filter.includeObject(object) )
			{
				sqlStrings.add(object.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
			}
		}
		return sqlStrings;
	}

	public List<String> getAddColumnsSQL(String tableName, String... columnNames)
	{
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		for( String columnName : columnNames )
		{
			Column column = table.getColumn(new Column(columnName));

			StringBuffer alter = new StringBuffer("alter table ")
				.append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(' ')
				.append(dialect.getAddColumnString());
			alter.append(' ').append(column.getQuotedName(dialect)).append(' ')
				.append(column.getSqlType(dialect, mapping));

			boolean useUniqueConstraint = extDialect.supportsModifyWithConstraints() && column.isUnique()
				&& dialect.supportsUnique() && (!column.isNullable() || dialect.supportsNotNullUnique());
			if( useUniqueConstraint )
			{
				alter.append(" unique");
			}
			if( column.hasCheckConstraint() && dialect.supportsColumnCheck() )
			{
				alter.append(" check(").append(column.getCheckConstraint()).append(")");
			}

			String columnComment = column.getComment();
			if( columnComment != null )
			{
				alter.append(dialect.getColumnComment(columnComment));
			}
			sqlStrings.add(alter.toString());
		}
		return sqlStrings;
	}

	/**
	 * By default we assume that foreign key constraints will be added for the
	 * columnNames
	 * 
	 * @param tableName
	 * @param columnNames
	 * @return
	 */
	public List<String> getAddIndexesAndConstraintsForColumns(String tableName, String... columnNames)
	{
		return getAddIndexesAndConstraintsForColumns(tableName, true, columnNames);
	}

	@SuppressWarnings({"unchecked"})
	public List<String> getAddIndexesAndConstraintsForColumns(String tableName, boolean includeForeignKeyConstraints,
		String... columnNames)
	{
		Set<Column> colSet = new HashSet<Column>();
		for( String columnName : columnNames )
		{
			colSet.add(new Column(columnName));
		}
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		if( !extDialect.supportsModifyWithConstraints() )
		{
			for( Column col : colSet )
			{
				Column realCol = table.getColumn(col);
				if( realCol.isUnique() )
				{
					table.createUniqueKey(Collections.singletonList(realCol));
				}
			}
		}
		Iterator<UniqueKey> keyIter = table.getUniqueKeyIterator();
		if( dialect.supportsUniqueConstraintInCreateAlterTable() )
		{
			while( keyIter.hasNext() )
			{
				UniqueKey uk = keyIter.next();
				if( !Collections.disjoint(uk.getColumns(), colSet) )
				{
					StringBuilder buf = new StringBuilder("alter table ");
					buf.append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema));
					buf.append(" add constraint ");
					buf.append(extDialect.getRandomIdentifier());
					buf.append(' ');
					String constraint = uk.sqlConstraintString(dialect);
					if( constraint != null )
					{
						buf.append(constraint);
						sqlStrings.add(buf.toString());
					}
				}
			}
		}
		else
		{
			while( keyIter.hasNext() )
			{
				UniqueKey ukey = keyIter.next();
				if( !Collections.disjoint(ukey.getColumns(), colSet) )
				{
					sqlStrings.add(ukey.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
				}
			}
		}

		addIndexSQL(sqlStrings, table, colSet);

		// Caller may opt to skip foreign key constraints
		if( includeForeignKeyConstraints )
		{
			Iterator<ForeignKey> fkeyIter = table.getForeignKeyIterator();
			while( fkeyIter.hasNext() )
			{
				ForeignKey fkey = fkeyIter.next();
				if( !Collections.disjoint(fkey.getColumns(), colSet) )
				{
					sqlStrings.add(fkey.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
				}
			}
		}

		return sqlStrings;
	}

	public List<String> getAddIndexesForColumns(String tableName, String... columnNames)
	{
		Set<Column> colSet = new HashSet<Column>();
		for( String columnName : columnNames )
		{
			colSet.add(new Column(columnName));
		}
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		addIndexSQL(sqlStrings, table, colSet);
		return sqlStrings;
	}

	@SuppressWarnings({"unchecked"})
	private void addIndexSQL(List<String> sqlStrings, Table table, Set<Column> colSet)
	{
		Iterator<Index> indexIterator = table.getIndexIterator();
		while( indexIterator.hasNext() )
		{
			Index index = indexIterator.next();
			Iterator<Column> colIter = index.getColumnIterator();
			boolean found = false;
			while( colIter.hasNext() )
			{
				Column col = colIter.next();
				if( colSet.contains(col) )
				{
					found = true;
					break;
				}
			}
			if( found && (!extDialect.supportsAutoIndexForUniqueColumn() || !hasUniqueIndex(index, table)) )
			{
				sqlStrings.add(index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
			}
		}
	}

	public String getAddNamedIndex(String tableName, String indexName, String... columnNames)
	{
		Index index = new Index();
		index.setName(indexName);
		index.setTable(findTable(tableName));
		for( String columnName : columnNames )
		{
			index.addColumn(new Column(columnName));
		}
		return index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
	}

	public String getDropNamedIndex(String tableName, String indexName)
	{
		return extDialect.getDropIndexSql(tableName, indexName);
	}

	@SuppressWarnings("unchecked")
	private boolean hasUniqueIndex(Index index, Table table)
	{
		HashSet<Column> indexCols = new HashSet<Column>();
		Iterator<Column> icolIter = index.getColumnIterator();
		while( icolIter.hasNext() )
		{
			Column col = icolIter.next();
			indexCols.add(col);
			if( index.getColumnSpan() == 1 && table.getColumn(col).isUnique() )
			{
				return true;
			}
		}
		Iterator<UniqueKey> iter = table.getUniqueKeyIterator();
		while( iter.hasNext() )
		{
			UniqueKey uk = iter.next();
			if( uk.getColumnSpan() == indexCols.size() && indexCols.containsAll(uk.getColumns()) )
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param tableName
	 * @param columnName
	 * @param changeNotNull
	 * @param nullable Pass in null to use the annotation on the column. This is
	 *            not always possible (see Redmine #3329).
	 * @param changeType
	 * @return
	 */
	public List<String> getModifyColumnSQL(String tableName, String columnName, boolean changeNotNull,
		boolean changeType)
	{
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		Column column = table.getColumn(new Column(columnName));

		StringBuffer alter = new StringBuffer("alter table ")
			.append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(' ')
			.append(extDialect.getModifyColumnSql(mapping, column, changeNotNull, changeType));

		sqlStrings.add(alter.toString());
		return sqlStrings;
	}

	public boolean tableExists(Session session, final String table)
	{
		final ExtendedDialect locDialect = (ExtendedDialect) this.dialect;
		final Boolean[] hasTable = new Boolean[1];
		session.doWork(new Work()
		{

			@Override
			public void execute(Connection connection) throws SQLException
			{
				ResultSet tables = connection.getMetaData().getTables(null, defaultSchema,
					locDialect.getNameForMetadataQuery(table, false), new String[]{"TABLE"});
				try
				{
					hasTable[0] = tables.next();
				}
				finally
				{
					tables.close();
				}
			}
		});
		return hasTable[0];
	}

	public List<String> getDropColumnSQL(String tableName, String... columns)
	{
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		for( String columnName : columns )
		{
			Column column = table.getColumn(new Column(columnName));
			if( column == null )
			{
				throw new RuntimeException("Could not find column " + columnName + " on table " + tableName);
			}
			sqlStrings.add(extDialect.getDropColumnSql(table.getQualifiedName(dialect, defaultCatalog, defaultSchema),
				column));
		}
		return sqlStrings;
	}

	public List<String> getAddNotNullSQL(String tableName, String... columns)
	{
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);
		for( String columnName : columns )
		{
			Column column = table.getColumn(new Column(columnName));

			StringBuffer alter = new StringBuffer("alter table ")
				.append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(' ')
				.append(extDialect.getAddNotNullSql(mapping, column));
			sqlStrings.add(alter.toString());
		}
		return sqlStrings;
	}

	public List<String> getRenameColumnSQL(String tableName, String columnName, String newColumnName)
	{
		List<String> sqlStrings = new ArrayList<String>();

		Table table = findTable(tableName);

		Column column = table.getColumn(new Column(columnName));
		String alter = null;
		alter = extDialect.getRenameColumnSql(table.getQualifiedName(dialect, defaultCatalog, defaultSchema), column,
			newColumnName);
		sqlStrings.add(alter);

		return sqlStrings;
	}

	public String getQuotedIdentifier(String ident)
	{
		return dialect.openQuote() + ident + dialect.closeQuote();
	}

	public String getDefaultCatalog()
	{
		return defaultCatalog;
	}

	public String getDefaultSchema()
	{
		return defaultSchema;
	}

	public SessionFactoryImpl getFactory()
	{
		return factory;
	}

	public ExtendedDialect getExtDialect()
	{
		return extDialect;
	}

	public List<String> getAddNotNullSQLIfRequired(Session session, String tableName, String... columns)
	{
		final Table table = findTable(tableName);

		final List<String> sqlStrings = new ArrayList<String>();
		final Set<String> colset = new HashSet<String>(Arrays.asList(columns));
		session.doWork(new Work()
		{
			@Override
			public void execute(Connection connection) throws SQLException
			{
				ResultSet colresult = connection.getMetaData().getColumns(getDefaultCatalog(),
					extDialect.getNameForMetadataQuery(getDefaultSchema(), false),
					extDialect.getNameForMetadataQuery(table.getName(), table.isQuoted()), "%");
				try
				{
					while( colresult.next() )
					{
						String columnName = colresult.getString("COLUMN_NAME").toLowerCase();
						if( colset.contains(columnName)
							&& "yes".equals(colresult.getString("IS_NULLABLE").toLowerCase()) )
						{
							Column column = table.getColumn(new Column(columnName));

							StringBuffer alter = new StringBuffer("alter table ")
								.append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(' ')
								.append(extDialect.getAddNotNullSql(mapping, column));
							sqlStrings.add(alter.toString());
						}
					}
				}
				finally
				{
					colresult.close();
				}
			}
		});
		return sqlStrings;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends String> getAddIndexesIfRequired(Session session, String tableName, String... indexes)
	{
		final Table table = findTable(tableName);

		List<String> sqlStrings = new ArrayList<String>();
		Map<Set<String>, String> revIndexMap = getExistingIndexes(table, session);
		Set<String> indexSet = new HashSet<String>(Arrays.asList(indexes));
		Iterator<Index> indexIterator = table.getIndexIterator();
		while( indexIterator.hasNext() )
		{
			Index index = indexIterator.next();
			if( !indexSet.remove(index.getName()) )
			{
				continue;
			}
			processIndex(table, index, revIndexMap, sqlStrings);
		}
		if( !indexSet.isEmpty() )
		{
			throw new RuntimeException("Failed to find indexes:" + indexSet + " on table: " + tableName);
		}
		return sqlStrings;
	}

	public Collection<? extends String> getAddIndexesRawIfRequired(Session session, String tableName, String indexName,
		String indexColumn)
	{
		return getAddIndexesRawIfRequired(session, tableName, new String[]{indexName, indexColumn});
	}

	public Collection<? extends String> getAddIndexesRawIfRequired(Session session, String tableName,
		String[]... indexes)
	{
		List<String> sqlStrings = new ArrayList<String>();
		final Table table = findTable(tableName);
		Map<Set<String>, String> revIndexMap = getExistingIndexes(table, session);
		for( String[] index : indexes )
		{
			Index indexObj = new Index();
			indexObj.setTable(table);
			indexObj.setName(index[0]);
			for( int i = 1; i < index.length; i++ )
			{
				Column col = new Column(index[i]);
				indexObj.addColumn(col);
			}
			processIndex(table, indexObj, revIndexMap, sqlStrings);
		}
		return sqlStrings;
	}

	public Collection<? extends String> getAddIndexesRaw(String tableName, String indexeName, String indexColumn)
	{
		return getAddIndexesRaw(tableName, new String[]{indexeName, indexColumn});
	}

	public Collection<? extends String> getAddIndexesRaw(String tableName, String[]... indexes)
	{
		List<String> sqlStrings = new ArrayList<String>();
		final Table table = findTable(tableName);
		for( String[] index : indexes )
		{
			Index indexObj = new Index();
			indexObj.setTable(table);
			indexObj.setName(index[0]);
			for( int i = 1; i < index.length; i++ )
			{
				Column col = new Column(index[i]);
				indexObj.addColumn(col);
			}
			sqlStrings.add(indexObj.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
		}
		return sqlStrings;
	}

	/**
	 * Variation on getAddIndexesRaw, whereby we pass it to the ExtendedDialect
	 * subclass(es) to custom knit a function call around all the column names,
	 * hence for example instead of indexing TLEUser.username, we want to index
	 * lower(TLEUser.username).
	 * 
	 * @param tableName
	 * @param indexes
	 * @return
	 */
	public Collection<? extends String> getAddFunctionIndexes(String tableName, String function, String[]... indexes)
	{
		List<String> sqlStrings = new ArrayList<String>();

		for( String[] indexableColumns : indexes )
		{
			sqlStrings.addAll(extDialect.getCreateFunctionalIndex(tableName, function, indexableColumns));
		}
		return sqlStrings;
	}

	@SuppressWarnings("unchecked")
	private void processIndex(Table table, Index index, Map<Set<String>, String> revIndexMap, List<String> sqlStrings)
	{
		Iterator<Column> colIter = index.getColumnIterator();
		Set<String> indexCols = new HashSet<String>();
		while( colIter.hasNext() )
		{
			Column col = colIter.next();
			indexCols.add(col.getName().toLowerCase());
		}
		String existingIndex = revIndexMap.get(indexCols);
		if( existingIndex != null )
		{
			if( existingIndex.equalsIgnoreCase(index.getName()) )
			{
				return;
			}
			else
			{
				sqlStrings.add(extDialect.getDropIndexSql(table.getName(), '`' + existingIndex + '`'));
			}
		}
		sqlStrings.add(index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
	}

	public Map<Set<String>, String> getExistingIndexes(final Table table, Session session)
	{
		final Map<String, Set<String>> indexMap = new HashMap<String, Set<String>>();

		// Query existing index
		session.doWork(new Work()
		{
			@Override
			public void execute(Connection connection) throws SQLException
			{
				ResultSet colresult = connection.getMetaData().getIndexInfo(getDefaultCatalog(),
					extDialect.getNameForMetadataQuery(getDefaultSchema(), false),
					extDialect.getNameForMetadataQuery(table.getName(), table.isQuoted()), false, false);
				try
				{
					while( colresult.next() )
					{
						String indexName = colresult.getString("INDEX_NAME");
						String columnName = colresult.getString("COLUMN_NAME");
						if( columnName != null )
						{
							columnName = columnName.toLowerCase();
							Set<String> cols = indexMap.get(indexName);
							if( cols == null )
							{
								cols = new HashSet<String>();
								indexMap.put(indexName, cols);
							}
							cols.add(columnName);
						}
					}
				}
				finally
				{
					colresult.close();
				}
			}
		});
		// Change from (index -> cols) to (cols -> index)
		Map<Set<String>, String> revIndexMap = new HashMap<Set<String>, String>();
		for( Map.Entry<String, Set<String>> entry : indexMap.entrySet() )
		{
			revIndexMap.put(entry.getValue(), entry.getKey());
		}
		return revIndexMap;
	}

	private Table findTable(String tableName)
	{
		Map<String, Table> tableMap = configuration.getTableMap();
		Table table = tableMap.get(tableName);
		if( table == null )
		{
			throw new RuntimeException("Failed to find table: " + tableName);
		}
		return table;
	}

	public List<String> getDropConstraintsSQL(String table, String... columnNames)
	{
		List<String> sql = new ArrayList<String>();
		for( String columnName : columnNames )
		{
			sql.add(extDialect.getDropConstraintsForColumnSql(table, columnName));
		}
		return sql;
	}
}
