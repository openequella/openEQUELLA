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

package com.tle.web.controls.flickr.filter;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.controls.flickr.FlickrSearchEvent;
import com.tle.web.controls.flickr.FlickrSearchResultsSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author larry
 */
@NonNullByDefault
@SuppressWarnings("nls")
public class FilterByFlickrUserSection
	extends
		AbstractPrototypeSection<FilterByFlickrUserSection.FilterByFlickrUserModel>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FlickrSearchEvent>
{
	@TreeLookup
	private FilterByFlickrInstitutionSection filterByFlickrInstitutionSection;
	@TreeLookup
	private FlickrSearchResultsSection searchResults;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "fif", parameter = "p", supported = true)
	private TextField flickrIdField;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	/**
	 */
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		// new search immediately on user field change
		flickrIdField.setEventHandler(JSHandler.EVENT_CHANGE, searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public void reset(SectionInfo info)
	{
		flickrIdField.setValue(info, null);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/filterbyflickruser.ftl", this);
	}

	/**
	 * Before examining the contents of the Flickr-user text box, be sure
	 * there's no active selection in the institution drop-down (institutions
	 * are glorified Flickr users - the institution drop-down and the
	 * flickr-user text box are mutually exclusive). The operation of the drop
	 * down is that the flickr-user box will be disabled but not necessarily
	 * cleared. The plain non-blank text entered by the user is set as the value
	 * of the UserRawText in the FlickrSearchParameters. The flickrService will
	 * further evaluate this field to determine if the raw string is to be
	 * considered as an email address, a flickr username or a flickr userid.
	 * 
	 * @param info SectionInfo
	 * @param event the FlickrSearchEvent to be populated
	 */
	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event)
	{
		String selectedInsti = filterByFlickrInstitutionSection.checkValidInstitution(info);
		if( selectedInsti != null )
		{
			return;
		}

		String rawUserVal = flickrIdField.getValue(info);
		if( rawUserVal != null )
		{
			rawUserVal = rawUserVal.trim();
			if( rawUserVal.length() > 0 )
			{
				event.getParams().setUserRawText(rawUserVal);
				event.setUserFiltered(true);
			}
		}
	}

	public TextField getFlickrIdField()
	{
		return flickrIdField;
	}

	@Override
	public Class<FilterByFlickrUserModel> getModelClass()
	{
		return FilterByFlickrUserModel.class;
	}

	public static class FilterByFlickrUserModel
	{
		private boolean showClearLink;

		public boolean isShowClearLink()
		{
			return showClearLink;
		}

		public void setShowClearLink(boolean showClearLink)
		{
			this.showClearLink = showClearLink;
		}
	}
}
