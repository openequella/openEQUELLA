package com.tle.core.mimetypes.migration;

import java.io.Serializable;

import javax.inject.Singleton;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.ScrollableResults;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class UpdateDefaultMimeTypeIcons extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(UpdateDefaultMimeTypeIcons.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		ScrollableResults results = session.createQuery(
			"FROM MimeEntryAttributes WHERE element LIKE '%.gif' AND mapkey = 'PluginIconPath' ").scroll();

		while( results.next() )
		{
			Object[] resultEntry = results.get();
			FakeMimeEntryAttributes fmeAttr = (FakeMimeEntryAttributes) resultEntry[0];
			fmeAttr.element = fmeAttr.element.replaceAll(".gif", ".png");

			session.save(fmeAttr);
			session.flush();
			session.clear();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM MimeEntryAttributes WHERE element LIKE '%.gif' AND mapkey = 'PluginIconPath' ");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeMimeEntryAttributes.class, FakeMimeEntryAttributesKey.class};
	}

	@Entity(name = "MimeEntryAttributes")
	public static class FakeMimeEntryAttributes implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Id
		public FakeMimeEntryAttributesKey mimeEntryId;

		@Lob
		String element;
	}

	@Embeddable
	public static class FakeMimeEntryAttributesKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		long mimeEntryId;

		String mapkey;
	}
}