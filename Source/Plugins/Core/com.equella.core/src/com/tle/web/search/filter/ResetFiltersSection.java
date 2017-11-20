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

package com.tle.web.search.filter;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.core.guice.Type;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

@Bind(value = ResetFiltersSection.class, types = @Type(unknown = true, value = Object.class))
public class ResetFiltersSection<RE extends AbstractSearchResultsEvent<RE>>
	extends
		AbstractPrototypeSection<ResetFiltersSection.Model> implements SearchResultsListener<RE>, HtmlRenderer
{
	private static final String DIV_ID = "searchform-filteredout"; //$NON-NLS-1$
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@PlugKey("search.generic.filteredout")
	private static String KEY_FILTEREDOUT;
	@PlugKey("search.generic.filteredout1")
	private static String KEY_FILTEREDOUT1;
	@PlugKey("search.generic.filtersinplace")
	private static String KEY_FILTERS_IN_PLACE;
	@Component
	@PlugKey("search.generic.clearfilters")
	private Button resetButton;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResultsSection;
	@TreeLookup
	private SearchResultsActionsSection searchResultsActionsSection;

	@SuppressWarnings("nls")
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		resetButton.setClickHandler(searchResultsSection.getResultsUpdater(tree,
			events.getEventHandler("resetFilters"), searchResultsActionsSection.getResetFilterAjaxIds()));
	}

	public void addAjaxDiv(List<String> ajaxDivs)
	{
		ajaxDivs.add(DIV_ID);
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/resetfilters.ftl", this);
	}

	@EventHandlerMethod
	public void resetFilters(SectionInfo info)
	{
		info.processEvent(new ResetFiltersEvent());
	}

	/**
	 * In the typical case, this class applies to an internal search where the
	 * search can determine how many elements would be returned for an
	 * unfiltered search, and thus the filteredOut value, where non-zero, can be
	 * used to inform the user of the actual number excluded by the filters.
	 * Where an external search service (such a the Flickr service) is being
	 * queried, we content ourselves with setting the filteredOut value as
	 * Integer.MIN_VALUE, where a filter has been applied to the search (ie in
	 * addition to keyword search), to serve as a simple pseudo-boolean alerting
	 * the user to the fact that a filter is in place and therefore the result
	 * set ~may~ have been reduced by some unspecified number. Some SearchResult
	 * structures may return small negatives with an identical intent to
	 * returning zero (eg Hierarchy search), so -1 is an unsatisfactory choice
	 * for 'non-zero but unspecified', which is the intent of passing
	 * Integer.MIN_VALUE.
	 */
	@Override
	public void processResults(SectionInfo info, RE results)
	{
		int filteredOut = results.getFilteredOut();
		if( filteredOut > 0 )
		{
			getModel(info).setFilteredOutLabel(
				new KeyLabel(filteredOut == 1 ? KEY_FILTEREDOUT1 : KEY_FILTEREDOUT, filteredOut));
		}
		else if( filteredOut == Integer.MIN_VALUE )
		{
			getModel(info).setFilteredOutLabel(new KeyLabel(KEY_FILTERS_IN_PLACE));
		}
	}

	public Button getResetButton()
	{
		return resetButton;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private Label filteredOutLabel;

		public Label getFilteredOutLabel()
		{
			return filteredOutLabel;
		}

		public void setFilteredOutLabel(Label filteredOutLabel)
		{
			this.filteredOutLabel = filteredOutLabel;
		}
	}

}
