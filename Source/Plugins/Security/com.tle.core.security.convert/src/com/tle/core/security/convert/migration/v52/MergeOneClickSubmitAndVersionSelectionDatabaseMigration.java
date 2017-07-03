/**
 * 
 */
package com.tle.core.security.convert.migration.v52;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author larry The former oneClickSubmit (aka Quick contribute,
 *         one.click.collection) and the version selection system settings moved
 *         from the admin console and merged into a single screen in EQUELLA
 *         system settings page. Accordingly the ACLs are updated in that the
 *         oneClickSubmit is removed altogether, and the versionSelection
 *         renamed to selectionSessions. In effect, all privilege holders who
 *         formerly held versionSelection now additionally hold oneClickSubmit.
 *         Former holders of oneClickSubmit who were excluded from
 *         versionSelection are now excluded from oneClickSubmit functionality.<br />
 *         Note we are taking advantage of this class to also remove the
 *         obsolete loginNotice and freetext privileges.
 */
@Bind
@Singleton
public class MergeOneClickSubmitAndVersionSelectionDatabaseMigration extends AbstractHibernateDataMigration
{
	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.mergeoneclickandversionselectprivs");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		session
			.createQuery(
				"DELETE FROM AccessEntry ae WHERE ae.targetObject IN ('C:loginnotice', 'C:oneClickSubmit', 'C:freetext')")
			.executeUpdate();
		session
			.createQuery(
				"UPDATE AccessEntry SET targetObject = 'C:selectionSessions' WHERE targetObject = 'C:versionSelection'")
			.executeUpdate();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		int ct = count(session.createQuery(
			"SELECT COUNT(*) From AccessEntry ae WHERE ae.targetObject in ('C:oneClickSubmit', 'C:versionSelection', 'C:loginnotice', 'C:freetext')"));
		return ct;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAccessEntry.class};
	}

	@SuppressWarnings("unused")
	@Entity(name = "AccessEntry")
	@AccessType("field")
	private static class FakeAccessEntry
	{
		@Id
		public long id;
		public String targetObject;
	}
}
