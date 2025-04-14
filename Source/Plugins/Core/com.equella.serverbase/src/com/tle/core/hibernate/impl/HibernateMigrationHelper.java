/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.core.hibernate.ExtendedAnnotationConfiguration;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.HibernateFactory;
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
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.Sequence;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Work;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;
import org.hibernate.tool.schema.internal.StandardSequenceExporter;

@SuppressWarnings("nls")
public class HibernateMigrationHelper {
  private final Dialect dialect;
  private final ExtendedDialect extDialect;
  private final String defaultCatalog;
  private final String defaultSchema;
  private final Mapping mapping;
  private final ExtendedAnnotationConfiguration configuration;
  private final SessionFactory factory;

  private SqlStringGenerationContext sqlStringGenerationContext;
  private static final Log LOGGER = LogFactory.getLog(HibernateMigrationHelper.class);

  public HibernateMigrationHelper(HibernateFactory factory) {
    this(factory, factory.getDefaultSchema());
  }

  public HibernateMigrationHelper(HibernateFactory factory, String defaultSchema) {
    this.factory = factory.getSessionFactory();
    dialect = ((SessionFactoryImplementor) this.factory).getJdbcServices().getDialect();
    extDialect = (ExtendedDialect) dialect;
    this.configuration = factory.getConfiguration();
    mapping = configuration.buildMapping();
    this.defaultSchema = defaultSchema;
    defaultCatalog = null;
    sqlStringGenerationContext =
        ((SessionFactoryImplementor) this.factory).getSqlStringGenerationContext();
  }

  public List<String> getDropTableSql(String... tables) {
    List<String> drops = new ArrayList<String>();
    for (String tableName : tables) {
      Table table = findTable(tableName);
      drops.add(table.sqlDropString(dialect, defaultCatalog, defaultSchema));
    }
    return drops;
  }

  public List<String> getCreationSql(HibernateCreationFilter filter) {
    return getCreationSql(filter, false);
  }

  @SuppressWarnings("unchecked")
  public List<String> getCreationSql(HibernateCreationFilter filter, Boolean sysTables) {
    List<String> sqlStrings = new ArrayList<String>();
    Map<String, Table> tables =
        sysTables ? configuration.getSysTableMap() : configuration.getNormalTableMap();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Tables registered:" + tables.keySet());
    }

    sqlStrings.addAll(getCreationSqlForTables(filter, tables.values()));

    sqlStrings.addAll(getCreationSqlForTableIndexAndFks(filter, tables.values()));

    sqlStrings.addAll(getCreationSqlForAuxDbos(filter));

