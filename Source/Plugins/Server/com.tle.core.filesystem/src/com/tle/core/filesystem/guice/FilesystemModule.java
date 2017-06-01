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
