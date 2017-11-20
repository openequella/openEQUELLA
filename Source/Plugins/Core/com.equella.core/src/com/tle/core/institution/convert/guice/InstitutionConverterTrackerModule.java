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

package com.tle.core.institution.convert.guice;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.institution.convert.Converter;
import com.tle.core.institution.convert.ItemXmlMigrator;
import com.tle.core.institution.convert.Migrator;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.institution.convert.extension.InstitutionInfoInitialiser;

/**
 * @author Aaron
 *
 */
public class InstitutionConverterTrackerModule extends PluginTrackerModule
{
	@Override
	protected String getPluginId()
	{
		return "com.tle.core.institution.convert";
	}

	@Override
	protected void configure()
	{
		bindTracker(Migrator.class, "xmlmigration", "bean").setIdParam("id").orderByParameter("date", false, true);
		bindTracker(ItemXmlMigrator.class, "itemxmlmigration", "bean").setIdParam("id").orderByParameter("date", false,
			true);
		bindTracker(Converter.class, "converter", "class").setIdParam("id").orderByParameter("order");
		bindTracker(PostReadMigrator.class, "postreadmigration", "bean").setIdParam("id");
		bindTracker(InstitutionInfoInitialiser.class, "institutionInfoInitialiser", "bean");
	}
}
