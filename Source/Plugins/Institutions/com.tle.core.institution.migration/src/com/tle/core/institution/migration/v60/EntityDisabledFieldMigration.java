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

package com.tle.core.institution.migration.v60;

import java.io.Serializable;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EntityDisabledFieldMigration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_ENTITY = "base_entity";
	private static final String COL_DISABLED = "disabled";
	private static final String KEY_ARCHIVED = "archived";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.v60.disabledentity.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		session.createQuery("UPDATE BaseEntity SET disabled = false").executeUpdate();
		result.incrementStatus();

		ScrollableResults scroll = session
			.createQuery(
				"FROM BaseEntity be LEFT JOIN be.attributes att WHERE att.key = :archived AND att.value = :true")
			.setParameter("archived", KEY_ARCHIVED).setParameter("true", "true").scroll();
		while( scroll.next() )
		{
			FakeBaseEntity be = (FakeBaseEntity) scroll.get(0);
			be.disabled = true;
			session.save(be);
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		Query query = session
			.createQuery(
				"SELECT COUNT(*) FROM BaseEntity be LEFT JOIN be.attributes att WHERE att.key = :archived AND att.value = :true")
			.setParameter("archived", KEY_ARCHIVED).setParameter("true", "true");
		return 1 + count(query);
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		final List<String> dropModify = Lists.newArrayList();
		dropModify.addAll(helper.getAddNotNullSQL(TABLE_ENTITY, COL_DISABLED));
		dropModify.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE_ENTITY, COL_DISABLED));
		return dropModify;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL(TABLE_ENTITY, COL_DISABLED);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeBaseEntity.Attribute.class};
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	public static class FakeBaseEntity
	{
		@Id
		long id;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "base_entity_attributes", joinColumns = @JoinColumn(name = "base_entity_id") )
		@Fetch(value = FetchMode.SUBSELECT)
		List<Attribute> attributes;

		@Index(name = "disabledIndex")
		Boolean disabled;

		@Embeddable
		@AccessType("field")
		public static class Attribute implements Serializable
		{
			private static final long serialVersionUID = 1L;

			@Column(length = 64, nullable = false)
			String key;
			@Column(name = "value", length = 1024)
			String value;
		}
	}
}
