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

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.controls.flickr.FlickrSearchEvent;
import com.tle.web.controls.flickr.FlickrSearchResultsSection;
import com.tle.web.controls.flickr.FlickrUtils;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.StylishDropDownRenderer;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.NotEqualsExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class FilterByFlickrInstitutionSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		ResetFiltersListener,
		SearchEventListener<FlickrSearchEvent>
{
	private static final String NO_SELECTION_PLACEHOLDER = "add.search.includeallinstis";

	@PlugKey(NO_SELECTION_PLACEHOLDER)
	private static String INCLUDE_ALL_INSTIS;

	@ResourceHelper
	private PluginResourceHelper RESOURCES;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private FilterByFlickrUserSection filterByFlickrUserSection;

	@TreeLookup
	private FlickrSearchResultsSection searchResults;

	@Component(name = "fins")
	private SingleSelectionList<String> flickrInstitutionSelector;

	private List<String[]> allCommonsInstitutions;

	private String anyInsti;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		// Retrieve the default list of institution names from file. First List
		// is the flickIds, second is the name (as will be presented to user)
		allCommonsInstitutions = FlickrUtils.getKeyStringsAsPair(FlickrUtils.INSTITUTION_KEY + ".txt", 2);

		// Set at the head of the list of institutions a catch-all string such
		// as "[Any]" (ie, no selection). Preserve in a member variable for
		// later reference.
		anyInsti = CurrentLocale.get(INCLUDE_ALL_INSTIS);
		if( anyInsti != null )
		{
			anyInsti = anyInsti.trim();
			if( anyInsti.length() > 0 )
			{
				// add a placeholder to keep indexes in parallel
				String[] anyInstPair = new String[2];
				anyInstPair[0] = INCLUDE_ALL_INSTIS;
				anyInstPair[1] = anyInsti;
				allCommonsInstitutions.add(0, anyInstPair);
			}
		}
		List<String> displayStrings = new ArrayList<String>();
		for( String[] line : allCommonsInstitutions )
		{
			displayStrings.add(line[1]);
		}
		flickrInstitutionSelector.setListModel(new SimpleHtmlListModel<String>(displayStrings));

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	/**
	 * Provide for the disabling of the Flickr-user field if a Flickr
	 * institution has been selected. Institutions are simply glorified Flickr
	 * users, so it makes no sense to attempt to filter on any (presumably,
	 * other) Flickr user as well. Note that field is not actually emptied: the
	 * Flick User section will ignore any content in the field if there is a
	 * valid institution currently selected. Also note that the disabler has to
	 * be set here in TreeFinished because the TreeLookup returns null during
	 * registered(...).
	 * 
	 * @see com.tle.web.sections.generic.AbstractSection#treeFinished(java.lang.String,
	 *      com.tle.web.sections.SectionTree)
	 */
	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		TextField externalFlickrUserField = filterByFlickrUserSection.getFlickrIdField();
		// SingleSelectionList<NameValue> licenceList =
		// filterByCreativeCommonsLicencesSection.getLicenceList();
		// Disable (both?) the Flick user text field (and the creative commons
		// licence list?) if an institution has been picked.
		// Also perform a new search immediately on institution selector change
		flickrInstitutionSelector.addEventStatements(JSHandler.EVENT_CHANGE,
			new OverrideHandler(externalFlickrUserField.createDisableFunction(), new NotEqualsExpression(
				flickrInstitutionSelector.createGetExpression(), new StringExpression(getAnyInsti()))),
			// new OverrideHandler(
			// licenceList.createDisableFunction(),
			// new NotEqualsExpression(
			// flickrInstitutionSelector.createGetExpression(),
			// new StringExpression(getAnyInsti()))),
			searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		flickrInstitutionSelector.getState(context).setAttribute(StylishDropDownRenderer.KEY_ALWAYS_DISPLAY_UP, true);

		return viewFactory.createResult("filter/filterbyflickrinstitution.ftl", this);
	}

	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event)
	{
		String validInstiFlickrId = checkValidInstitution(info);
		if( validInstiFlickrId != null )
		{
			event.getParams().setUserId(validInstiFlickrId);
			event.setUserFiltered(true);
		}
	}

	/**
	 * Convenient public method to determine if there is a selection from the
	 * Institutions list, and if so is it a selection of an actual institution
	 * (and not a mere placeholder in the list for "[Any]" institution).
	 * 
	 * @param info
	 * @return the Flickr-Id of the selected institution if applicable,
	 *         otherwise null
	 */
	public String checkValidInstitution(SectionInfo info)
	{
		String selectedInsti = flickrInstitutionSelector.getSelectedValue(info);
		String validInstiFlickrId = null;
		if( selectedInsti != null && selectedInsti.length() > 0 )
		{
			for( int i = 0; i < allCommonsInstitutions.size(); ++i )
			{
				// Look for a match in the stored list of institution names, but
				// ignore the value if the corresponding
				// flickrID is a mere placeholder (which indicates 'no
				// selection')
				if( allCommonsInstitutions.get(i)[1].equals(selectedInsti) )
				{
					String correspondingFlickrId = allCommonsInstitutions.get(i)[0];
					if( !correspondingFlickrId.equals(RESOURCES.key(NO_SELECTION_PLACEHOLDER)) )
					{
						validInstiFlickrId = correspondingFlickrId;
						break; // from for-loop
					}
				}
			}
		}
		return validInstiFlickrId;
	}

	/**
	 * @see com.tle.web.search.filter.ResetFiltersListener#reset(com.tle.web.sections.SectionInfo)
	 */
	@Override
	public void reset(SectionInfo info)
	{
		flickrInstitutionSelector.setSelectedValue(info, null);
	}

	/**
	 * As a last ditch safety, return the presumed English-language default.
	 * 
	 * @return the anyInsti string that should have been set in registered()
	 */
	private String getAnyInsti()
	{
		return anyInsti == null ? "[Any]" : anyInsti;
	}

	public SingleSelectionList<String> getFlickrInstitutionSelector()
	{
		return flickrInstitutionSelector;
	}
}