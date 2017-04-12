package com.tle.core.wizard.controls.universal.migration.v52;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.wizard.controls.universal.migration.v50.FixIMSHandlerPath;

/**
 * @author Aaron
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class FixIMSHandlerPathRepeat extends FixIMSHandlerPath
{
	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.wizard.controls.universal.migration.associateattachments.imsfix.title",
			"");
	}
}
