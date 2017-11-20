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

package com.tle.web.entity.services;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.entity.service.BaseEntityService;
import com.tle.core.i18n.service.LanguageService;
import com.tle.core.item.service.ItemService;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.schema.service.SchemaService;
import com.tle.core.security.PrivilegeTreeService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.core.workflow.service.WorkflowService;

public class GuiceModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		// just provider
	}

	@Provides
	@Named("remoteSchemaService")
	Object provideSchemaService(SchemaService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteLanguageService")
	Object provideLanguageService(LanguageService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteUserService")
	Object provideUserService(UserService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteWorkflowService")
	Object provideWorkflowService(WorkflowService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteItemdefinitionService")
	Object provideItemdefinitionService(ItemDefinitionService remote)
	{
		return remote;
	}

	@Provides
	@Named("remotePowerSearchService")
	Object providePowerSearchService(PowerSearchService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteItemService")
	Object provideItemService(ItemService remote)
	{
		return remote;
	}

	@Provides
	@Named("remotePrivilegeTreeService")
	Object provideItemService(PrivilegeTreeService remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteAclService")
	Object provideAclService(TLEAclManager remote)
	{
		return remote;
	}

	@Provides
	@Named("remoteBaseEntityService")
	Object provideBaseEntityService(BaseEntityService remote)
	{
		return remote;
	}
}
