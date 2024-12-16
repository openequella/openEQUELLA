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

package com.tle.core.hierarchy.migration;

import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;
import javax.inject.Singleton;

@Bind
@Singleton
public class HierarchyTopicDynamicKeyResourcesMigration extends AbstractCreateMigration {
  private static final String TITLE_KEY =
      PluginServiceImpl.getMyPluginId(HierarchyTopicDynamicKeyResourcesMigration.class)
          + ".migration.dynamic.title";

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(TITLE_KEY);
  }

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter("hierarchy_topic_dynamic_key_re");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {HierarchyTopicKeyResource.class, Institution.class};
  }
}
