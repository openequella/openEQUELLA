package com.tle.core.institution.migration.v20241;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.Session;
import org.hibernate.annotations.AccessType;

@Bind
@Singleton
public class AddLTI13UsernameClaim extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {}

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return helper.getAddColumnsSQL("lti_platform", "username_claim");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeLtiPlatform.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20241.lti.platform.username.claim");
  }

  @Entity(name = "LtiPlatform")
  @AccessType("field")
  public static class FakeLtiPlatform {
    @Id private Long id;

    @Column private String usernameClaim;
  }
}
