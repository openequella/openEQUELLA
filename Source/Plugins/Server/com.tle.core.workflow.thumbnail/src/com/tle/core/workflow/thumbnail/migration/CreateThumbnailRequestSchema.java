package com.tle.core.workflow.thumbnail.migration;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateThumbnailRequestSchema extends AbstractCreateMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(CreateThumbnailRequestSchema.class)
			+ ".migration.createthumbrequest.title");
	}

	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("thumbnail_request");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{ThumbnailRequest.class, Institution.class};
	}
}
