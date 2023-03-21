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

package com.tle.core.institution.migration.v20231;

import com.tle.beans.Institution;
import com.tle.beans.lti.LtiCustomRole;
import com.tle.beans.lti.LtiPlatform;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import javax.inject.Singleton;

@Bind
@Singleton
public class CreateLtiPlatformTable extends AbstractCreateMigration {
  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter(
        "lti_platform",
        "lti_unknown_user_groups",
        "lti_instructor_roles",
        "lti_unknown_roles",
        "lti_custom_role",
        "lti_custom_role_target");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {LtiPlatform.class, LtiCustomRole.class, Institution.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20231.lti.platform");
  }
}
