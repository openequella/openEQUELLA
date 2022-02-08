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

package com.tle.core.institution.migration.v66;

import com.tle.beans.viewcount.ViewcountAttachment;
import com.tle.beans.viewcount.ViewcountAttachmentId;
import com.tle.beans.viewcount.ViewcountItem;
import com.tle.beans.viewcount.ViewcountItemId;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.inject.Singleton;
import org.hibernate.Session;

@Bind
@Singleton
public class NewViewCountTables extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {}

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return helper.getCreationSql(new TablesOnlyFilter("viewcount_item", "viewcount_attachment"));
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {
      ViewcountItem.class,
      ViewcountItemId.class,
      ViewcountAttachment.class,
      ViewcountAttachmentId.class
    };
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v66.viewcount");
  }
}
