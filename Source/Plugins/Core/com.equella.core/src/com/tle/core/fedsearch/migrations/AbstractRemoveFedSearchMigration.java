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

package com.tle.core.fedsearch.migrations;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.classic.Session;

import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationResult;

@SuppressWarnings("nls")
public abstract class AbstractRemoveFedSearchMigration extends AbstractHibernateDataMigration
{
	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeBaseEntity.Attribute.class, FakeLanguageBundle.class,
				FakeLanguageString.class, FakeFederatedSearch.class, FakeEntityLock.class,};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	protected abstract String getFedSearchType();

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM FederatedSearch WHERE type = '" + getFedSearchType() + "'");
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		final List<FakeFederatedSearch> fs = session.createQuery(
			"FROM FederatedSearch WHERE type = '" + getFedSearchType() + "'").list();
		if( !fs.isEmpty() )
		{
			session.createQuery("DELETE FROM EntityLock WHERE entity IN (:entities)").setParameterList("entities", fs)
				.executeUpdate();

			for( FakeFederatedSearch f : fs )
			{
				session.delete(f);
				result.incrementStatus();
			}
		}
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		long id;

		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle name;
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		FakeLanguageBundle description;

		@JoinColumn
		@ElementCollection(fetch = FetchType.EAGER)
		@CollectionTable(name = "base_entity_attributes", joinColumns = @JoinColumn(name = "base_entity_id"))
		@Fetch(value = FetchMode.SUBSELECT)
		List<Attribute> attributes;

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

	@Entity(name = "LanguageBundle")
	@AccessType("field")
	public static class FakeLanguageBundle
	{
		@Id
		long id;

		@OneToMany(cascade = CascadeType.ALL, mappedBy = "bundle")
		@Fetch(value = FetchMode.SELECT)
		@MapKey(name = "locale")
		Map<String, FakeLanguageString> strings;
	}

	@Entity(name = "LanguageString")
	@AccessType("field")
	public static class FakeLanguageString
	{
		@Id
		long id;

		@Column(length = 20, nullable = false)
		String locale;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		FakeLanguageBundle bundle;
	}

	@Entity(name = "FederatedSearch")
	@AccessType("field")
	public static class FakeFederatedSearch extends FakeBaseEntity
	{
		String type;
	}

	@Entity(name = "EntityLock")
	@AccessType("field")
	public static class FakeEntityLock
	{
		@Id
		long id;

		@OneToOne(fetch = FetchType.LAZY)
		FakeBaseEntity entity;
	}
}
