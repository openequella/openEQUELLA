/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
