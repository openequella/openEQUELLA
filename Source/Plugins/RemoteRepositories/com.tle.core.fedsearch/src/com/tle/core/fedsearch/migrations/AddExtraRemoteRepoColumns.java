package com.tle.core.fedsearch.migrations;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
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
public class AddExtraRemoteRepoColumns extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(AddExtraRemoteRepoColumns.class) + ".setupremoterepo.";

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 2;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query setDenyQuery = session.createQuery("UPDATE ItemDefinition SET denyDirectContribution = :value");
		setDenyQuery.setParameter("value", false);

		Query setDisabledQuery = session.createQuery("UPDATE FederatedSearch SET disabled = :value");
		setDisabledQuery.setParameter("value", false);

		setDenyQuery.executeUpdate();
		setDisabledQuery.executeUpdate();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getAddColumnsSQL("item_definition", "deny_direct_contribution");
		sql.addAll((helper.getAddColumnsSQL("federated_search", "collection_uuid", "disabled")));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBaseEntity.class, FakeItemDefinition.class, FakeFederatedSearch.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Entity(name = "FederatedSearch")
	@AccessType("field")
	public static class FakeFederatedSearch extends FakeBaseEntity
	{
		public String collectionUuid;

		public boolean disabled;
	}

	@Entity(name = "ItemDefinition")
	@AccessType("field")
	public static class FakeItemDefinition extends FakeBaseEntity
	{
		public boolean denyDirectContribution;
	}

	@Entity(name = "BaseEntity")
	@AccessType("field")
	@Inheritance(strategy = InheritanceType.JOINED)
	public static class FakeBaseEntity
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public long id;
	}
}
