package com.tle.admin.harvester.tool;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.common.EntityPack;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

public class HarvesterProfileEditor extends BaseEntityEditor<HarvesterProfile>
{

	private final HarvesterProfileTool tool2;
	private HarvesterDetailsTab harvestDetailsTab;
	private HarvesterActionsTab harvestActionsTab;

	public HarvesterProfileEditor(HarvesterProfileTool tool, boolean readonly)
	{
		super(tool, readonly);
		tool2 = tool;
	}

	@Override
	public void load(EntityPack<HarvesterProfile> bentity, boolean isLoaded)
	{
		setType(bentity.getEntity().getType());
		super.load(bentity, isLoaded);
	}

	public void setType(String type)
	{
		harvestDetailsTab = new HarvesterDetailsTab();
		harvestActionsTab = new HarvesterActionsTab(this);

		harvestDetailsTab.setPlugin(tool2.getToolInstance(type));
		harvestActionsTab.setPlugin(tool2.getToolInstance(type));
	}

	@Override
	protected AbstractDetailsTab<HarvesterProfile> constructDetailsTab()
	{
		return harvestDetailsTab;
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.harvester.tool.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.harvester.tool.windowtitle"); //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.harvester.tool.name"); //$NON-NLS-1$
	}

	@Override
	protected List<? extends BaseEntityTab<HarvesterProfile>> getTabs()
	{
		List<BaseEntityTab<HarvesterProfile>> list = new ArrayList<BaseEntityTab<HarvesterProfile>>();
		list.add(harvestDetailsTab);
		list.add(harvestActionsTab);
		list.add(new AccessControlTab<HarvesterProfile>(Node.HARVESTER_PROFILE));
		return list;
	}
}
