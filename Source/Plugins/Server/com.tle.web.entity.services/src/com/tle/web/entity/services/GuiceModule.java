package com.tle.web.entity.services;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.tle.core.powersearch.PowerSearchService;
import com.tle.core.schema.SchemaService;
import com.tle.core.security.PrivilegeTreeService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.entity.BaseEntityService;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.services.item.ItemService;
import com.tle.core.services.language.LanguageService;
import com.tle.core.services.user.UserService;

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
