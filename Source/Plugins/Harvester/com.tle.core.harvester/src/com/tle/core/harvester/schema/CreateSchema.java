package com.tle.core.harvester.schema;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.itemdef.ItemdefBlobs;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;

@Bind
@Singleton
public class CreateSchema extends AbstractCreateMigration
{

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("harvester_profile"); //$NON-NLS-1$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{HarvesterProfile.class, BaseEntity.class, LanguageBundle.class, Institution.class,
				LanguageString.class, ItemdefBlobs.class, BaseEntity.Attribute.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.harvester.migration.title", //$NON-NLS-1$
			"com.tle.core.harvester.description"); //$NON-NLS-1$
	}

}
