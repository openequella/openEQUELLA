package com.tle.core.customlinks.migration.v50;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.entity.itemdef.ItemdefBlobs;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateCustomLinksEntities extends AbstractCreateMigration
{

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("custom_link");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{CustomLink.class, BaseEntity.class, LanguageBundle.class, Institution.class,
				LanguageString.class, ItemdefBlobs.class, BaseEntity.Attribute.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.customlinks.migration.title");
	}

}
