package com.tle.core.hierarchy.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

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
public class RemoveCoursesMigration extends AbstractHibernateSchemaMigration
{
	private static final String TITLE_KEY = PluginServiceImpl.getMyPluginId(RemoveCoursesMigration.class)
		+ ".migration.removecourses.title";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(TITLE_KEY);
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeHierarchyTopicCourses.class,};
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("hierarchy_topic_courses");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to do!
	}

	@Entity(name = "HierarchyTopicCourses")
	@AccessType("field")
	public static class FakeHierarchyTopicCourses
	{
		@Id
		long hierarchyTopicId;
	}
}
