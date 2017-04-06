package com.tle.admin.fedsearch;

import java.util.ArrayList;
import java.util.List;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.fedsearch.tool.SearchTool;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.EntityPack;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;

/**
 * @author Nicholas Read
 */
public class SearchManagement extends BaseEntityEditor<FederatedSearch>
{
	private final SearchTool tool2;
	private SearchTab search;

	public SearchManagement(SearchTool tool, boolean readonly)
	{
		super(tool, readonly);
		tool2 = tool;
	}

	@Override
	public void load(EntityPack<FederatedSearch> bentity, boolean isLoaded)
	{
		search = new SearchTab();
		search.setPlugin(tool2.getToolInstance(bentity.getEntity().getType()));

		super.load(bentity, isLoaded);
	}

	@Override
	protected AbstractDetailsTab<FederatedSearch> constructDetailsTab()
	{
		return search;
	}

	@Override
	protected String getEntityName()
	{
		return CurrentLocale.get("com.tle.admin.search.searchmanagement.name"); //$NON-NLS-1$
	}

	@Override
	protected String getWindowTitle()
	{
		return CurrentLocale.get("com.tle.admin.search.searchmanagement.title"); //$NON-NLS-1$
	}

	@Override
	protected List<? extends BaseEntityTab<FederatedSearch>> getTabs()
	{
		List<BaseEntityTab<FederatedSearch>> list = new ArrayList<BaseEntityTab<FederatedSearch>>();
		list.add(search);
		list.add(new AccessControlTab<FederatedSearch>(Node.FEDERATED_SEARCH));
		return list;
	}

	@Override
	public String getDocumentName()
	{
		return CurrentLocale.get("com.tle.admin.search.searchmanagement.name"); //$NON-NLS-1$
	}
}
