package com.tle.core.filesystem.guice;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.inject.TypeLiteral;
import com.tle.common.filesystem.Filestore;
import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.OptionalConfigModule;

@SuppressWarnings("nls")
public class FilesystemModule extends OptionalConfigModule
{

	@Override
	protected void configure()
	{
		bindBoolean("files.useXSendfile");
		bindBoolean("filestore.advanced");

		final Map<String, Filestore> filestores = new HashMap<>();
		final String filestoresProp = getProperty("filestore.additional.ids");
		if( !Strings.isNullOrEmpty(filestoresProp) )
		{
			final String[] filestoreIds = filestoresProp.split(",");
			for( String filestoreId : filestoreIds )
			{
				String id = filestoreId.trim();
				if( !id.isEmpty() )
				{
					final String nameProp = "filestore.additional." + id + ".name";
					final String name = getProperty(nameProp);
					if( name == null )
					{
						throw new Error("No property " + nameProp + " for filestore ID = " + id);
					}

					final String pathProp = "filestore.additional." + id + ".path";
					final String pathString = getProperty(pathProp);
					if( pathString == null )
					{
						throw new Error("No property " + pathProp + " for filestore ID = " + id);
					}

					filestores.put(id, new Filestore(id, name, Paths.get(pathString)));
				}
			}
		}
		bind(new TypeLiteral<Map<String, Filestore>>()
		{
		}).toInstance(filestores);

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
