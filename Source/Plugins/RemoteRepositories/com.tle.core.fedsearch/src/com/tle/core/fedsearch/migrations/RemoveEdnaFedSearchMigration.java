package com.tle.core.fedsearch.migrations;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveEdnaFedSearchMigration extends AbstractRemoveFedSearchMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(RemoveEdnaFedSearchMigration.class) + ".removeedna.title");
	}

	@Override
	protected String getFedSearchType()
	{
		return "EdnaOnlineSearchEngine";
	}
}
