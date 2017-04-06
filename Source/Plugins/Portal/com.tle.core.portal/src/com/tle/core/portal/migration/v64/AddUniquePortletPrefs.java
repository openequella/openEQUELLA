package com.tle.core.portal.migration.v64;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;

import com.google.common.collect.Sets;
import com.tle.common.i18n.KeyString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddUniquePortletPrefs extends AbstractHibernateSchemaMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(AddUniquePortletPrefs.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(new KeyString(KEY_PREFIX + "migrate.adduniqueportletprefs"));
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// delete one of the duplicates
		final ScrollableResults dupes = session.createQuery(getDupesFrom() + " ORDER BY p.id DESC").scroll(
			ScrollMode.FORWARD_ONLY);
		final Set<String> visited = Sets.newHashSet();
		while( dupes.next() )
		{
			final FakePortletPreference dupe = (FakePortletPreference) dupes.get(0);
			final String key = dupe.userId + dupe.portlet.id;

			if( !visited.contains(key) )
			{
				visited.add(key);
			}
			else
			{
				session.delete(dupe);
				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session.createQuery("SELECT COUNT(*) " + getDupesFrom()));
	}

	private String getDupesFrom()
	{
		return "FROM portlet_preference p WHERE 1 < (SELECT COUNT(*) FROM portlet_preference p2 WHERE p2.portlet.id = p.portlet.id AND p2.userId = p.userId )";
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{

		return helper.getAddIndexesAndConstraintsForColumns("portlet_preference", false, "portlet_id", "user_id");
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakePortletPreference.class, FakePortlet.class};
	}

	@Entity(name = "portlet_preference")
	@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"portlet_id", "userId"}))
	public static class FakePortletPreference
	{
		@Id
		long id;
		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		FakePortlet portlet;
		String userId;
	}

	@Entity(name = "Portlet")
	public static class FakePortlet
	{
		@Id
		long id;
	}

}
