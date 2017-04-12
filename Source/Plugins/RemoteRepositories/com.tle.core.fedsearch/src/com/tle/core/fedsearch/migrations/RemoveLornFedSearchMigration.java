package com.tle.core.fedsearch.migrations;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveLornFedSearchMigration extends AbstractRemoveFedSearchMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(RemoveLornFedSearchMigration.class)
			+ ".removelorn.title");
	}

	@Override
	protected String getFedSearchType()
	{
		return "LornSearchEngine";
	}
}
