/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
