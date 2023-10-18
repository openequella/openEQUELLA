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

package com.tle.core.institution.migration.v20232;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.Session;
import org.hibernate.annotations.AccessType;

@Bind
@Singleton
public class AddOAuthClientTokenValidity extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    session.createQuery("UPDATE OAuthClient SET tokenValidity = 0").executeUpdate();
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
    return helper.getAddColumnsSQL("oauth_client", "token_validity");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeOAuthClient.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20232.oauthclient.tokenvalidity");
  }

  @Entity(name = "OAuthClient")
  @AccessType("field")
  public static class FakeOAuthClient {
    @Id private Long id;

    @Column(nullable = false)
    private int tokenValidity;
  }
}
