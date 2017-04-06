package com.tle.mycontent.web.selection;

import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.mycontent.MyContentConstants;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.search.base.AbstractItemListResultSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.filter.SelectionAllowedMimeTypes;

public class MyContentSelectResults extends AbstractItemListResultSection<SearchResultsModel>
{
	@Inject
	private SelectionAllowedHandlers allowedHandlers;
	@Inject
	private SelectionAllowedMimeTypes allowedMimeTypes;

	@Override
	protected DefaultSearch createDefaultSearch(SectionInfo info)
	{
		MyResourcesSearch search = new MyResourcesSearch();
		search.setItemStatuses(ItemStatus.PERSONAL);
		Set<String> handlers = allowedHandlers.get(info);
		if( !Check.isEmpty(handlers) )
		{
			search.addMust('/' + MyContentConstants.CONTENT_TYPE_NODE, handlers);
		}
		search.setMimeTypes(allowedMimeTypes.get(info));
		return search;
	}

	@Override
	protected void customiseSettings(SectionInfo info, ListSettings<StandardItemListEntry> settings)
	{
		settings.setEditable(false);
	}
}
