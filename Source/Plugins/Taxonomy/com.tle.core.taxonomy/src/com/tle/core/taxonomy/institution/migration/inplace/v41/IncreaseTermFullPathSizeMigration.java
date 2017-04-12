package com.tle.core.taxonomy.institution.migration.inplace.v41;

import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.tle.common.taxonomy.terms.Term;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class IncreaseTermFullPathSizeMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(IncreaseTermFullPathSizeMigration.class) + "."; //$NON-NLS-1$

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		result.incrementStatus();
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeTerm.class};
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getModifyColumnSQL("term", "full_value", false, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "term.columnsize.migration.title", //$NON-NLS-1$
			keyPrefix + "term.columnsize.migration.title"); //$NON-NLS-1$
	}

	@Entity(name = "Term")
	@AccessType("field")
	public static class FakeTerm
	{
		@Id
		long id;

		@Column(length = Term.MAX_TERM_FULLVALUE_LENGTH)
		@Type(type = "blankable")
		@Index(name = "term_full_value")
		String fullValue;
	}
}
