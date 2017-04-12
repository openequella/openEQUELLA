package com.tle.core.url.migration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddReferencedUrlIndexes extends AbstractHibernateSchemaMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(AddReferencedUrlIndexes.class) + ".migration.indexes.title";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(migInfo, migInfo);
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.add(helper.getAddNamedIndex("item_referenced_urls", "iruitemidindex", "item_id"));
		sql.add(helper.getAddNamedIndex("item_referenced_urls", "irurefurlsindex", "referenced_urls_id"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeReferencedURL.class, FakeItem.class};
	}

	@Entity(name = "ReferencedURL")
	@AccessType("field")
	public static class FakeReferencedURL
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@ManyToMany
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeReferencedURL> referencedUrls = new ArrayList<FakeReferencedURL>();
	}

}
