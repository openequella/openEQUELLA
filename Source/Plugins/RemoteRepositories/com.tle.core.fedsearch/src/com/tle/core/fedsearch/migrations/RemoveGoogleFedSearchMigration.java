package com.tle.core.fedsearch.migrations;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveGoogleFedSearchMigration extends AbstractRemoveFedSearchMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(RemoveGoogleFedSearchMigration.class) + ".removegoogle.title");
	}

	@Override
	protected String getFedSearchType()
	{
		return "GoogleSearchEngine";
	}
}
