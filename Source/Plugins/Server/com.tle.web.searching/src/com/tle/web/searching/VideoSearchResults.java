package com.tle.web.searching;

import javax.inject.Inject;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.system.SearchSettings;
import com.tle.common.search.DefaultSearch;
import com.tle.core.guice.Bind;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.searching.itemlist.VideoItemList;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractPrototypeSection;

@SuppressWarnings("nls")
@Bind
public class VideoSearchResults extends AbstractPrototypeSection<SearchResultsModel>
	implements
		StandardSearchResultType
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(VideoSearchResults.class);

	@Inject
	private VideoItemList videoItemList;
	@Inject
	private ConfigurationService configService;

	@Override
	public AbstractItemList<StandardItemListEntry, ?> getCustomItemList()
	{
		return videoItemList;
	}

	@Override
	public String getKey()
	{
		return helper.key("result.type.video");
	}

	@Override
	public String getValue()
	{
		return "video";
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
		tree.registerInnerSection(videoItemList, parentId);
	}

	@Override
	public void addResultTypeDefaultRestrictions(DefaultSearch defaultSearch)
	{
		defaultSearch.addMust(FreeTextQuery.FIELD_VIDEO_THUMB, "true");
	}

	@Override
	public boolean isDisabled()
	{
		SearchSettings properties = configService.getProperties(new SearchSettings());
		return properties.isSearchingDisableVideos();
	}
}
