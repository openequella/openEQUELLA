package com.tle.core.institution.migration.v65;

import com.tle.beans.user.UserInfoBackup;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import javax.inject.Singleton;

@Bind
@Singleton
public class CreateUserInfoBackupTable extends AbstractCreateMigration {
  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v65.unknownuser.alluser");
  }

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter("user_info_backup");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {UserInfoBackup.class};
  }
}
