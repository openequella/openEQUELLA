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

package com.tle.core.institution.migration.v20251;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import org.hibernate.Session;

/**
 * This migration updates the data type of column 'allow_expression' in table 'lti_platform' from
 * varchar(255) to LOB. This is because it's easy to exceed the length limit when user selects
 * multiple users/roles/groups for ACL control.
 */
@Bind
@Singleton
public class UpdateAllowExpressionColumn extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    List<String> sql =
        helper.getUpdateColumnSQL("lti_platform", "allow_expression", "temp_lob_allow_expression");
    executeSqlStatements(result, session, sql);
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeLtiPlatform.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20251.lti.platform.allow.expression.add.lob");
  }

  @Entity(name = "LtiPlatform")
  public static class FakeLtiPlatform {
    @Id private Long id;

    @Lob private String allowExpression;
    @Lob private String tempLobAllowExpression;
  }
}
