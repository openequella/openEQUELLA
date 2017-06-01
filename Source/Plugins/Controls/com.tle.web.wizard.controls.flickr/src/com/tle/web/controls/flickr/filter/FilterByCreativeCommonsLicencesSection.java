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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.NameValueExtra;
import com.tle.common.Utils;
import com.tle.web.controls.flickr.FlickrSearchEvent;
import com.tle.web.controls.flickr.FlickrSearchResultsSection;
import com.tle.web.controls.flickr.FlickrUtils;
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
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class FilterByCreativeCommonsLicencesSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FlickrSearchEvent>
{
	public static final String NOT_FOR_DISPLAY = "not-for-display";
	public static final String NO_DATA = "no-data";

	@Component(name = "v")
	private MultiSelectionList<NameValueExtra> licenceList;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private FlickrSearchResultsSection searchResults;

	private final List<NameValueExtra> allLicenceValues = new ArrayList<NameValueExtra>();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		// Retrieve the list of licence names from file.
		List<NameValue> allLicenceValsFromFile = FlickrUtils.getNameValuesFromFile(
			FlickrUtils.CREATIVE_COMMONS_LICENCES_KEY + ".txt", FlickrUtils.CREATIVE_COMMONS_ELEMENTS_PER_LINE, true);

		// Extract only those licence records which are NOT recorded as
		// 'not-for-display'
		List<NameValueExtra> opts = new ArrayList<NameValueExtra>();
		for( NameValue nv : allLicenceValsFromFile )
		{
			NameValueExtra nve = (NameValueExtra) nv;
			if( nve.getName() != null && !nve.getName().equals(NOT_FOR_DISPLAY) )
			{
				opts.add(nve);
			}
			allLicenceValues.add(nve);
		}

		licenceList.setListModel(new SimpleHtmlListModel<NameValueExtra>(opts));

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		licenceList.setEventHandler(JSHandler.EVENT_CHANGE,
			new StatementHandler(searchResults.getRestartSearchHandler(tree)));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("filter/filterbycreativecommonslicence.ftl", this);
	}

	/**
	 * At present, there's no tie-in between selection of a flickr institution,
	 * and selection of a specific commons licence, so we are in effect enabling
	 * a user to search within a creative commons institution for a specifically
	 * licenced subset. (This may possibly be overkill ...)
	 */
	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event) throws Exception
	{
		String licenceFilter = getLicenceFilter(info);
		if( !Check.isEmpty(licenceFilter) )
		{
			event.getParams().setLicense(licenceFilter);
			event.setUserFiltered(true);
		}
	}

	/**
	 * Convenient public method to determine if a valid selection of any of the
	 * licence options has been made, and if so to identify the numerical key(s)
	 * thereof.
	 * 
	 * @param info
	 * @return a compounded, comma-separated string of licence numbers if
	 *         applicable, otherwise null.
	 */
	public String getLicenceFilter(SectionInfo info)
	{
		Set<String> licences = licenceList.getSelectedValuesAsStrings(info);
		return Check.isEmpty(licences) ? null : Utils.join(licences.toArray(), ",");
	}

	@Override
	public void reset(SectionInfo info)
	{
		licenceList.setSelectedStringValues(info, null);
	}

	public MultiSelectionList<NameValueExtra> getLicenceList()
	{
		return licenceList;
	}

	public String getDisplayLicenceForValue(String value)
	{
		for( NameValueExtra nve : allLicenceValues )
		{
			if( nve.getValue() != null && nve.getValue().equals(value) )
			{
				return nve.getExtra();
			}
		}
		return null;
	}

	public final List<NameValueExtra> getAllLicenceValues()
	{
		return allLicenceValues;
	}
}
