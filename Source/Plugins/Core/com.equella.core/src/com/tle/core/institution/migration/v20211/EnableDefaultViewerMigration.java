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

package com.tle.core.institution.migration.v20211;

import com.google.inject.Singleton;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.mimetypes.MimeTypeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import org.hibernate.Session;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;

/**
 * Some existing MIME types do not have their default viewers enabled. This results in error
 * messages showing in the New Search UI. So we use this migration to ensure the default viewer is
 * in list of enabled viewers.
 */
@Bind
@Singleton
public class EnableDefaultViewerMigration extends AbstractHibernateDataMigration {

  @Inject private MimeTypeService mimeTypeService;

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    final List<FakeMimeEntry> entries = session.createQuery("FROM MimeEntry").list();
    for (FakeMimeEntry entry : entries) {
      Map<String, String> attributes = entry.attributes;
      // Calling 'getEnabledViewerList' to fix this issue as the list returned from this function
      // include the default viewer whereas the one saved in attributes may have default viewer
      // missing.
      String enabledViewers = mimeTypeService.getEnabledViewerList(attributes);
      if (!Check.isEmpty(enabledViewers)) {
        attributes.put(MimeTypeConstants.KEY_ENABLED_VIEWERS, enabledViewers);
        session.update(entry);
        session.flush();
      }
    }
    result.incrementStatus();
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 0;
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class[] {FakeMimeEntry.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20211.enable.default.viewer");
  }

  @Entity(name = "MimeEntry")
  @AccessType("field")
  public static class FakeMimeEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @ElementCollection
    @Column(name = "element", nullable = false)
    @CollectionTable(
        name = "mime_entry_attributes",
        joinColumns = @JoinColumn(name = "mime_entry_id"))
    @Lob
    @MapKeyColumn(name = "mapkey", length = 100, nullable = false)
    @MapKeyType(@Type(type = "string"))
    Map<String, String> attributes = new HashMap<String, String>();
  }
}
