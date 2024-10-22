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

import com.tle.core.hibernate.ExtendedDialect;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.hibernate.dialect.Oracle9iDialect;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.mapping.Column;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

public class ExtendedOracle9iDialect extends Oracle9iDialect implements ExtendedDialect {

  private final ExtendedOracle10gDialect tenG;

  public ExtendedOracle9iDialect() {
    tenG = new ExtendedOracle10gDialect();
    registerHibernateType(Types.CLOB, StandardBasicTypes.STRING.getName());
  }

  @Override
  public boolean canRollbackSchemaChanges() {
    return false;
  }

  @Override
  public String getModifyColumnSql(
      Mapping mapping, Column column, boolean changeNotNull, boolean changeType) {
    return tenG.getModifyColumnSql(mapping, column, changeNotNull, changeType);
  }

  @Override
  public String getDropColumnSql(String table, Column column) {
    return tenG.getDropColumnSql(table, column);
  }

  @Override
  public String getDropIndexSql(String table, String indexName) {
    return tenG.getDropIndexSql(table, indexName);
  }

  @Override
  public String getAddNotNullSql(Mapping mapping, Column column) {
    return tenG.getAddNotNullSql(mapping, column);
  }

  @Override
  public String getRenameColumnSql(String table, Column column, String name) {
    return tenG.getRenameColumnSql(table, column, name);
  }

  @Override
  public String getRenameTableSql(String table, String newName) {
    return tenG.getRenameTableSql(table, newName);
  }

  @Override
  public String getRenameIndexSql(String table, String indexName, String newName) {
    return tenG.getRenameIndexSql(table, indexName, newName);
  }

  @Override
  public boolean supportsAutoIndexForUniqueColumn() {
    return true;
  }

  @Override
  public boolean supportsModifyWithConstraints() {
    return true;
  }

  @Override
  public String getNameForMetadataQuery(String name, boolean quoted) {
    return tenG.getNameForMetadataQuery(name, quoted);
  }

  @Override
  public boolean requiresAliasOnSubselect() {
    return false;
  }

  @Override
  public boolean requiresNoConstraintsForModify() {
    return false;
  }

  @Override
  public String getDropConstraintsForColumnSql(String table, String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getRandomIdentifier() {
    return tenG.getRandomIdentifier();
  }

  @Override
  public String getDefaultSchema(Connection connection) throws SQLException {
    return tenG.getDefaultSchema(connection);
  }

  @Override
  public String getDisplayNameForUrl(String url) {
    return tenG.getDisplayNameForUrl(url);
  }

  @Override
  public Iterable<? extends BasicType> getExtraTypeOverrides() {
    return tenG.getExtraTypeOverrides();
  }

  @Override
  public List<String> getCreateFunctionalIndex(
      String tableName, String function, String[]... indexes) {
    return tenG.getCreateFunctionalIndex(tableName, function, indexes);
  }

  @Override
  public UniqueDelegate getUniqueDelegate() {
    return tenG.getUniqueDelegate();
  }
}
