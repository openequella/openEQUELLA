package com.tle.core.institution.migration.v20261;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.Collections;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.Session;

@Bind
@Singleton
public class DropNewEntityTable extends AbstractHibernateSchemaMigration {

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20261.entity");
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {}

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return helper.getDropTableSql("entities");
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return Collections.emptyList();
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class[] {FakeEntity.class};
  }

  @Entity(name = "Entities")
  public static class FakeEntity {
    @Id long id;
  }
}
