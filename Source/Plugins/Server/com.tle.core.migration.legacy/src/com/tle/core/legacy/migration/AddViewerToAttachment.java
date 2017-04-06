package com.tle.core.legacy.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
public class AddViewerToAttachment extends AbstractHibernateSchemaMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(AddViewerToAttachment.class) + ".addviewertoattachment.title"; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to see here... move along
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("attachment", "viewer"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAttachment.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(migInfo);
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		String viewer;
	}
}
