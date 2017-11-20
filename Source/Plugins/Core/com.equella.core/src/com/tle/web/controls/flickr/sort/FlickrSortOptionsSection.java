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

package com.tle.web.controls.flickr.sort;

import java.util.Arrays;

import com.flickr4java.flickr.photos.SearchParameters;
import com.tle.common.Pair;
import com.tle.web.controls.flickr.FlickrSearchEvent;
import com.tle.web.controls.flickr.FlickrSearchResultsSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.EqualityExpression;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class FlickrSortOptionsSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<FlickrSearchEvent>
{
	protected static final String RELEVANCE_KEY = "sort.relevance";
	protected static final String INTERESTINGNESS_DESC_KEY = "sort.interestingness.desc";
	protected static final String DATE_TAKEN_DESC_KEY = "sort.date.taken.desc";
	protected static final String DATE_POSTED_DESC_KEY = "sort.date.posted.desc";

	@ViewFactory(fixed = true)
	private FreemarkerFactory viewFactory;

	@PlugKey("sort.results.reverse")
	private static Label LABEL_REVERSE;
	@PlugKey("")
	private static String KEY_SORTPFX;

	@Component(name = "so", parameter = "sort", supported = true)
	private SingleSelectionList<FlickrSortType> sortOptions;

	@Component(name = "r", parameter = "rs", supported = true)
	protected Checkbox reverse;

	@TreeLookup
	private FlickrSearchResultsSection searchResults;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sortOptions.setListModel(new SimpleHtmlListModel<FlickrSortType>(Arrays.asList(FlickrSortType.values()))
		{
			@Override
			protected Option<FlickrSortType> convertToOption(FlickrSortType obj)
			{
				return new KeyOption<FlickrSortOptionsSection.FlickrSortType>(KEY_SORTPFX + obj.getField(), obj
					.getDirectionConstant().toString(), obj);
			}
		});
		sortOptions.setAlwaysSelect(true);
		reverse.setLabel(LABEL_REVERSE);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SORT);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		SimpleFunction disFunc = new SimpleFunction("disRev", reverse, new FunctionCallStatement(
			reverse.createDisableFunction(), new EqualityExpression(sortOptions.createGetExpression(),
				new StringExpression(FlickrSortType.RELEVANCE.getDirectionConstant().toString()))));
		sortOptions.addEventStatements(JSHandler.EVENT_CHANGE, searchResults.getRestartSearchHandler(tree),
			new FunctionCallStatement(disFunc));
		sortOptions.addEventStatements(JSHandler.EVENT_READY, new FunctionCallStatement(disFunc));
		reverse.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null)));
	}

	/**
	 * Because RELEVANCE has no reversible sort option in the Flickr service,
	 * disable the reverse button when RELEVANCE is the current selection.
	 */
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("sort/flickrsortoptions.ftl", context);
	}

	public SingleSelectionList<FlickrSortType> getSortOptions()
	{
		return sortOptions;
	}

	/**
	 * FlickrSortType maps our selected value to an index, but we also need to
	 * see if the 'Reverse order' checkbox has been selected, in which case our
	 * intended selected value is the opposite pair in the FlickrSortType list
	 * (provided of course that an opposite String actually exists - where it
	 * doesn't, reverse has no effect).
	 * 
	 * @see com.tle.web.sections.equella.search.event.SearchEventListener#prepareSearch(com.tle.web.sections.SectionInfo,
	 *      com.tle.web.sections.equella.search.event.AbstractSearchEvent)
	 */
	@Override
	public void prepareSearch(SectionInfo info, FlickrSearchEvent event) throws Exception
	{
		FlickrSortType selectedOption = sortOptions.getSelectedValue(info);
		int sortInt = selectedOption.getDirectionConstant();
		if( reverse.isChecked(info) )
		{
			sortInt = selectedOption.getReverseConstant();
		}
		event.setSort(sortInt);
	}

	public Checkbox getReverse()
	{
		return reverse;
	}

	public enum FlickrSortType
	{
		/**
		 * The flickr values translate into static SearchParameters constant
		 * integers as follows: DATE_POSTED_DESC = 0;<br />
		 * DATE_POSTED_ASC = 1;<br />
		 * DATE_TAKEN_DESC = 2;<br />
		 * DATE_TAKEN_ASC = 3;<br />
		 * INTERESTINGNESS_DESC = 4;<br />
		 * INTERESTINGNESS_ASC = 5;<br />
		 * RELEVANCE = 6;
		 */
		RELEVANCE(RELEVANCE_KEY, new Pair<Integer, Integer>(SearchParameters.RELEVANCE, null)), // there
																								// is
																								// no
																								// ascending
																								// option
																								// for
																								// RELEVANCE
		INTERESTINGNESS_DESC(INTERESTINGNESS_DESC_KEY, new Pair<Integer, Integer>(
			SearchParameters.INTERESTINGNESS_DESC, SearchParameters.INTERESTINGNESS_ASC)), DATE_TAKEN_DESC(
			DATE_TAKEN_DESC_KEY, new Pair<Integer, Integer>(SearchParameters.DATE_TAKEN_DESC,
				SearchParameters.DATE_TAKEN_ASC)), DATE_POSTED_DESC(DATE_POSTED_DESC_KEY, new Pair<Integer, Integer>(
			SearchParameters.DATE_POSTED_DESC, SearchParameters.DATE_POSTED_ASC));

		private String field;
		private Pair<Integer, Integer> flickrSortConstants;

		private FlickrSortType(String field, Pair<Integer, Integer> flickrSortConstants)
		{
			this.field = field;
			this.flickrSortConstants = flickrSortConstants;
		}

		public String getField()
		{
			return field;
		}

		public Pair<Integer, Integer> getFlickrSortConstants()
		{
			return flickrSortConstants;
		}

		public Integer getDirectionConstant()
		{
			return flickrSortConstants.getFirst();
		}

		public Integer getReverseConstant()
		{
			return flickrSortConstants.getSecond();
		}
	}

	public LabelTagRenderer getLabelTag()
	{
		return new LabelTagRenderer(sortOptions, null, null);
	}
}
