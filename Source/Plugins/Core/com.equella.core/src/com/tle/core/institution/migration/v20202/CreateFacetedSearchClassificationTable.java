package com.tle.core.institution.migration.v20202;

import com.tle.beans.Institution;
import com.tle.core.facetedsearch.bean.FacetedSearchClassification;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import javax.inject.Singleton;

@Bind
@Singleton
public class CreateFacetedSearchClassificationTable extends AbstractCreateMigration {
  private final String TABLE_NAME = "faceted_search_classification";

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.entity.services.migration.v20202.facetedsearch.classification");
  }

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    return new TablesOnlyFilter(TABLE_NAME);
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {Institution.class, FacetedSearchClassification.class};
  }
}
