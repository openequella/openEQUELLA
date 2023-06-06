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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.unique.DefaultUniqueDelegate;
import org.hibernate.mapping.Table;

/**
 * Standard Hib 5.3+ does not include the unique constraints in the create table logic, so we need
 * to add our own.
 */
public class InPlaceUniqueDelegate extends DefaultUniqueDelegate {
  private static final Log LOGGER = LogFactory.getLog(InPlaceUniqueDelegate.class);

  public InPlaceUniqueDelegate(Dialect dialect) {
    super(dialect);
  }

  @Override
  public String getTableCreationUniqueConstraintsFragment(
      Table table, SqlStringGenerationContext context) {
    StringBuilder sb = new StringBuilder();

    table
        .getUniqueKeyIterator()
        .forEachRemaining(uniqueKey -> sb.append(", ").append(uniqueConstraintSql(uniqueKey)));

    final String sql = sb.toString();
    LOGGER.debug(
        "For table [" + table.getName() + "], generated the uniqueness constraint: " + sql);
    return sql;
  }
}
