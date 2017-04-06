package com.tle.core.taxonomy.institution.migration;

import java.util.List;

import javax.inject.Singleton;

import org.hibernate.classic.Session;

import com.google.common.collect.Lists;
import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.terms.Term;
import com.tle.common.taxonomy.terms.Term.TermAttribute;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

/**
 * @author larry
 */
@Bind
@Singleton
public class AddTermAttributeKeyIndex extends AbstractHibernateSchemaMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(AddTermAttributeKeyIndex.class)
			+ "migration.attrkeyindex");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// nothing
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		// nothing
		return 0;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		// nothing
		return null;
	}

	/**
	 * Oracle at least doesn't accept a column - including an index's column -
	 * named "KEY" but will accept "key", so an intervention within the
	 * statement building is necessary. backquotes?
	 */
	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = Lists.newArrayList();
		sql.addAll(helper.getAddIndexesRaw("term_attributes", "termAttrKey", "`key`"));
		return sql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{Institution.class, BaseEntity.class, BaseEntity.Attribute.class, Taxonomy.class,
				Term.class, TermAttribute.class, LanguageBundle.class, LanguageString.class};
	}
}
