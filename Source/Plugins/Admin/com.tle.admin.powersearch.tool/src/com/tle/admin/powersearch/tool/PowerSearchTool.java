package com.tle.admin.powersearch.tool;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.powersearch.PowerSearchEditor;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemotePowerSearchService;

public class PowerSearchTool extends BaseEntityTool<PowerSearch>
{
	public PowerSearchTool() throws Exception
	{
		super(PowerSearch.class, RemotePowerSearchService.ENTITY_TYPE);
	}

	@Override
	protected RemoteAbstractEntityService<PowerSearch> getService(ClientService client)
	{
		return client.getService(RemotePowerSearchService.class);
	}

	@Override
	protected String getErrorPath()
	{
		return "powersearch";
	}

	@Override
	protected BaseEntityEditor<PowerSearch> createEditor(boolean readonly)
	{
		return new PowerSearchEditor(this, readonly);
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.gui.powersearchtool.title");
	}

	@Override
	protected PowerSearch process(PowerSearch powersearch)
	{
		List<Long> itemdefs = driver.getClientService().getService(RemotePowerSearchService.class)
			.enumerateItemdefIds(powersearch.getId());

		List<ItemDefinition> fullItemdefs = new ArrayList<ItemDefinition>();
		powersearch.setItemdefs(fullItemdefs);
		for( Long itemdef : itemdefs )
		{
			ItemDefinition definition = new ItemDefinition();
			definition.setId(itemdef);
			fullItemdefs.add(definition);
		}
		return powersearch;
	}
}
