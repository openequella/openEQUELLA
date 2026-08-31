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

package com.tle.core.institution.migration.v20262;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import org.hibernate.Session;
import org.hibernate.annotations.AttributeAccessor;

/**
 * The virtual name of a hierarchy compound UUID used to be encoded with the standard base64
 * alphabet, which is not safe to put in a URL - the '/' splits the URL path segment and the '+' is
 * read back as a space. It is now encoded with the unpadded URL-safe alphabet instead.
 *
 * <p>Favouriting a hierarchy search stores the New UI page URL (which embeds the compound UUID in
 * its path) in the 'url' column of the 'favourite_search' table, so those rows need rewriting.
 */
@Bind
@Singleton
public class MigrateHierarchyFavouriteSearchUrl extends AbstractHibernateDataMigration {
  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.institution.migration.v20262.migrate.hierarchy.favourite.search.url");
  }

  @Override
  protected int countDataMigrations(HibernateMigrationHelper helper, Session session) {
    return count(session, "FROM FavouriteSearch");
  }

  @Override
  protected void executeDataMigration(
      HibernateMigrationHelper helper, MigrationResult result, Session session) throws Exception {
    final List<FakeFavouriteSearch> searches =
        session.createQuery("FROM FavouriteSearch", FakeFavouriteSearch.class).list();

    for (FakeFavouriteSearch search : searches) {
      // Hibernate session tracks changes via dirty checking.
      search.url = MigrateHierarchyFavouriteSearchUrlHelper.toUrlSafeFormat(search.url);
      result.incrementStatus();
    }
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {FakeFavouriteSearch.class};
  }

  @Entity(name = "FavouriteSearch")
  @AttributeAccessor("field")
  public static class FakeFavouriteSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    @Lob String url;
  }
}
