package com.tle.core.institution.migration.v60;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntity.Attribute;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;

/**
 * @author larry
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class AddBaseEntitySystemTypeIndex extends AbstractHibernateSchemaMigration
{
	private static final String BASE_ENTITY_SYSTEMTYPE_INDEX = "baseEntitySystemTypeIndex";
	private static final String COLUMN_NAME = "system_type";
	private static final String BASE_ENTITY_TABLE = "base_entity";

	/**
	 * @see com.tle.core.migration.Migration#createMigrationInfo()
	 */
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.entity.services.migration.v60.systemindex.title");
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#executeDataMigration(com.tle.core.hibernate.impl.HibernateMigrationHelper,
	 *      com.tle.core.migration.MigrationResult,
	 *      org.hibernate.classic.Session)
	 */
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Nothing to do: index is self populating
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#countDataMigrations(com.tle.core.hibernate.impl.HibernateMigrationHelper,
	 *      org.hibernate.classic.Session)
	 */
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	/**
	 * Nothing to remove
	 * 
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#getDropModifySql(com.tle.core.hibernate.impl.HibernateMigrationHelper)
	 */
	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#getAddSql(com.tle.core.hibernate.impl.HibernateMigrationHelper)
	 */
	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = new ArrayList<String>();
		sql.addAll(helper.getAddIndexesRaw(BASE_ENTITY_TABLE, BASE_ENTITY_SYSTEMTYPE_INDEX, COLUMN_NAME));
		return sql;
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#getDomainClasses()
	 */
	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{BaseEntity.class, Institution.class, Attribute.class, LanguageBundle.class,
				LanguageString.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}
}
