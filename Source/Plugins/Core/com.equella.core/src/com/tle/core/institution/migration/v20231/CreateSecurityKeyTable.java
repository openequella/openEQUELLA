package com.tle.core.institution.migration.v20231;

import com.tle.beans.securitykey.SecurityKey;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import javax.inject.Singleton;

@Bind
@Singleton
public class CreateSecurityKeyTable extends AbstractCreateMigration {

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter("security_key");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {SecurityKey.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20231.security.key");
  }
}
