/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.taxonomy.institution.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TermUuidMigration extends AbstractHibernateSchemaMigration {
	private static final int BATCH_SIZE = 1000;
	private static final String TABLE = "term";
	private static final String COLUMN = "uuid";
	@Override
	public MigrationInfo createMigrationInfo() {
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(TermUuidMigration.class) + ".migration.title");
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper,
			MigrationResult result, Session session) throws Exception {
		List<FakeTerm> terms = session.createQuery("FROM Term").list();
		session.clear();

		int i = 0;
		for( FakeTerm term : terms )
		{
			term.uuid = UUID.randomUUID().toString();

			session.update(term);
			i++;
			if( i % BATCH_SIZE == 0 )
			{
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper,
			Session session) {
		return count(session, "FROM Term");

	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper) {
		List<String> dropModify = new ArrayList<String>();
		dropModify.addAll(helper.getAddNotNullSQL(TABLE, COLUMN));
		dropModify.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE, COLUMN));
		return dropModify;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper) {
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE, COLUMN));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses() {
		return new Class[]{FakeTerm.class, FakeTaxonomy.class, };

	}
	
	@Entity(name = "Taxonomy")
	@AccessType("field")
	public static class FakeTaxonomy
	{
		@Id
		long id;
	}

	@Entity(name = "Term")
	@Table(name = "term", uniqueConstraints = {@UniqueConstraint(columnNames = {"taxonomy_id", "parent_id", "valueHash", "uuid"})})
	@AccessType("field")
	public static class FakeTerm
	{
		@Id
		long id;
		@Column(length = 40, nullable = false)
		@Index(name = "termUuidIndex")
		String uuid;

		@Column(length = 32)
		private String valueHash;

		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "term_parent")
		private FakeTerm parent;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakeTaxonomy taxonomy;
	}
}
