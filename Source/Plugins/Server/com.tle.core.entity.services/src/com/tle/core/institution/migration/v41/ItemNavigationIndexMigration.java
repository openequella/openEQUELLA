package com.tle.core.institution.migration.v41;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.Query;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class ItemNavigationIndexMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ItemNavigationIndexMigration.class) + "."; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM ItemNavigationNode"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		Query query = session.createQuery("FROM ItemNavigationNode"); //$NON-NLS-1$
		List<FakeItemNavigationNode> nodes = query.list();

		int index = 0;

		for( FakeItemNavigationNode node : nodes )
		{
			index = 0;
			for( FakeItemNavigationTab tab : node.tabs )
			{
				tab.tabindex = index;
				session.update(tab);
				index++;
			}
			result.incrementStatus();
		}
		session.flush();
		session.clear();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return helper.getAddColumnsSQL("item_navigation_tab", "tabindex"); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeItemNavigationNode.class, FakeItemNavigationTab.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getAddNotNullSQL("item_navigation_tab", "tabindex"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "itemnavigationindex.title", keyPrefix //$NON-NLS-1$
			+ "itemnavigationindex.description"); //$NON-NLS-1$
	}

	@Entity(name = "ItemNavigationNode")
	@AccessType("field")
	public static class FakeItemNavigationNode
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public long id;

		@OneToMany(fetch = FetchType.LAZY, mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeItemNavigationTab> tabs;
	}

	@Entity(name = "ItemNavigationTab")
	@AccessType("field")
	public static class FakeItemNavigationTab
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		public long id;

		@ManyToOne
		@JoinColumn(nullable = false)
		@Index(name = "itemNavTabNode")
		public FakeItemNavigationNode node;

		@Column(nullable = false)
		Integer tabindex;
	}
}
