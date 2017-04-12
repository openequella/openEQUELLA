package com.tle.web.cloud.guice;

import com.tle.web.cloud.search.filters.CloudFilterByEducationLevelSection;
import com.tle.web.cloud.search.filters.CloudFilterByLanguageSection;
import com.tle.web.cloud.search.filters.CloudFilterByLicenceSection;
import com.tle.web.cloud.search.filters.CloudFilterByMimeTypeSection;
import com.tle.web.cloud.search.filters.CloudFilterByPublisherSection;
import com.tle.web.cloud.search.section.CloudPagingSection;
import com.tle.web.cloud.search.section.CloudQuerySection;
import com.tle.web.cloud.search.section.CloudSearchResultsSection;
import com.tle.web.cloud.search.section.CloudSearchTabsSection;
import com.tle.web.cloud.search.section.CloudShareSearchQuerySection;
import com.tle.web.cloud.search.section.RootCloudSearchSection;
import com.tle.web.cloud.search.sort.CloudSortOptionsSection;
import com.tle.web.search.actions.StandardFavouriteSearchAction;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.selection.section.SelectionSummarySection;

@SuppressWarnings("nls")
public class CloudSearchModule extends AbstractSearchModule
{
	@Override
	protected NodeProvider getRootNode()
	{
		return node(RootCloudSearchSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(CloudQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(CloudSearchResultsSection.class);
	}

	@Override
	protected void addActions(NodeProvider node)
	{
		node.child(SelectionSummarySection.class);
	}

	@Override
	protected void addQueryActions(NodeProvider node)
	{
		node.child(StandardFavouriteSearchAction.class);
		node.child(CloudSearchTabsSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(CloudShareSearchQuerySection.class);
		node.child(CloudSortOptionsSection.class);
		node.child(CloudFilterByLanguageSection.class);
		node.child(CloudFilterByLicenceSection.class);
		node.child(CloudFilterByPublisherSection.class);
		node.child(CloudFilterByEducationLevelSection.class);
		node.child(CloudFilterByMimeTypeSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return new NodeProvider(CloudPagingSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "/cloudsearch";
	}
}
