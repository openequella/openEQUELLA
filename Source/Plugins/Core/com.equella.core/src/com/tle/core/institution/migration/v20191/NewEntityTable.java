package com.tle.core.institution.migration.v20191;

import com.tle.beans.newentity.Entity;
import com.tle.beans.newentity.EntityID;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import javax.inject.Singleton;

@Bind
@Singleton
public class NewEntityTable extends AbstractCreateMigration {
  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20191.entity");
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter("entities");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {Entity.class, EntityID.class};
  }
}
