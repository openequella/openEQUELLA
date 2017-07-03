package com.tle.core.institution.migration.v41;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.beans.item.VersionSelection;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

@Bind
@Singleton
public class CourseInfoMigration extends AbstractHibernateSchemaMigration
{
	private static final String TABLE_NAME = "course_info";
	private static final String NEW_SETTING_COLUMN = "version_selection";

	@SuppressWarnings("nls")
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.courseinfo.versel.title");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeCourseInfo.class, VersionSelection.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddColumnsSQL(TABLE_NAME, NEW_SETTING_COLUMN));
		sql.addAll(helper.getAddIndexesAndConstraintsForColumns(TABLE_NAME, NEW_SETTING_COLUMN));
		return sql;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to do here
	}

	@Entity(name = "CourseInfo")
	@AccessType("field")
	public static class FakeCourseInfo
	{
		@Id
		long id;

		@Column(length = 30)
		@Enumerated(EnumType.STRING)
		VersionSelection versionSelection;
	}
}
