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

package com.tle.core.freetext.guice;

import com.tle.core.config.guice.MandatoryConfigModule;
import com.tle.core.config.guice.PropertiesModule;

@SuppressWarnings("nls")
public class FreetextModule extends PropertiesModule
{
	@Override
	protected String getFilename()
	{
		return "/plugins/com.tle.core.freetext/optional.properties";
	}

	@Override
	protected void configure()
	{
		bindInt("freetextIndex.synchroiseMinutes");
		bindProp("freetextIndex.defaultOperator");
		bindBoolean("textExtracter.indexAttachments");
		bindBoolean("textExtracter.indexImsPackages");
		install(new FreetextMandatoryModule());
	}

	public static class FreetextMandatoryModule extends MandatoryConfigModule
	{
		@Override
		protected void configure()
		{
			bindFile("freetext.stopwords.file");
			bindFile("freetext.index.location");
		}
	}
}
