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

import java.util.HashMap;
import java.util.Map;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;

// TODO [SpringHib5] - StringHelper is now internal and should not be used directly.

/**
 * Extends the PhysicalNamingStrategyStandardImpl to make sure that the resulting table name is all
 * lowercase. This helps with Enums on Postgresql.
 */
@SuppressWarnings("nls")
public class LowercasePhysicalNamingStrategy extends PhysicalNamingStrategyStandardImpl {
  private static final long serialVersionUID = 1L;

  private final Map<String, String> overrides = new HashMap<String, String>();
  private final Map<String, String> columnOverrides = new HashMap<String, String>();

  private enum Transform {
    COLUMN,
    TABLE,
    OTHER
  }

  public LowercasePhysicalNamingStrategy() {
    // MySQL5 can't handle schemas and `schemas` doesn't work
    // SQLServer can't handle schema
    registerOverride("schema", "tleschemas");
    registerOverride("comment", "`comment`");

    // SQL Server Specific
    columnOverrides.put("user", "`user`");
    columnOverrides.put("from", "`from`");
    columnOverrides.put("comment", "`comment`");
    columnOverrides.put("date", "`date`");
    columnOverrides.put("schema", "`schema`");
    columnOverrides.put("key", "`key`");
    columnOverrides.put("start", "`start`");
    columnOverrides.put("freetext", "`freetext`");
    columnOverrides.put("order", "`order`");
    columnOverrides.put("comment", "`comment`");
    columnOverrides.put("index", "`index`");

    // Oracle specific
    columnOverrides.put("successful", "`successful`");

    // HSQL Specific
    columnOverrides.put("position", "`position`");

    columnOverrides.put("timestamp", "`timestamp`");
  }

  private String getColumnName(String columnName) {
    if (columnName != null) {
      String col = columnOverrides.get(StringHelper.unqualify(columnName));
      if (col != null) {
        columnName = col;
      }
    }
    if (columnName != null && columnName.length() > 25) {
      columnName = columnName.substring(0, 25);
    }
    return columnName;
  }

  private String postProcess(String tableName) {
    tableName = tableName.toLowerCase();
    String result = overrides.get(tableName);
    if (result == null) {
      result = tableName;
    }
    if (result != null && result.length() > 30) {
      result = result.substring(0, 30);
    }
    return result;
  }

  private void registerOverride(String from, String to) {
    overrides.put(from.toLowerCase(), to.toLowerCase());
  }

  @Override
  public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return apply(name, Transform.OTHER);
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return apply(name, Transform.OTHER);
  }

  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return apply(name, Transform.TABLE);
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return apply(name, Transform.OTHER);
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return apply(name, Transform.COLUMN);
  }

  private Identifier apply(Identifier name, Transform transform) {
    if (name == null) {
      return null;
    }

    String resultantName = null;
    switch (transform) {
      case COLUMN:
        {
          resultantName = getColumnName(name.getText());
          break;
        }
      case TABLE:
        {
          resultantName = postProcess(name.getText());
          break;
        }
      case OTHER:
      default:
        {
          resultantName = name.getText();
          break;
        }
    }
    return new Identifier(resultantName, name.isQuoted());
  }
}
