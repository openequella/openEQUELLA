/*
 * Created on May 10, 2005
 */
package com.tle.admin.dynacollection;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class DynaCollectionEditor extends BaseEntityEditor<DynaCollection>
{
	public DynaCollectionEditor(BaseEntityTool<DynaCollection> tool, boolean readonly)
	{
		super(tool, readonly);
	}

	@Override
	protected AbstractDetailsTab<DynaCollection> constructDetailsTab()
	{
		return new DetailsTab();
	}

	@Override
	protected List<BaseEntityTab<DynaCollection>> getTabs()
	{
		EntityCache cache = new EntityCache(clientService);

		List<BaseEntityTab<DynaCollection>> tabs = new ArrayList<BaseEntityTab<DynaCollection>>();
		tabs.add((DetailsTab) detailsTab);
		tabs.add(new FilterTab(cache));
		tabs.add(new VirtualisationTab());
		tabs.add(new AccessControlTab<DynaCollection>(Node.DYNA_COLLECTION));
		return tabs;
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.entityname"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.windowtitle"); //$NON-NLS-1$
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.dynacollection.entityname"); //$NON-NLS-1$
	}
}
