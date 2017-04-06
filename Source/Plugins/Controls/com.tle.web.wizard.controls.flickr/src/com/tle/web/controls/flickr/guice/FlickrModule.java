package com.tle.web.controls.flickr.guice;

import com.tle.web.controls.flickr.FlickrLayoutSection;
import com.tle.web.controls.flickr.FlickrPagingSection;
import com.tle.web.controls.flickr.FlickrQuerySection;
import com.tle.web.controls.flickr.FlickrSearchResultsSection;
import com.tle.web.controls.flickr.filter.FilterByCreativeCommonsLicencesSection;
import com.tle.web.controls.flickr.filter.FilterByFlickrInstitutionSection;
import com.tle.web.controls.flickr.filter.FilterByFlickrUserSection;
import com.tle.web.controls.flickr.filter.FlickrFilterByDateRangeSection;
import com.tle.web.controls.flickr.sort.FlickrSortOptionsSection;
import com.tle.web.search.guice.AbstractSearchModule;
import com.tle.web.sections.SectionNode;

@SuppressWarnings("nls")
public class FlickrModule extends AbstractSearchModule
{
	@Override
	protected void doBinding(NodeProvider node)
	{
		bind(SectionNode.class).toProvider(node);
	}

	@Override
	protected NodeProvider getRootNode()
	{
		return node(FlickrLayoutSection.class);
	}

	@Override
	protected NodeProvider getQueryNode()
	{
		return node(FlickrQuerySection.class);
	}

	@Override
	protected NodeProvider getResultsNode()
	{
		return node(FlickrSearchResultsSection.class);
	}

	@Override
	protected NodeProvider getPagingNode()
	{
		return node(FlickrPagingSection.class);
	}

	@Override
	protected void addSearchResultsActions(NodeProvider node)
	{
		node.child(FlickrSortOptionsSection.class);
		node.child(FlickrFilterByDateRangeSection.class);
		node.child(FilterByCreativeCommonsLicencesSection.class);
		node.child(FilterByFlickrInstitutionSection.class);
		node.child(FilterByFlickrUserSection.class);
	}

	@Override
	protected String getTreeName()
	{
		return "flickrTree";
	}
}
