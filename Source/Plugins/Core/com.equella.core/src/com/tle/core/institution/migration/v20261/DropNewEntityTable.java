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

package com.tle.core.institution.migration.v20261;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.Session;
import org.hibernate.annotations.AttributeAccessor;

/** Migration to drop the 'entities' table that was created in v20191. */
@Bind
@Singleton
public class DropNewEntityTable extends AbstractHibernateSchemaMigration {

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20261.entity");
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    // No data migration required - this is a pure schema drop operation
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return helper.getDropTableSql("entities");
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return Collections.emptyList();
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class[] {FakeEntity.class};
  }

  /**
   * Placeholder entity class used by {@link HibernateMigrationHelper#getDropTableSql(String)} to
   * generate the correct DROP TABLE SQL statement for the 'entities' table. This class does not
   * represent actual domain logic; it's only used during migration.
   */
  @Entity(name = "Entities")
  @AttributeAccessor("field")
  public static class FakeEntity {
    @Id long id;
  }
}
