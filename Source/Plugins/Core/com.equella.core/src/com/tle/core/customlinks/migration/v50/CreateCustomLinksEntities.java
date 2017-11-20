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

package com.tle.core.customlinks.migration.v50;

import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.AbstractPluginService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class CreateCustomLinksEntities extends AbstractCreateMigration
{
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(CreateCustomLinksEntities.class)+".";
	@Override
	protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper)
	{
		return new TablesOnlyFilter("custom_link");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{CustomLink.class, BaseEntity.class, LanguageBundle.class, Institution.class,
				LanguageString.class, BaseEntity.Attribute.class};
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(KEY_PFX+"customlinks.migration.title");
	}

}
