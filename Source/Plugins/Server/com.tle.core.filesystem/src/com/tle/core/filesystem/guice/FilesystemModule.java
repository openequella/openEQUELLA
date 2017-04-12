package com.tle.core.filesystem.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;

@SuppressWarnings("nls")
public class FilesystemModule extends OptionalConfigModule
{

	@Override
	protected void configure()
	{
		bindBoolean("files.useXSendfile");
		install(new FilesystemMandatoryModule());
	}

	public static class FilesystemMandatoryModule extends MandatoryConfigModule
	{

		@Override
		protected void configure()
		{
			bindFile("filestore.root");
		}

	}

}
