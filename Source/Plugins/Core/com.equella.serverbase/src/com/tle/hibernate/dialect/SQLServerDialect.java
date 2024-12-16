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

package com.tle.hibernate.dialect;

import com.google.common.collect.ImmutableList;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.hibernate.type.HibernateCustomTypes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import org.hibernate.dialect.SQLServer2012Dialect;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

// TECH_DEBT - StringHelper is now internal - https://github.com/openequella/openEQUELLA/issues/2507

@SuppressWarnings("nls")
public class SQLServerDialect extends SQLServer2012Dialect implements ExtendedDialect {
  // General note on the queries in this class - With the advent of hibernate 5,
  // queries with '?' in them need to be ordinal ( ie `?4` ).  However, this class
  // does not leverage the JPA / Hibernate logic, so we can leave the `?`s as-is.

  private static final String URL_SCHEME = "jdbc:sqlserver://";
  private final String dropConstraintsSQL;

  private final UniqueDelegate uniqueDelegate;

  public SQLServerDialect() {
    uniqueDelegate = new InPlaceUniqueDelegate(this);

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
    try (InputStream inp = getClass().getResourceAsStream("dropconstraints.sql")) {
      // Sonar whinges about the new InputStreamReader not being closed in
      // a finally block, but the existing finally block does that ...?
      BufferedReader bufReader =
          new BufferedReader(new InputStreamReader(inp, StandardCharsets.UTF_8)); // NOSONAR
      String line = null;
      while ((line = bufReader.readLine()) != null) {
        sbuf.append(line);
        sbuf.append('\n');
      }
      dropConstraintsSQL = sbuf.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean canRollbackSchemaChanges() {
    return true;
  }

  @Override
  public String getModifyColumnSql(
      Mapping mapping, Column column, boolean changeNotNull, boolean changeType) {
    String columnName = column.getQuotedName(this);
    String nullStr = column.isNullable() ? " NULL" : " NOT NULL";
    String typeStr = ' ' + column.getSqlType(this, mapping);
    return "ALTER COLUMN " + columnName + typeStr + nullStr;
  }

  @Override
  public String getAddNotNullSql(Mapping mapping, Column column) {
    String columnName = column.getQuotedName(this);

    String typeStr = ' ' + column.getSqlType(this, mapping);
    return "ALTER COLUMN " + columnName + typeStr + " NOT NULL";
  }

  @Override
  public String getRenameColumnSql(String table, Column column, String name) {
    String columnName = column.getQuotedName(this);
    return "EXEC SP_RENAME '" + table + "." + columnName + "', '" + name + "', 'COLUMN'";
  }

  @Override
  public String getRenameTableSql(String table, String newName) {
    return "EXEC SP_RENAME '" + table + "', " + "'" + newName + "'";
  }

  @Override
  public String getRenameIndexSql(String table, String indexName, String newName) {
    return "EXEC SP_RENAME N'" + table + "." + indexName + "', N'" + newName + "', N'index'";
  }

  @Override
  public String getDropColumnSql(String table, Column column) {
    String dropSQL = dropConstraintsSQL.replaceAll("\\$table", Matcher.quoteReplacement(table));
    dropSQL = dropSQL.replaceAll("\\$column", Matcher.quoteReplacement(column.getName()));
    return dropSQL + "\nalter table " + table + " drop column " + column.getQuotedName(this);
  }

  @Override
  public String getDropConstraintsForColumnSql(String table, String columnName) {
    String dropSQL = dropConstraintsSQL.replaceAll("\\$table", Matcher.quoteReplacement(table));
    return dropSQL.replaceAll("\\$column", Matcher.quoteReplacement(columnName));
  }

  @Override
  public String getDropIndexSql(String table, String indexName) {
    return "drop index " + StringHelper.qualify(table, quote(indexName));
  }

  @Override
  public boolean supportsModifyWithConstraints() {
    return false;
  }

  @Override
  public boolean supportsAutoIndexForUniqueColumn() {
    return false;
  }

  @Override
  public String getNameForMetadataQuery(String name, boolean quoted) {
    return name;
  }

  @Override
  public boolean requiresNoConstraintsForModify() {
    return true;
  }

  @Override
  public boolean requiresAliasOnSubselect() {
    return true;
  }

  @Override
  public String getRandomIdentifier() {
    return openQuote() + UUID.randomUUID().toString() + closeQuote();
  }

  @Override
  public String getDefaultSchema(Connection connection) throws SQLException {
    String username = connection.getMetaData().getUserName();
    PreparedStatement userStatement =
        connection.prepareStatement(
            "SELECT dp.name AS login_name FROM sys.server_principals sp "
                + "JOIN sys.database_principals dp ON (sp.sid = dp.sid) WHERE sp.name = ?");

    userStatement.setString(1, username);
    ResultSet rs = userStatement.executeQuery();
    String dbUser;
    try {
      if (!rs.next()) {
        throw new SQLException("Can't find the default_schema for " + username);
      }
      dbUser = rs.getString(1);
    } finally {
      rs.close();
    }

    PreparedStatement statement =
        connection.prepareStatement(
            "select default_schema_name from sys.database_principals where name = ?");
    statement.setString(1, dbUser);
    ResultSet rs2 = statement.executeQuery();
    try {
      if (!rs2.next()) {
        throw new SQLException("Can't find the default_schema for " + username);
      }
      return rs2.getString(1);
    } finally {
      rs2.close();
    }
  }

  @Override
  public String getDisplayNameForUrl(String url) {
    if (url.startsWith(URL_SCHEME)) {
      return url.substring(URL_SCHEME.length());
    }
    return url;
  }

  @Override
  public Iterable<? extends BasicType> getExtraTypeOverrides() {
    List<BasicType> customTypes = new ArrayList<>(HibernateCustomTypes.getCustomTypes(this));
    customTypes.add(new NStringType());
    customTypes.add(new LongNStringType());
    return ImmutableList.copyOf(customTypes);
  }

  /**
   * We don't have the option to create an index on the lower_case value of a column (as we can in
   * Oracle or Postgres) without going the full distance of creating a computed column and then
   * indexing on that, hence.<br>
   * alter table <i>tableName</i> add <i>columnName_indexName</i> as
   * <i>function</i>(<i>columnName</i>);<br>
   * create index <i>indexName</i> on <i>tableName</i>(<i>columnName_indexName</i>); With sqlserver,
   * we're safe in adding extra characters to the column name, because maximum allowable length is
   * 128 characters, much longer then the lower length imposed on EQUELLA by virtue of Oracle (30)
   * or Postgres (63)
   */
  @Override
  public List<String> getCreateFunctionalIndex(
      String tableName, String function, String[]... indexes) {
    List<String> createIndexStatements = new ArrayList<String>();
    for (String[] indexAndColumns : indexes) {
      // 1st element in each indexAndColumns array in the indexes array is
      // the index name which must be followed by one or more column names
      if (indexAndColumns.length < 2) {
        String msg = "Cannot create functional Index on table " + tableName + ", ";
        msg +=
            indexAndColumns.length == 0
                ? " no index name or columns"
                : "no columns provided (for index name " + indexAndColumns[0] + ')';
        throw new RuntimeException(msg);
      } else {
        String indexName = indexAndColumns[0];
        for (int i = 1; i < indexAndColumns.length; ++i) {
          String alterTableStatement =
              "ALTER TABLE "
                  + tableName
                  + " ADD "
                  + indexName
                  + "_"
                  + indexName
                  + " AS "
                  + function
                  + '('
                  + indexAndColumns[i]
                  + ')';
          createIndexStatements.add(alterTableStatement);
          String createIndexStatement =
              "CREATE INDEX " + indexName + " ON " + tableName + '(' + indexAndColumns[i] + ')';
          createIndexStatements.add(createIndexStatement);
        }
      }
    }
    return createIndexStatements;
  }

  @Override
  public UniqueDelegate getUniqueDelegate() {
    return uniqueDelegate;
  }
}
