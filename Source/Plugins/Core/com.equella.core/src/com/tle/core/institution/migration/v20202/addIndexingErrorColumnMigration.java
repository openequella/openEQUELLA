package com.tle.core.institution.migration.v20202;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

@Bind
@Singleton
public class addIndexingErrorColumnMigration extends AbstractHibernateSchemaMigration {

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    Query q = session.createQuery("UPDATE Attachment SET errored_when_indexing = :value");
    q.setParameter("value", false);
    q.executeUpdate();
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return 1;
  }

  @Override
  protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
    return helper.getAddNotNullSQL("attachment", "errored_when_indexing");
  }

  @Override
  protected List<String> getAddSql(HibernateMigrationHelper helper) {
    return helper.getAddColumnsSQL("attachment", "errored_when_indexing");
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeAttachment.class};
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.entity.services.migration.v20202.indexing.errored");
  }

  @Entity(name = "Attachment")
  @AccessType("field")
  public static class FakeAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Column boolean errored_when_indexing;
  }
}
