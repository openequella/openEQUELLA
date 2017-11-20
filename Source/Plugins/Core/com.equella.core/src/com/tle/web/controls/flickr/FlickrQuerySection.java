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

package com.tle.web.controls.flickr;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.flickr.FlickrService;
import com.tle.web.controls.flickr.filter.FilterByCreativeCommonsLicencesSection;
import com.tle.web.controls.flickr.filter.FilterByFlickrInstitutionSection;
import com.tle.web.controls.flickr.filter.FilterByFlickrUserSection;
import com.tle.web.controls.flickr.filter.FlickrFilterByDateRangeSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class FlickrQuerySection
	extends
		AbstractResetFiltersQuerySection<FlickrQuerySection.FlickrQueryModel, FlickrSearchEvent>
{
	@PlugKey("query.search")
	private static Label SEARCH_LABEL;
	@PlugKey("query.text.any")
	private static String TEXT_ANY;
	@PlugKey("query.tags.only.any")
	private static String TAGS_ONLY_ANY;
	@PlugKey("query.tags.only.all")
	private static String TAGS_ONLY_ALL;

	@Component(name = "tort")
	private SingleSelectionList<String> textOrTagSelector;

	@TreeLookup
	private FlickrSearchResultsSection flickrSearchResultsSection;
	@TreeLookup
	private FilterByFlickrUserSection filterByFlickrUserSection;
	@TreeLookup
	private FilterByFlickrInstitutionSection filterByFlickrInstitutionSection;
	@TreeLookup
	private FilterByCreativeCommonsLicencesSection filterByCreativeCommonsLicencesSection;
	@TreeLookup
	private FlickrFilterByDateRangeSection filterByDateRangeSection;
	@Inject
	private FlickrService flickrService;

	@ViewFactory
	private FreemarkerFactory rrView;

	private ElementId dialogFooterId;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setTitle(new TextLabel(flickrService.getServiceName()));
		return rrView.createResult("flickrquery.ftl", this);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		List<String> textOrTagsList = new ArrayList<String>();
		textOrTagsList.add(CurrentLocale.get(TEXT_ANY));
		textOrTagsList.add(CurrentLocale.get(TAGS_ONLY_ANY));
		textOrTagsList.add(CurrentLocale.get(TAGS_ONLY_ALL));
		textOrTagSelector.setListModel(new SimpleHtmlListModel<String>(textOrTagsList));

		searchButton.setLabel(SEARCH_LABEL);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		final JSValidator validator = createValidator();
		final JSHandler restartSearch = new StatementHandler(new OverrideHandler(
			flickrSearchResultsSection.getResultsUpdater(tree, null, dialogFooterId.getElementId(null))));

		if( validator != null )
		{
			restartSearch.addValidator(validator);
		}
		searchButton.setClickHandler(restartSearch);

		textOrTagSelector.addChangeEventHandler(restartSearch);
	}

	@Override
	protected String getAjaxDiv()
	{
		return "searchform";
	}

	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event) throws Exception
	{
		boolean hasQueryFilter = !Check.isEmpty(event.getParams().getLicense());
		boolean hasKeywordFilter = !Check.isEmpty(event.getParams().getSearchRawText());
		boolean hasUserFilter = !Check.isEmpty(event.getParams().getUserRawText())
			|| !Check.isEmpty(event.getParams().getUserId());

		// Check the other sections manually: they may not have populated the
		// event object yet
		// any value entered in the user id box?
		String rawIdVal = filterByFlickrUserSection.getFlickrIdField().getValue(info);
		hasUserFilter |= !Check.isEmpty(rawIdVal);

		// any effective choice of Flickr institution?
		String validInstiFlickrId = filterByFlickrInstitutionSection.checkValidInstitution(info);
		hasUserFilter |= !Check.isEmpty(validInstiFlickrId);

		// Have we chosen any licence type?
		String licenceStr = filterByCreativeCommonsLicencesSection.getLicenceFilter(info);
		hasQueryFilter |= !Check.isEmpty(licenceStr);

		hasQueryFilter |= filterByDateRangeSection.getDatePrimary().isDateSet(info)
			|| filterByDateRangeSection.getDateSecondary().isDateSet(info);

		String textOrTag = this.textOrTagSelector.getSelectedValue(info);

		if( textOrTag != null && textOrTag.length() > 0 )
		{
			event.getParams().setTagsNotText(
				textOrTag.equals(CurrentLocale.get(TAGS_ONLY_ANY))
					|| textOrTag.equals(CurrentLocale.get(TAGS_ONLY_ALL)));
			event.getParams().setTagsAll(textOrTag.equals(CurrentLocale.get(TAGS_ONLY_ALL)));
		}

		// has the user made any selection (eg, institution) or entered text
		// into any field?
		boolean controlSelected = hasQueryFilter || hasKeywordFilter || hasUserFilter;
		String q = getParsedQuery(info);
		if( !controlSelected && Check.isEmpty(q) )
		{
			event.setInvalid(true);
			event.stopProcessing();
		}
		else
		{
			event.filterByTextQuery(q, true);
			event.setQueryFiltered(true);
		}

		event.setMoreThanKeywordFilter(hasQueryFilter || hasUserFilter);
	}

	/**
	 * Enable the rendering of the reset filters label in the query area
	 * 
	 * @return flickrSearchResultsSection.resetFiltersSection
	 */
	public FlickrSearchResultsSection getFlickrSearchResultsSection()
	{
		return flickrSearchResultsSection;
	}

	public SingleSelectionList<String> getTextOrTagSelector()
	{
		return textOrTagSelector;
	}

	/**
	 * Ideally we could compose a validator which tested for any of<br />
	 * <ul>
	 * <li>Text in the query field, or</li>
	 * <li>text in the flick used id field, or</li>
	 * <li>a selection of an institution, or</li>
	 * <li>selection of at least one licence type</li>
	 * </ul>
	 * 
	 * @return null
	 */
	protected JSValidator createValidator()
	{
		return null;
	}

	@Override
	public Class<FlickrQueryModel> getModelClass()
	{
		return FlickrQueryModel.class;
	}

	public static class FlickrQueryModel
	{
		private Label title;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}
	}

	public void setDialogFooterId(ElementId dialogFooterId)
	{
		this.dialogFooterId = dialogFooterId;
	}
}
