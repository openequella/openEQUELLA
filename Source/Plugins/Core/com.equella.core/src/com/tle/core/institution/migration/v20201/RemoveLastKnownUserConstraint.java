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

package com.tle.core.institution.migration.v20201;

import com.google.inject.Singleton;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

@Bind
@Singleton
public class RemoveLastKnownUserConstraint extends AbstractHibernateSchemaMigration {
  private static final String TABLE_NAME = "user_info_backup";
  private static final String COLUMN_NAME = "username";
  private static final String TEMP_COLUMN_NAME = "username1";

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    List<String> sql = new ArrayList<String>();
    // Rename column username to username1 and add a new column named username.
    sql.addAll(helper.getRenameColumnSQL(TABLE_NAME, COLUMN_NAME, TEMP_COLUMN_NAME));
    sql.addAll(helper.getAddColumnsSQL(TABLE_NAME, COLUMN_NAME));
    return sql;
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    // Copy data from username1 to username.
    Query query =
        session.createQuery("UPDATE UserInfoBackup SET " + COLUMN_NAME + " = " + TEMP_COLUMN_NAME);
    query.executeUpdate();
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    // Drop username1.
    return helper.getDropColumnSQL(TABLE_NAME, TEMP_COLUMN_NAME);
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20201.removelastknownuserconstraint");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeUserInfoBackup.class};
  }

  @Entity(name = "UserInfoBackup")
  @AccessType("field")
  public static class FakeUserInfoBackup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @XStreamOmitField
    long id;

    @Column public String username;

    @Column public String username1;
  }
}
