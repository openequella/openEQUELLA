package com.tle.core.hierarchy.migration;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.security.AccessExpression;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@SuppressWarnings("nls")
public class HierarchyACLDataMigration extends AbstractHibernateDataMigration
{
	private static final String KEY_PREFIX = PluginServiceImpl.getMyPluginId(HierarchyACLDataMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PREFIX + "migration.acl.title");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		List<FakeAccessEntry> list = session.createQuery(
			"from AccessEntry ae where ae.privilege = 'ADD_K_R_TO_HIERARCHY_TOPIC'").list();
		for( FakeAccessEntry entry : list )
		{
			entry.privilege = "MODIFY_KEY_RESOURCE";
			session.save(entry);
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from AccessEntry ae where ae.privilege = 'ADD_K_R_TO_HIERARCHY_TOPIC'");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeAccessEntry.class, AccessExpression.class, Institution.class};
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "accessEntryExpression")
		AccessExpression expression;

		@ManyToOne(fetch = FetchType.LAZY)
		@JoinColumn(nullable = false)
		@Index(name = "accessEntryInstitution")
		Institution institution;

		@Column(length = 80)
		@Index(name = "targetObjectIndex")
		String targetObject;

		@Column(length = 30)
		@Index(name = "privilegeIndex")
		String privilege;

		@Column(length = 12, nullable = false)
		@Index(name = "aggregateOrderingIndex")
		String aggregateOrdering;
	}
}
