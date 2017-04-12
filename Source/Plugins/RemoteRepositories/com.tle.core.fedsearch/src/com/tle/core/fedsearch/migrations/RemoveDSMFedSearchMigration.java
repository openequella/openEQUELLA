package com.tle.core.fedsearch.migrations;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveDSMFedSearchMigration extends AbstractRemoveFedSearchMigration
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(PluginServiceImpl.getMyPluginId(RemoveDSMFedSearchMigration.class) + ".removedsm.title");
	}

	@Override
	protected String getFedSearchType()
	{
		return "DSMSearchEngine";
	}
}