    return sqlStrings;
  }

  private List<String> getCreationSqlForTables(
      HibernateCreationFilter filter, Collection<Table> tables) {
    List<String> sqlStrings = new ArrayList<>();

    for (Table table : tables) {
      if (table.isPhysicalTable() && filter.includeTable(table)) {
        final String sql =
            table.sqlCreateString(
                mapping, sqlStringGenerationContext, defaultCatalog, defaultSchema);
        LOGGER.debug("Table create SQL: " + sql);
        sqlStrings.add(sql);
      }
    }

    return sqlStrings;
  }

  private List<String> getCreationSqlForTableIndexAndFks(
      HibernateCreationFilter filter, Collection<Table> tables) {
    List<String> sqlStrings = new ArrayList<String>();

    for (Table table : tables) {
      if (table.isPhysicalTable()) {

        Iterator<Index> subIter = table.getIndexIterator();
        while (subIter.hasNext()) {
          Index index = subIter.next();
          final boolean addIndex =
              filter.includeIndex(table, index)
                  && (!extDialect.supportsAutoIndexForUniqueColumn()
                      || !hasUniqueIndex(index, table));
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Index to review: " + index.toString());
            LOGGER.debug("Filter.includeIndex? " + filter.includeIndex(table, index));
            LOGGER.debug(
                "Dialect ["
                    + extDialect.getClass().getName()
                    + "] - supportsAutoIndexForUniqueColumn? "
                    + extDialect.supportsAutoIndexForUniqueColumn());
            LOGGER.debug("HasUniqueIndex? " + hasUniqueIndex(index, table));
            LOGGER.debug("Should index be added? " + addIndex);
          }
          if (addIndex) {
            final String sql =
                index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
            LOGGER.debug("Index create SQL: " + sql);
            sqlStrings.add(sql);
          }
        }

        if (dialect.hasAlterTable()) {
          Iterator<ForeignKey> forIter = table.getForeignKeyIterator();
          while (forIter.hasNext()) {
            ForeignKey fk = forIter.next();
            if (LOGGER.isDebugEnabled()) {
              LOGGER.debug("Reviewing FK for creation SQL: " + fk.getName());
              LOGGER.debug("Is FK a physical constraint? " + fk.isPhysicalConstraint());
              LOGGER.debug(
                  "Should the FK be included for table ["
                      + table.getName()
                      + "]? "
                      + filter.includeForeignKey(table, fk));
            }
            if (fk.isPhysicalConstraint() && filter.includeForeignKey(table, fk)) {
              final String sql =
                  fk.sqlCreateString(
                      mapping, sqlStringGenerationContext, defaultCatalog, defaultSchema);
              LOGGER.debug("FK create SQL: " + sql);
              sqlStrings.add(sql);
            }
          }
        } else {
          LOGGER.debug("Dialect does not support ALTER TABLE.");
        }
      } else {
        LOGGER.debug("Table is not physical, not generating SQL for it: " + table.getName());
      }
    }

    return sqlStrings;
  }

  private List<String> getCreationSqlForAuxDbos(HibernateCreationFilter filter) {
    List<String> sqlStrings = new ArrayList<String>();

    for (AuxiliaryDatabaseObject object : configuration.getAuxiliaryDatabaseObjects()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Reviewing Aux DB Obj for creation SQL: " + object.getExportIdentifier());
        LOGGER.debug("Does Aux DB Obj apply to dialect? " + object.appliesToDialect(dialect));
        LOGGER.debug("Should the Aux DB Obj be included? " + filter.includeObject(object));
      }
      if (object.appliesToDialect(dialect) && filter.includeObject(object)) {
        // Due to SpringHib5, removed the extra parameters and switched packages.
        // DDL for Oracle, Postgres, and Sql Server were comparable.
        final List<String> lines =
            Lists.newArrayList(object.sqlCreateStrings(sqlStringGenerationContext));
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(
              "Filter includes Aux DB object ["
                  + object.getExportIdentifier()
                  + "].  Adding SQL lines: "
                  + Arrays.toString(lines.toArray()));
        }
        sqlStrings.addAll(lines);
      }
    }

    return sqlStrings;
  }

  /**
   * Generate SQL statements for creating a sequence in the default DB Schema.
   *
   * @param sequenceName Name of the sequence to be created.
   */
  public List<String> getSqlCreateStringForSequence(String sequenceName) {
    Sequence sequence =
        new Sequence(
            Identifier.toIdentifier(defaultCatalog),
            Identifier.toIdentifier(defaultSchema),
            Identifier.toIdentifier(sequenceName));

    StandardSequenceExporter exporter = new StandardSequenceExporter(dialect);

    return Arrays.asList(
        exporter.getSqlCreateStrings(
            sequence, configuration.getMetadata(), sqlStringGenerationContext));
  }

  public List<String> getAddColumnsSQL(String tableName, String... columnNames) {
    List<String> sqlStrings = new ArrayList<>();

    Table table = findTable(tableName);
    for (String columnName : columnNames) {
      Column column = table.getColumn(new Column(columnName));

      StringBuilder alter =
          new StringBuilder("alter table ")
              .append(table.getQualifiedName(sqlStringGenerationContext))
              .append(' ')
              .append(dialect.getAddColumnString());
      alter
          .append(' ')
          .append(column.getQuotedName(dialect))
          .append(' ')
          .append(column.getSqlType(dialect, mapping));

      boolean useUniqueConstraint =
          extDialect.supportsModifyWithConstraints()
              && column.isUnique()
              && dialect.supportsUnique()
              && (!column.isNullable() || dialect.supportsNotNullUnique());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "Considering if a unique constraint should be used.  "
                + "DialectSupportsModifyWithConst=["
                + extDialect.supportsModifyWithConstraints()
                + "], "
                + "IsColumnUnique=["
                + column.isUnique()
                + "], "
                + "DialectSupportsUnique=["
                + dialect.supportsUnique()
                + "], "
                + "ColumnNullable=["
                + column.isNullable()
                + "], "
                + "DialectSupportsNotNullUnique=["
                + dialect.supportsNotNullUnique()
                + "] "
                + "- useUniqueConstraint=["
                + useUniqueConstraint
                + "]");
      }
      if (useUniqueConstraint) {
        LOGGER.debug("Using unique constraint on " + tableName + " - " + columnName);
        alter.append(" unique");
      }
      if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
        LOGGER.debug("Using check constraint for uniqueness on " + tableName + " - " + columnName);
        alter.append(" check(").append(column.getCheckConstraint()).append(")");
      }

      String columnComment = column.getComment();
      if (columnComment != null) {
        alter.append(dialect.getColumnComment(columnComment));
      }
      sqlStrings.add(alter.toString());
    }
    return sqlStrings;
  }

  /**
   * By default we assume that foreign key constraints will be added for the columnNames
   *
   * @param tableName
   * @param columnNames
   * @return
   */
  public List<String> getAddIndexesAndConstraintsForColumns(
      String tableName, String... columnNames) {
    return getAddIndexesAndConstraintsForColumns(tableName, true, columnNames);
  }

  @SuppressWarnings({"unchecked"})
  public List<String> getAddIndexesAndConstraintsForColumns(
      String tableName, boolean includeForeignKeyConstraints, String... columnNames) {
    Set<Column> colSet = new HashSet<Column>();
    for (String columnName : columnNames) {
      colSet.add(new Column(columnName));
    }
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Setting up indexes and constraints on columns for table:" + tableName);
    }
    if (!extDialect.supportsModifyWithConstraints()) {
      for (Column col : colSet) {
        Column realCol = table.getColumn(col);
        if (realCol.isUnique()) {
          LOGGER.debug("Creating unique key for column: " + realCol.getName());
          table.createUniqueKey(Collections.singletonList(realCol));
        }
      }
    } else {
      LOGGER.debug("Dialect does not support modification with constraints");
    }
    Iterator<UniqueKey> keyIter = table.getUniqueKeyIterator();
    if (dialect.supportsUniqueConstraintInCreateAlterTable()) {
      while (keyIter.hasNext()) {
        UniqueKey uk = keyIter.next();
        LOGGER.debug("Considering unique key for table [" + tableName + "]: " + uk.getName());
        if (!Collections.disjoint(uk.getColumns(), colSet)) {
          StringBuilder buf = new StringBuilder("alter table ");
          buf.append(table.getQualifiedName(sqlStringGenerationContext));
          buf.append(" add constraint ");
          String constraint =
              uk.sqlConstraintString(
                  sqlStringGenerationContext,
                  extDialect.getRandomIdentifier(),
                  defaultCatalog,
                  defaultSchema);
          LOGGER.debug(
              "Adding unique alter constraint to table [" + tableName + "]: " + constraint);
          if (constraint != null) {
            buf.append(constraint);
            sqlStrings.add(buf.toString());
          }
        }
      }
    } else {
      while (keyIter.hasNext()) {
        UniqueKey ukey = keyIter.next();
        if (!Collections.disjoint(ukey.getColumns(), colSet)) {
          final String sql = ukey.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
          LOGGER.debug("Adding unique constraint to table [" + tableName + "]: " + sql);
          sqlStrings.add(sql);
        }
      }
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Index create SQL: " + Arrays.toString(sqlStrings.toArray()));
    }
    addIndexSQL(sqlStrings, table, colSet);

    // Caller may opt to skip foreign key constraints
    if (includeForeignKeyConstraints) {
      Iterator<ForeignKey> fkeyIter = table.getForeignKeyIterator();
      while (fkeyIter.hasNext()) {
        ForeignKey fkey = fkeyIter.next();
        if (!Collections.disjoint(fkey.getColumns(), colSet)) {
          final String sql = fkey.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
          LOGGER.debug("FK create SQL: " + sql);
          sqlStrings.add(sql);
        }
      }
    } else {
      LOGGER.debug("Skipping FKs for table: " + tableName);
    }

    return sqlStrings;
  }

  public List<String> getAddIndexesForColumns(String tableName, String... columnNames) {
    Set<Column> colSet = new HashSet<Column>();
    for (String columnName : columnNames) {
      colSet.add(new Column(columnName));
    }
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);
    addIndexSQL(sqlStrings, table, colSet);
    return sqlStrings;
  }

  @SuppressWarnings({"unchecked"})
  private void addIndexSQL(List<String> sqlStrings, Table table, Set<Column> colSet) {
    Iterator<Index> indexIterator = table.getIndexIterator();
    while (indexIterator.hasNext()) {
      Index index = indexIterator.next();
      Iterator<Column> colIter = index.getColumnIterator();
      boolean found = false;
      while (colIter.hasNext()) {
        Column col = colIter.next();
        if (colSet.contains(col)) {
          found = true;
          break;
        }
      }
      if (found
          && (!extDialect.supportsAutoIndexForUniqueColumn() || !hasUniqueIndex(index, table))) {
        final String sql = index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
        LOGGER.debug("Index create SQL: " + sql);
        sqlStrings.add(sql);
      } else {
        LOGGER.debug(
            "NOT adding index create SQL on table ["
                + table.getName()
                + "] for: "
                + index.getName());
      }
    }
  }

  public String getAddNamedIndex(String tableName, String indexName, String... columnNames) {
    Index index = new Index();
    index.setName(indexName);
    index.setTable(findTable(tableName));
    for (String columnName : columnNames) {
      index.addColumn(new Column(columnName));
    }
    return index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
  }

  public String getDropNamedIndex(String tableName, String indexName) {
    return extDialect.getDropIndexSql(tableName, indexName);
  }

  @SuppressWarnings("unchecked")
  private boolean hasUniqueIndex(Index index, Table table) {
    HashSet<Column> indexCols = new HashSet<Column>();
    Iterator<Column> icolIter = index.getColumnIterator();
    while (icolIter.hasNext()) {
      Column col = icolIter.next();
      indexCols.add(col);
      if (index.getColumnSpan() == 1 && table.getColumn(col).isUnique()) {
        return true;
      }
    }
    Iterator<UniqueKey> iter = table.getUniqueKeyIterator();
    while (iter.hasNext()) {
      UniqueKey uk = iter.next();
      if (uk.getColumnSpan() == indexCols.size() && indexCols.containsAll(uk.getColumns())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param tableName The name of the table to modify.
   * @param columnName The name of the column to modify.
   * @param changeNotNull `True` if the column should be changed to `NOT NULL`.
   * @param changeType `True` if the column type should be changed. nullable Pass in null to use the
   *     annotation on the column. This is not always possible (see Redmine #3329).
   * @return A list of SQL strings to modify the column.
   */
  public List<String> getModifyColumnSQL(
      String tableName, String columnName, boolean changeNotNull, boolean changeType) {
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);
    Column column = table.getColumn(new Column(columnName));

    String alter =
        "alter table "
            + table.getQualifiedName(sqlStringGenerationContext)
            + ' '
            + extDialect.getModifyColumnSql(mapping, column, changeNotNull, changeType);

    sqlStrings.add(alter);
    return sqlStrings;
  }

  public boolean tableExists(Session session, final String table) {
    final ExtendedDialect locDialect = (ExtendedDialect) this.dialect;
    final Boolean[] hasTable = new Boolean[1];
    session.doWork(
        new Work() {

          @Override
          public void execute(Connection connection) throws SQLException {
            ResultSet tables =
                connection
                    .getMetaData()
                    .getTables(
                        null,
                        defaultSchema,
                        locDialect.getNameForMetadataQuery(table, false),
                        new String[] {"TABLE"});
            try {
              hasTable[0] = tables.next();
            } finally {
              tables.close();
            }
          }
        });
    return hasTable[0];
  }

  public List<String> getDropColumnSQL(String tableName, String... columns) {
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);
    for (String columnName : columns) {
      Column column = table.getColumn(new Column(columnName));
      if (column == null) {
        throw new RuntimeException(
            "Could not find column " + columnName + " on table " + tableName);
      }
      sqlStrings.add(
          extDialect.getDropColumnSql(table.getQualifiedName(sqlStringGenerationContext), column));
    }
    return sqlStrings;
  }

  public List<String> getAddNotNullSQL(String tableName, String... columns) {
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);
    for (String columnName : columns) {
      Column column = table.getColumn(new Column(columnName));

      StringBuffer alter =
          new StringBuffer("alter table ")
              .append(table.getQualifiedName(sqlStringGenerationContext))
              .append(' ')
              .append(extDialect.getAddNotNullSql(mapping, column));
      sqlStrings.add(alter.toString());
    }
    return sqlStrings;
  }

  public List<String> getRenameColumnSQL(
      String tableName, String columnName, String newColumnName) {
    List<String> sqlStrings = new ArrayList<String>();

    Table table = findTable(tableName);

    Column column = table.getColumn(new Column(columnName));
    String alter = null;
    alter =
        extDialect.getRenameColumnSql(
            table.getQualifiedName(sqlStringGenerationContext), column, newColumnName);
    sqlStrings.add(alter);

    return sqlStrings;
  }

  public String getRenameTableSQL(String tableName, String newTableName) {
    Table table = findTable(tableName);
    return extDialect.getRenameTableSql(
        table.getQualifiedName(sqlStringGenerationContext), newTableName);
  }

  public String getRenameIndexSQL(String tableName, String index, String newIndexName) {
    Table table = findTable(tableName);
    return extDialect.getRenameIndexSql(
        table.getQualifiedName(sqlStringGenerationContext), index, newIndexName);
  }

  public String getQuotedIdentifier(String ident) {
    return dialect.openQuote() + ident + dialect.closeQuote();
  }

  public String getDefaultCatalog() {
    return defaultCatalog;
  }

  public String getDefaultSchema() {
    return defaultSchema;
  }

  public SessionFactory getFactory() {
    return factory;
  }

  public ExtendedDialect getExtDialect() {
    return extDialect;
  }

  public List<String> getAddNotNullSQLIfRequired(
      Session session, String tableName, String... columns) {
    final Table table = findTable(tableName);

    final List<String> sqlStrings = new ArrayList<String>();
    final Set<String> colset = new HashSet<String>(Arrays.asList(columns));
    session.doWork(
        new Work() {
          @Override
          public void execute(Connection connection) throws SQLException {
            ResultSet colresult =
                connection
                    .getMetaData()
                    .getColumns(
                        getDefaultCatalog(),
                        extDialect.getNameForMetadataQuery(getDefaultSchema(), false),
                        extDialect.getNameForMetadataQuery(table.getName(), table.isQuoted()),
                        "%");
            try {
              while (colresult.next()) {
                String columnName = colresult.getString("COLUMN_NAME").toLowerCase();
                if (colset.contains(columnName)
                    && "yes".equals(colresult.getString("IS_NULLABLE").toLowerCase())) {
                  Column column = table.getColumn(new Column(columnName));

                  StringBuffer alter =
                      new StringBuffer("alter table ")
                          .append(table.getQualifiedName(sqlStringGenerationContext))
                          .append(' ')
                          .append(extDialect.getAddNotNullSql(mapping, column));
                  sqlStrings.add(alter.toString());
                }
              }
            } finally {
              colresult.close();
            }
          }
        });
    return sqlStrings;
  }

  @SuppressWarnings("unchecked")
  public Collection<? extends String> getAddIndexesIfRequired(
      Session session, String tableName, String... indexes) {
    final Table table = findTable(tableName);

    List<String> sqlStrings = new ArrayList<String>();
    Map<Set<String>, String> revIndexMap = getExistingIndexes(table, session);
    Set<String> indexSet = new HashSet<String>(Arrays.asList(indexes));
    Iterator<Index> indexIterator = table.getIndexIterator();
    while (indexIterator.hasNext()) {
      Index index = indexIterator.next();
      if (!indexSet.remove(index.getName())) {
        continue;
      }
      processIndex(table, index, revIndexMap, sqlStrings);
    }
    if (!indexSet.isEmpty()) {
      throw new RuntimeException("Failed to find indexes:" + indexSet + " on table: " + tableName);
    }
    return sqlStrings;
  }

  public Collection<? extends String> getAddIndexesRawIfRequired(
      Session session, String tableName, String indexName, String indexColumn) {
    return getAddIndexesRawIfRequired(session, tableName, new String[] {indexName, indexColumn});
  }

  public Collection<? extends String> getAddIndexesRawIfRequired(
      Session session, String tableName, String[]... indexes) {
    List<String> sqlStrings = new ArrayList<String>();
    final Table table = findTable(tableName);
    Map<Set<String>, String> revIndexMap = getExistingIndexes(table, session);
    for (String[] index : indexes) {
      Index indexObj = new Index();
      indexObj.setTable(table);
      indexObj.setName(index[0]);
      for (int i = 1; i < index.length; i++) {
        Column col = new Column(index[i]);
        indexObj.addColumn(col);
      }
      processIndex(table, indexObj, revIndexMap, sqlStrings);
    }
    return sqlStrings;
  }

  public Collection<? extends String> getAddIndexesRaw(
      String tableName, String indexeName, String indexColumn) {
    return getAddIndexesRaw(tableName, new String[] {indexeName, indexColumn});
  }

  public Collection<? extends String> getAddIndexesRaw(String tableName, String[]... indexes) {
    List<String> sqlStrings = new ArrayList<String>();
    final Table table = findTable(tableName);
    for (String[] index : indexes) {
      Index indexObj = new Index();
      indexObj.setTable(table);
      indexObj.setName(index[0]);
      for (int i = 1; i < index.length; i++) {
        Column col = new Column(index[i]);
        indexObj.addColumn(col);
      }
      sqlStrings.add(indexObj.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema));
    }
    return sqlStrings;
  }

  /**
   * Variation on getAddIndexesRaw, whereby we pass it to the ExtendedDialect subclass(es) to custom
   * knit a function call around all the column names, hence for example instead of indexing
   * TLEUser.username, we want to index lower(TLEUser.username).
   *
   * @param tableName
   * @param indexes
   * @return
   */
  public Collection<? extends String> getAddFunctionIndexes(
      String tableName, String function, String[]... indexes) {
    List<String> sqlStrings = new ArrayList<String>();

    for (String[] indexableColumns : indexes) {
      sqlStrings.addAll(extDialect.getCreateFunctionalIndex(tableName, function, indexableColumns));
    }
    return sqlStrings;
  }

  @SuppressWarnings("unchecked")
  private void processIndex(
      Table table, Index index, Map<Set<String>, String> revIndexMap, List<String> sqlStrings) {
    Iterator<Column> colIter = index.getColumnIterator();
    Set<String> indexCols = new HashSet<String>();
    while (colIter.hasNext()) {
      Column col = colIter.next();
      indexCols.add(col.getName().toLowerCase());
    }
    String existingIndex = revIndexMap.get(indexCols);
    if (existingIndex != null) {
      if (existingIndex.equalsIgnoreCase(index.getName())) {
        LOGGER.debug("Index [" + index.getName() + "] exists.  returning.");
        return;
      } else {
        final String sql = extDialect.getDropIndexSql(table.getName(), '`' + existingIndex + '`');
        LOGGER.debug("Index does not exist.  Dropping index [" + index.getName() + "]");
        sqlStrings.add(sql);
      }
    }
    final String sql = index.sqlCreateString(dialect, mapping, defaultCatalog, defaultSchema);
    LOGGER.debug("Existing index is null.  Creating index SQL: " + sql);
    sqlStrings.add(sql);
  }

  public Map<Set<String>, String> getExistingIndexes(final Table table, Session session) {
    final Map<String, Set<String>> indexMap = new HashMap<String, Set<String>>();

    // Query existing index
    session.doWork(
        new Work() {
          @Override
          public void execute(Connection connection) throws SQLException {
            ResultSet colresult =
                connection
                    .getMetaData()
                    .getIndexInfo(
                        getDefaultCatalog(),
                        extDialect.getNameForMetadataQuery(getDefaultSchema(), false),
                        extDialect.getNameForMetadataQuery(table.getName(), table.isQuoted()),
                        false,
                        false);
            try {
              while (colresult.next()) {
                String indexName = colresult.getString("INDEX_NAME");
                String columnName = colresult.getString("COLUMN_NAME");
                if (columnName != null) {
                  columnName = columnName.toLowerCase();
                  Set<String> cols = indexMap.get(indexName);
                  if (cols == null) {
                    cols = new HashSet<String>();
                    indexMap.put(indexName, cols);
                  }
                  cols.add(columnName);
                }
              }
            } finally {
              colresult.close();
            }
          }
        });
    // Change from (index -> cols) to (cols -> index)
    Map<Set<String>, String> revIndexMap = new HashMap<Set<String>, String>();
    for (Map.Entry<String, Set<String>> entry : indexMap.entrySet()) {
      revIndexMap.put(entry.getValue(), entry.getKey());
    }
    return revIndexMap;
  }

  private Table findTable(String tableName) {
    Map<String, Table> tableMap = configuration.getNormalTableMap();
    Table table = tableMap.get(tableName);
    if (table == null) {
      throw new RuntimeException("Failed to find table: " + tableName);
    }
    return table;
  }

  public List<String> getDropConstraintsSQL(String table, String... columnNames) {
    List<String> sql = new ArrayList<String>();
    for (String columnName : columnNames) {
      sql.add(extDialect.getDropConstraintsForColumnSql(table, columnName));
    }
    return sql;
  }

  /**
   * Update the column. <br>
   * The difference between this method and {@link #getModifyColumnSQL} is this method will create a
   * new column to inherit the data from the original column, then drop the original column, and
   * finally rename the temporary column to the original column. <br>
   * Examples of using this method:
   *
   * <ul>
   *   <li>Changing the data type of a column with existing data in Oracle</>
   *   <li>Dropping a constraint for a column however the constraint name is unknown</>
   * </ul>
   *
   * @param tableName The name of the table.
   * @param columnName The name of the column to change.
   * @param tempColumnName The name of the temporary column to create to hold the data.
   */
  public List<String> getUpdateColumnSQL(
      String tableName, String columnName, String tempColumnName) {
    // Create a temporary column.
    List<String> addTempColumnSQL = getAddColumnsSQL(tableName, tempColumnName);
    // Copy the data from the original column to the temporary column.
    String copyDataSQL = "UPDATE " + tableName + " SET " + tempColumnName + "=" + columnName;
    // Drop the original column.
    List<String> dropColumnSQL = getDropColumnSQL(tableName, columnName);
    // Rename the temporary column to the original column.
    List<String> renameColumnSQL = getRenameColumnSQL(tableName, tempColumnName, columnName);

    List<String> results = new ArrayList<>(addTempColumnSQL);
    results.add(copyDataSQL);
    results.addAll(dropColumnSQL);
    results.addAll(renameColumnSQL);

    return results;
  }
}
