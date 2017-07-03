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

package com.tle.web.search.base;

import static com.tle.web.sections.render.CssInclude.include;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.ListEntriesSection;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.search.filter.ResetFiltersParent;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.search.PagingSection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.WrappedEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryScrollTo;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractSearchResultsSection<LE extends ListEntry, SE extends AbstractSearchEvent<SE>, RE extends AbstractSearchResultsEvent<RE>, M extends AbstractSearchResultsSection.SearchResultsModel>
	extends
		AbstractPrototypeSection<M>
	implements HtmlRenderer, SearchResultsListener<RE>
{
	protected static final String DIV_SEARCHRESULTS = "searchresults";
	protected static final String DIV_SEARCHEADER = "searchresults-header-cont";
	protected static final String DIV_ACTION_BUTTONS = "actionbuttons";

	@PlugURL("js/resultsupdate.js")
	private static String URL_RESULTSJS;
	@PlugURL("css/searchresults.css")
	private static String URL_CSSINCLUDE;

	@PlugKey("search.generic.results")
	private static Label LABEL_RESULTS;
	@PlugKey("search.generic.noresults")
	private static Label LABEL_NORESULTS;
	@PlugKey("search.generic.noavailable")
	private static Label LABEL_NOAVAILABLE;
	@PlugKey("search.generic.invalid")
	private static Label LABEL_ERRORTITLE;

	@PlugKey("search.generic.help")
	private static String KEY_SUGGESTIONS;
	@PlugKey("search.generic.keywordhelp")
	private static Label LABEL_SUGGESTKEYWORD;
	@PlugKey("search.generic.filterhelp")
	private static Label LABEL_SUGGESTFILTER;

	@ViewFactory
	private FreemarkerFactory searchViewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@TreeLookup
	protected PagingSection<SE, RE> paging;
	@TreeLookup(mandatory = false)
	protected AbstractRootSearchSection<?> rootSearchSection;

	@Inject
	private SelectionService selectionService;

	private JSHandler restartSearchHandler;
	private JSCallable resultsEffect;
	private CssInclude cssInclude;
	private ParameterizedEvent restartEvent;
	private ParameterizedEvent checkBoxEvent;

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SearchResultsModel();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		resultsEffect = new PrependedParameterFunction(new ExternallyDefinedFunction("showNewResults",
			new IncludeFile(URL_RESULTSJS), AjaxGenerator.AJAX_LIBRARY, JQueryScrollTo.INCLUDE),
			AjaxGenerator.URL_SPINNER);
		restartEvent = events.getEventHandler("startSearch");
		checkBoxEvent = events.getEventHandler("toggleDisableCheckBox");
		cssInclude = include(URL_CSSINCLUDE).hasRtl().make();
	}

	private JSCallable getResultsUpdater(SectionTree tree, ParameterizedEvent event, boolean restart, String... ajaxIds)
	{
		if( !tree.isFinished() )
		{
			throw new Error("Only after treeFinished() can this be called");
		}
		List<String> ajaxList = new ArrayList<String>();
		ajaxList.addAll(getStandardAjaxDivs());
		Collections.addAll(ajaxList, ajaxIds);
		addAjaxUpdateDivs(tree, ajaxList);
		if( event == null && restart )
		{
			event = restartEvent;
		}
		else if( restart )
		{
			event = new RestartEvent(event);
		}
		return ajax.getAjaxUpdateDomFunction(tree, null, event, resultsEffect,
			ajaxList.toArray(new String[ajaxList.size()]));
	}

	protected List<String> getStandardAjaxDivs()
	{
		return Lists.newArrayList(DIV_SEARCHRESULTS, DIV_SEARCHEADER, DIV_ACTION_BUTTONS);
	}

	private JSCallable getCheckBoxUpdater(SectionTree tree)
	{
		if( !tree.isFinished() )
		{
			throw new Error("Only after treeFinished() can this be called");
		}

		return ajax.getAjaxUpdateDomFunction(tree, null, checkBoxEvent, resultsEffect, DIV_SEARCHRESULTS);
	}

	protected void addAjaxUpdateDivs(SectionTree tree, List<String> ajaxList)
	{
		ResetFiltersParent resetParent = tree.lookupSection(ResetFiltersParent.class, this);
		if( resetParent != null )
		{
			resetParent.addResetDiv(tree, ajaxList);
		}
	}

	public JSCallable getResultsUpdater(SectionTree tree, ParameterizedEvent event, String... ajaxIds)
	{
		return getResultsUpdater(tree, event, true, ajaxIds);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		paging.setChangeHandlers(getResultsUpdater(tree, null, false), getResultsUpdater(tree, null, true),
			getCheckBoxUpdater(tree));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		SearchResultsModel model = getModel(context);
		RE resultsEvent = null;
		SE searchEvent = createSearchEvent(context);
		if( searchEvent != null )
		{
			searchEvent.setLoggable(true);
			context.processEvent(searchEvent);
			resultsEvent = createResultsEvent(context, searchEvent);
		}

		if( resultsEvent != null && !resultsEvent.isErrored() )
		{
			context.processEvent(resultsEvent);
			ListEntriesSection<? extends LE> listEntrySection = getItemList(context);
			model.setItemList(listEntrySection);
			boolean resultsAvailable = paging.isResultsAvailable(context);

			if( !resultsAvailable )
			{
				int currentPage = paging.getPager().getCurrentPage(context);
				int lastPage = paging.getPager().getLastPage(context);

				if( currentPage > 1 && currentPage > lastPage )
				{
					paging.getPager().setCurrentPage(context, 1);
					searchEvent.setOffset(0);
					searchEvent.setCount(10);
					context.processEvent(searchEvent);
					resultsEvent = createResultsEvent(context, searchEvent);
					if( resultsEvent != null && !resultsEvent.isErrored() )
					{
						context.processEvent(resultsEvent);
						listEntrySection = getItemList(context);
						model.setItemList(listEntrySection);
						resultsAvailable = paging.isResultsAvailable(context);
					}
				}
			}
			model.setResultsAvailable(resultsAvailable);
			model.setResultsText(paging.getResultsText(context));
			if( !resultsAvailable )
			{
				model.setNoResultsTitle(getNoResultsTitle(context, searchEvent, resultsEvent));
				model.setSuggestions(getSuggestions(context, searchEvent, resultsEvent));
			}
		}
		else
		{
			model.setErrored(true);
			model.setErrorTitle(getErrorTitle(context, searchEvent, resultsEvent));
			model.setErrorMessageLabels(getErrorMessageLabels(context, searchEvent, resultsEvent));
		}

		SingleSelectionList resultTypeSelector = getResultTypeSelector(context);
		if( resultTypeSelector != null && resultTypeSelector.size(context) > 1 )
		{
			model.setShowResultSelection(true);
			model.setResultSelectionMenu(resultTypeSelector);
		}

		model.setShowResults(showResults(context, searchEvent, resultsEvent));
		model.setFooter(getFooter(context, searchEvent, resultsEvent));
		model.setActions(SectionUtils.renderChildren(context, this, new ResultListCollector(true)).getFirstResult());
		model.setResultsTitle(getDefaultResultsTitle(context, searchEvent, resultsEvent));
		model.setCourseSelectionSession(isCourseSelectionSession(context));
		if( rootSearchSection != null )
		{
			model.setCanonicalUrl(rootSearchSection.getPermanentUrl(context).getHref());
		}

		return searchViewFactory.createResult("results.ftl", this);
	}

	private boolean isCourseSelectionSession(SectionInfo info)
	{
		SelectionSession currentSession = selectionService.getCurrentSession(info);
		if( currentSession != null && currentSession.getLayout() == Layout.COURSE )
		{
			return true;
		}
		return false;
	}

	protected boolean showResults(RenderContext context, SE searchEvent, RE resultsEvent)
	{
		return true;
	}

	protected SectionRenderable getFooter(RenderContext context, SE searchEvent, RE resultsEvent)
	{
		return null;
	}

	protected Label getNoResultsTitle(SectionInfo info, SE searchEvent, RE resultsEvent)
	{
		if( !searchEvent.isFiltered() )
		{
			return LABEL_NOAVAILABLE;
		}
		return LABEL_NORESULTS;
	}

	protected Label getSuggestions(SectionInfo info, SE searchEvent, RE resultsEvent)
	{
		// check filtering
		Label suggest = null;
		if( searchEvent.isKeywordFiltered() )
		{
			suggest = LABEL_SUGGESTKEYWORD;
		}
		if( searchEvent.isUserFiltered() )
		{
			suggest = AppendedLabel.get(suggest, LABEL_SUGGESTFILTER);
		}
		if( suggest == null )
		{
			return null;
		}
		return new KeyLabel(KEY_SUGGESTIONS, suggest);
	}

	protected Label getErrorTitle(SectionInfo info, SE searchEvent, RE resultsEvent)
	{
		return LABEL_ERRORTITLE;
	}

	protected List<Label> getErrorMessageLabels(SectionInfo info, SE searchEvent, RE resultsEvent)
	{
		String errorMessage = resultsEvent != null ? resultsEvent.getErrorMessage() : null;
		if( errorMessage != null )
		{
			List<Label> errorLabels = new ArrayList<Label>();
			// can't be null
			for( String errorMsgPart : errorMessage.split("\n") )
			{
				errorLabels.add(new TextLabel(errorMsgPart));
			}
			return errorLabels;
		}

		return null;
	}

	protected Label getDefaultResultsTitle(SectionInfo info, SE searchEvent, RE resultsEvent)
	{
		return LABEL_RESULTS;
	}

	protected SingleSelectionList getResultTypeSelector(SectionInfo info)
	{
		return null;
	}

	protected abstract RE createResultsEvent(SectionInfo info, SE searchEvent);

	public abstract SE createSearchEvent(SectionInfo info);

	public abstract ListEntriesSection<LE> getItemList(SectionInfo info);

	public class RestartEvent implements ParameterizedEvent
	{
		private final ParameterizedEvent inner;

		public RestartEvent(ParameterizedEvent event)
		{
			this.inner = event;
		}

		@Override
		public SectionEvent<?> createEvent(SectionInfo info, String[] params)
		{
			return new WrappedEvent(inner.createEvent(info, params))
			{
				@Override
				protected void prefire(SectionId sectionId, SectionInfo info, EventListener listener)
				{
					startSearch(info);
				}
			};
		}

		@Override
		public String getEventId()
		{
			return inner.getEventId();
		}

		@Override
		public int getParameterCount()
		{
			return inner.getParameterCount();
		}

		@Override
		public boolean isPreventXsrf()
		{
			return inner.isPreventXsrf();
		}
	}

	public static class SearchResultsModel
	{
		private boolean resultsAvailable;
		private boolean errored;
		private boolean hasFilteredResults;
		private boolean showResults = true;
		private boolean courseSelectionSession;
		private ListEntriesSection<? extends ListEntry> itemList;
		private Label errorTitle;
		private List<Label> errorMessageLabels;
		private Label resultsText;
		private Label resultsTitle;
		private Label noResultsTitle;
		private Label suggestions;
		private SectionRenderable footer;
		private SectionRenderable actions;
		private boolean showResultTypeSelection;
		private SingleSelectionList resultSelectionMenu;
		private String canonicalUrl;

		public boolean isShowResultSelection()
		{
			return showResultTypeSelection;
		}

		public void setShowResultSelection(boolean showResultSelection)
		{
			this.showResultTypeSelection = showResultSelection;
		}

		public SingleSelectionList getResultSelectionMenu()
		{
			return resultSelectionMenu;
		}

		public void setResultSelectionMenu(SingleSelectionList resultSelectionMenu)
		{
			this.resultSelectionMenu = resultSelectionMenu;
		}

		public boolean isResultsAvailable()
		{
			return resultsAvailable;
		}

		public void setResultsAvailable(boolean resultsAvailable)
		{
			this.resultsAvailable = resultsAvailable;
		}

		public Label getResultsText()
		{
			return resultsText;
		}

		public void setResultsText(Label resultsText)
		{
			this.resultsText = resultsText;
		}

		public Label getResultsTitle()
		{
			return resultsTitle;
		}

		public void setResultsTitle(Label resultsTitle)
		{
			this.resultsTitle = resultsTitle;
		}

		public ListEntriesSection<? extends ListEntry> getItemList()
		{
			return itemList;
		}

		public void setItemList(ListEntriesSection<? extends ListEntry> itemList)
		{
			this.itemList = itemList;
		}

		public List<Label> getErrorMessageLabels()
		{
			return errorMessageLabels;
		}

		public void setErrorMessageLabels(List<Label> errorMessageLabels)
		{
			this.errorMessageLabels = errorMessageLabels;
		}

		public boolean isErrored()
		{
			return errored;
		}

		public void setErrored(boolean errored)
		{
			this.errored = errored;
		}

		public Label getSuggestions()
		{
			return suggestions;
		}

		public void setSuggestions(Label suggestions)
		{
			this.suggestions = suggestions;
		}

		public Label getNoResultsTitle()
		{
			return noResultsTitle;
		}

		public void setNoResultsTitle(Label noResultsTitle)
		{
			this.noResultsTitle = noResultsTitle;
		}

		public Label getErrorTitle()
		{
			return errorTitle;
		}

		public void setErrorTitle(Label errorTitle)
		{
			this.errorTitle = errorTitle;
		}

		public SectionRenderable getFooter()
		{
			return footer;
		}

		public void setFooter(SectionRenderable footer)
		{
			this.footer = footer;
		}

		public boolean isHasFilteredResults()
		{
			return hasFilteredResults;
		}

		public void setHasFilteredResults(boolean hasFilteredResults)
		{
			this.hasFilteredResults = hasFilteredResults;
		}

		public boolean isShowResults()
		{
			return showResults;
		}

		public void setShowResults(boolean showResults)
		{
			this.showResults = showResults;
		}

		public boolean isCourseSelectionSession()
		{
			return courseSelectionSession;
		}

		public void setCourseSelectionSession(boolean courseSelectionSession)
		{
			this.courseSelectionSession = courseSelectionSession;
		}

		public SectionRenderable getActions()
		{
			return actions;
		}

		public void setActions(SectionRenderable actions)
		{
			this.actions = actions;
		}

		public String getCanonicalUrl()
		{
			return canonicalUrl;
		}

		public void setCanonicalUrl(String canonicalUrl)
		{
			this.canonicalUrl = canonicalUrl;
		}
	}

	public PagingSection<SE, RE> getPaging()
	{
		return paging;
	}

	@EventHandlerMethod
	public void toggleDisableCheckBox(SectionInfo info)
	{
		paging.toggleDisabled(info);
	}

	@EventHandlerMethod
	public void startSearch(SectionInfo info)
	{
		paging.resetToFirst(info);
	}

	public JSHandler getRestartSearchHandler(SectionTree tree)
	{
		if( restartSearchHandler == null )
		{
			restartSearchHandler = new OverrideHandler(getResultsUpdater(tree, null, true));
		}
		return restartSearchHandler;
	}

	public CssInclude getCssInclude()
	{
		return cssInclude;
	}

	public SelectionService getSelectionService()
	{
		return selectionService;
	}
}