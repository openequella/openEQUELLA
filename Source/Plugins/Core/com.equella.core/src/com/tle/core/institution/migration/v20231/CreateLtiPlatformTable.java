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
        "lti_custom_role_oeq_roles");
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
