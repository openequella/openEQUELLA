package com.tle.web.myresources;

import javax.inject.Inject;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ReadyToRespondListener;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.Label;

@TreeIndexed
public class MyResourcesSearchResults extends AbstractFreetextResultsSection<AbstractItemListEntry, SearchResultsModel>
	implements
		ReadyToRespondListener
{
	@PlugKey("menu")
	private static Label LABEL_TITLE;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Inject
	private ResetFiltersSection<?> resetFiltersSection;
	@Inject
	private StandardItemList itemList;

	@TreeLookup
	private MyResourcesSearchTypeSection searchTypeSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(resetFiltersSection, id);
	}

	@Override
	protected void registerItemList(SectionTree tree, String id)
	{
		tree.registerInnerSection(itemList, id);
	}

	@EventHandlerMethod
	public void subSearchChanged(SectionInfo info)
	{
		startSearch(info);
	}

	@Override
	public FreetextSearchEvent createSearchEvent(SectionInfo info)
	{
		MyResourcesSubSearch subSearch = searchTypeSection.getSearchType().getSelectedValue(info);
		return new FreetextSearchEvent(subSearch.createDefaultSearch(info), subSearch.createDefaultSearch(info));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public AbstractItemList getItemList(SectionInfo info)
	{
		AbstractItemList customItemList = searchTypeSection.getSearchType().getSelectedValue(info).getCustomItemList();
		if( customItemList != null )
		{
			return customItemList;
		}
		return itemList;
	}

	@Override
	public void readyToRespond(SectionInfo info, boolean redirect)
	{
		if( !redirect )
		{
			searchTypeSection.getSearchType().getSelectedValue(info).setupFilters(info);
		}
	}

	public Label getHeaderTitle()
	{
		return LABEL_TITLE;
	}
}
