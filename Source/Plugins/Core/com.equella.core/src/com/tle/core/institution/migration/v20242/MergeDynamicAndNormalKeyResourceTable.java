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

package com.tle.core.institution.migration.v20242;

import com.google.inject.Singleton;
import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.Session;

/**
 * In order to support versioned key resources, the table for dynamic key resources should be reused
 * to store normal key resources. To achieve this, these steps are followed:
 *
 * <p>1. Get all old key resources from the original "hierarchy_topic_key_resources" and save to
 * "hierarchy_topic_dynamic_key_re" table.
 *
 * <p>2. Delete the original key resource table "hierarchy_topic_key_resources".
 *
 * <p>3. Rename columns in "hierarchy_topic_dynamic_key_re" table.
 *
 * <p>4. Rename index in "hierarchy_topic_dynamic_key_re" table.
 *
 * <p>5. Rename "hierarchy_topic_dynamic_key_re" table to "hierarchy_topic_key_resources".
 */
@Bind
@Singleton
public class MergeDynamicAndNormalKeyResourceTable extends AbstractHibernateSchemaMigration {
  private static final String DYNAMIC_KEY_RESOURCE_TABLE = "hierarchy_topic_dynamic_key_re";
  private static final String KEY_RESOURCE_TABLE = "hierarchy_topic_key_resources";

  private Stream<FakeKeyResource> buildKeyResources(FakeHierarchyTopic hierarchy) {
    return hierarchy.keyResources.stream()
        .map(
            item -> {
              FakeKeyResource keyResource = new FakeKeyResource();
              keyResource.dynamicHierarchyId = hierarchy.uuid;
              keyResource.uuid = item.uuid;
              keyResource.version = item.version;
              keyResource.institutionId = hierarchy.institution.getDatabaseId();
              keyResource.dateCreated = new Date();
              return keyResource;
            });
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    // Get all old key resources from KEY_RESOURCE_TABLE and save to DYNAMIC_KEY_RESOURCE_TABLE.
    List<FakeHierarchyTopic> hierarchies = session.createQuery("From HierarchyTopic").list();
    hierarchies.stream().flatMap(this::buildKeyResources).forEach(session::save);
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    // Delete old key resource table.
    String dropOldKeyResourceTable = helper.getDropTableSql(KEY_RESOURCE_TABLE).getFirst();

    // update column name.
    String updateIdColumnSql =
        helper
            .getRenameColumnSQL(
                DYNAMIC_KEY_RESOURCE_TABLE, "dynamic_hierarchy_id", "hierarchy_compound_uuid")
            .getFirst();
    String updateUuidColumnSql =
        helper.getRenameColumnSQL(DYNAMIC_KEY_RESOURCE_TABLE, "uuid", "item_uuid").getFirst();
    String updateVersionColumnSql =
        helper.getRenameColumnSQL(DYNAMIC_KEY_RESOURCE_TABLE, "version", "item_version").getFirst();
    // update index name
    String updateKeyResourceHierarchyIndex =
        helper.getRenameIndexSQL(
            DYNAMIC_KEY_RESOURCE_TABLE, "dynamic_hierarchy_id", "key_resource_hierarchy_uuid");

    // Rename dynamic table to key resources table.
    String renameDynamicTableSql =
        helper.getRenameTableSQL(DYNAMIC_KEY_RESOURCE_TABLE, KEY_RESOURCE_TABLE);

    return Arrays.asList(
        dropOldKeyResourceTable,
        updateIdColumnSql,
        updateUuidColumnSql,
        updateVersionColumnSql,
        updateKeyResourceHierarchyIndex,
        renameDynamicTableSql);
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {
      Institution.class,
      HierarchyTopicKeyResource.class,
      FakeItem.class,
      FakeHierarchyTopic.class,
      FakeKeyResource.class,
    };
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20242.hierarchy.key.resource");
  }

  @Entity(name = "Item")
  public static class FakeItem {
    @Id long id;

    String uuid;

    int version;
  }

  @Entity(name = "HierarchyTopic")
  private static final class FakeHierarchyTopic {
    @Id long id;

    String uuid;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Institution institution;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "hierarchy_topic_key_resources",
        joinColumns = {@JoinColumn(name = "hierarchy_topic_id")},
        inverseJoinColumns = {@JoinColumn(name = "element")})
    List<FakeItem> keyResources;
  }

  // Represents the old dynamic key resource table.
  @Table(name = DYNAMIC_KEY_RESOURCE_TABLE)
  @Entity
  private static final class FakeKeyResource {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    String dynamicHierarchyId;
    String uuid;
    int version;
    Date dateCreated;
    long institutionId;
  }
}
