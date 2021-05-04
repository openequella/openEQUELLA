package com.tle.core.institution.migration.v66;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import java.util.List;
import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.Session;
import org.hibernate.annotations.AttributeAccessor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Bind
@Singleton
public class NewAuditLogColumn extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {}

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  public boolean isBackwardsCompatible() {
    return true;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return null;
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return helper.getAddColumnsSQL("audit_log_entry", "meta");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeAuditLogEntry.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v66.auditlogentry.meta");
  }

  @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
  @Entity(name = "AuditLogEntry")
  @AttributeAccessor("field")
  public static class FakeAuditLogEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String meta;
  }
}
