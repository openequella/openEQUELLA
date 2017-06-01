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

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.common.taxonomy.terms.Term;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class IncreaseTermSize1024Migration extends AbstractHibernateSchemaMigration
{
	private static final String TITLE_KEY = PluginServiceImpl.getMyPluginId(IncreaseTermSize1024Migration.class)
		+ ".migration.term1024.title";

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeTerm.class, FakeTaxonomy.class,};
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from Term");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		// We need to drop the old (taxonomy ID + parent ID + value) constraint,
		// so create temp column to move the value to, copy the contents, drop
		// the column, rename the temp one back to the original.
		return helper.getAddColumnsSQL("term", "value_temp", "value_hash");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		ScrollableResults res = session.createQuery("from Term").scroll(ScrollMode.FORWARD_ONLY);
		while( res.next() )
		{
			FakeTerm ft = (FakeTerm) res.get(0);
			ft.valueTemp = ft.value;
			// The next time is totally 4337 haX0r
			ft.valueHash = DigestUtils.md5Hex(ft.valueTemp);

			session.update(ft);
			result.incrementStatus();
		}
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		List<String> rv = Lists.newArrayList();

		rv.addAll(helper.getDropColumnSQL("term", "value"));
		rv.addAll(helper.getRenameColumnSQL("term", "value_temp", "value"));

		// Add the uniqueness constraint for the new value_hash
		rv.addAll(helper.getAddIndexesAndConstraintsForColumns("term", "value_hash"));

		// We need to drop the indices since SQL Server complains if your index
		// is "wider" than 900 bytes, which for nvarchar means 450ish
		// characters. This data needs to be put into a Lucene index index as we
		// really can't rely on the DB anymore.
		rv.add(helper.getExtDialect().getDropIndexSql("term", "term_full_value"));

		return rv;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(TITLE_KEY);
	}

	@AccessType("field")
	@Entity(name = "Term")
	@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"taxonomy_id", "parent_id", "valueHash"})})
	public static class FakeTerm
	{
		@Id
		long id;

		@Column(length = Term.MAX_TERM_VALUE_LENGTH, nullable = false)
		String value;

		@Column(length = Term.MAX_TERM_VALUE_LENGTH, nullable = false)
		String valueTemp;

		@Column(length = 32)
		String valueHash;

		@Column(length = Term.MAX_TERM_FULLVALUE_LENGTH)
		String fullValue;

		@ManyToOne(fetch = FetchType.LAZY)
		FakeTerm parent;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakeTaxonomy taxonomy;
	}

	@AccessType("field")
	@Entity(name = "Taxonomy")
	public static class FakeTaxonomy
	{
		@Id
		long id;
	}
}
