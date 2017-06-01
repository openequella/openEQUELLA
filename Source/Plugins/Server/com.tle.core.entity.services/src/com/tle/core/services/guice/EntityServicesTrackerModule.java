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

package com.tle.core.services.guice;

import com.google.inject.TypeLiteral;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.item.AbstractHelper;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.institution.migration.ItemXmlMigrator;
import com.tle.core.institution.migration.Migrator;
import com.tle.core.services.entity.AbstractEntityService;

public class EntityServicesTrackerModule extends PluginTrackerModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		TypeLiteral<AbstractEntityService<?, BaseEntity>> entityService = new TypeLiteral<AbstractEntityService<?, BaseEntity>>()
		{
			// empty
		};
		bindTracker(entityService.getType(), "entityService", "serviceClass").orderByParameter("order");
		bindTracker(AbstractHelper.class, "itemHelpers", "bean").orderByParameter("order");

		bindTracker(Migrator.class, "xmlmigration", "bean").setIdParam("id").orderByParameter("date", false, true);
		bindTracker(ItemXmlMigrator.class, "itemxmlmigration", "bean").setIdParam("id").orderByParameter("date", false,
			true);
	}
}
