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

package com.tle.core.institution.migration.v20252;

import com.google.inject.Singleton;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.Session;

/**
 * This migration renames the 'date_modified' column in the bookmark and favourite_search tables.
 * This aims to provide a clear meaning of the column, as it represents the time when the favourite
 * item/search was added.
 */
@Bind
@Singleton
public class RenameFavouritesDateModified extends AbstractHibernateSchemaMigration {
  private static final String FAVOURITE_SEARCH_TABLE = "favourite_search";
  private static final String FAVOURITE_ITEM_TABLE = "bookmark";
  private static final String DATE_MODIFIED = "date_modified";
  private static final String ADDED_AT = "added_at";

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
    return Arrays.asList(
        helper.getRenameColumnSQL(FAVOURITE_SEARCH_TABLE, DATE_MODIFIED, ADDED_AT).getFirst(),
        helper.getRenameColumnSQL(FAVOURITE_ITEM_TABLE, DATE_MODIFIED, ADDED_AT).getFirst());
  }

  @Override
  protected Class<?>[] getDomainClasses() {
    return new Class<?>[] {
      FakeFavouriteSearch.class, FakeBookmark.class,
    };
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo(
        "com.tle.core.institution.migration.v20252.rename.favourites.date.modified");
  }

  // Represents the old favourite_search table.
  @Table(name = FAVOURITE_SEARCH_TABLE)
  @Entity
  private static final class FakeFavouriteSearch {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Date dateModified;
  }

  // Represents the old bookmark table.
  @Table(name = FAVOURITE_ITEM_TABLE)
  @Entity
  private static final class FakeBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private Date dateModified;
  }
}
