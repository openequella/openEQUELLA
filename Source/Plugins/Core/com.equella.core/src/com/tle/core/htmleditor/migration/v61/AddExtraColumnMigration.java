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

package com.tle.core.htmleditor.migration.v61;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import org.hibernate.Session;
import org.hibernate.annotations.AccessType;

@SuppressWarnings("nls")
@Bind
@Singleton
public class AddExtraColumnMigration extends AbstractHibernateSchemaMigration {
  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.htmleditor.migration.addextracolumn.title");
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) {
    // No data migration
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
    return helper.getAddColumnsSQL("html_editor_plugin", "extra", "client_js", "server_js");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeBaseEntity.class, FakeHtmlEditorPlugin.class};
  }

  @Entity(name = "BaseEntity")
  @AccessType("field")
  @Inheritance(strategy = InheritanceType.JOINED)
  public static class FakeBaseEntity {
    @Id long id;
  }

  @Entity(name = "HtmlEditorPlugin")
  @AccessType("field")
  public static class FakeHtmlEditorPlugin extends FakeBaseEntity {
    @Lob String extra;
    @Lob String clientJs;
    @Lob String serverJs;
  }
}
