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

package com.tle.core.qti.migration;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.Session;
import org.hibernate.annotations.Index;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddItemVariableValueIndex extends AbstractHibernateSchemaMigration {
  private static final String keyPrefix =
      PluginServiceImpl.getMyPluginId(AddItemVariableValueIndex.class)
          + ".migration.additemvariablevalueindex.";

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(keyPrefix + "title");
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) {
    // None
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 0;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return helper.getAddIndexesAndConstraintsForColumns(
        "qti_item_variable_value", "qti_item_variable_id");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class[] {FakeItemVariableValue.class};
  }

  @Entity(name = "QtiItemVariableValue")
  public static class FakeItemVariableValue {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Index(name = "itemValVarIdx")
    long qtiItemVariableId;
  }
}
