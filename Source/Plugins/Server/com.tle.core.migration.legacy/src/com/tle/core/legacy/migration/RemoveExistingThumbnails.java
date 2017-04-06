package com.tle.core.legacy.migration;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class RemoveExistingThumbnails extends AbstractHibernateDataMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(RemoveExistingThumbnails.class) + ".removethumbnails.title"; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// Set all thumb nails to NULL
		session.createQuery("UPDATE Attachment SET thumbnail = NULL").executeUpdate(); //$NON-NLS-1$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAttachment.class};
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

		@Column(length = 256)
		String thumbnail;
	}
}
