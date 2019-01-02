/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella.search;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection.SearchResultsActionsModel;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;

@TreeIndexed
@SuppressWarnings("nls")
public class SearchResultsActionsSection<RE extends AbstractSearchResultsEvent<RE>>
	extends
		AbstractPrototypeSection<SearchResultsActionsModel> implements HtmlRenderer, SearchResultsListener<RE>
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(SearchResultsActionsSection.class);
	private static final ExternallyDefinedFunction BLIND = new ExternallyDefinedFunction("showHideActions",
		JQueryUIEffects.BLIND, new IncludeFile(resources.url("scripts/resultactions.js")));

	public static final String AREA_SHARE = "share";
	public static final String AREA_SORT = "sort";
	public static final String AREA_FILTER = "filter";

	protected final List<SectionId> shareSections = new ArrayList<SectionId>();
	protected final List<SectionId> sortSections = new ArrayList<SectionId>();
	protected final List<SectionId> filterSections = new ArrayList<SectionId>();

	public static final String ACTIONS_AJAX_ID = "searchresults-actions";
	public static final String BUTTON_AJAX_ID = "actionbuttons";

	public static enum Showing
	{
		NONE, SHARE, SAVE, FILTER, SORT;
	}

	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	@PlugKey("results.actions.share")
	private Button share;
	@Component
	@PlugKey("actions.sort")
	private Button sort;
	@Component
	@PlugKey("actions.filter")
	private Button filter;

	private JSCallable showFunc;
	private JSCallable hideFunc;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		List<SectionId> children = tree.getChildIds(id);
		for( SectionId child : children )
		{
			String layout = tree.getLayout(child.getSectionId());
			if( AREA_SORT.equals(layout) )
			{
				sortSections.add(child);
			}
			else if( AREA_FILTER.equals(layout) )
			{
				filterSections.add(child);
			}
			else if( AREA_SHARE.equals(layout) )
			{
				shareSections.add(child);
			}
		}
		super.treeFinished(id, tree);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		showFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("show"), BLIND, ACTIONS_AJAX_ID,
			BUTTON_AJAX_ID);
		hideFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("hide"), BLIND, ACTIONS_AJAX_ID,
			BUTTON_AJAX_ID);

		tree.setAttribute(share, Showing.SHARE);
		tree.setAttribute(sort, Showing.SORT);
		tree.setAttribute(filter, Showing.FILTER);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SearchResultsActionsModel model = getModel(context);
		final Showing showing = model.getShowing();

		// TopicDisplay section can lookup this section, and set disabled where
		// appropriate
		if( model.isSearchDisabled() )
		{
			return null;
		}

		setupButton(sort, sortSections, showing, context);
		setupButton(filter, filterSections, showing, context);

		if( !model.isSaveAndShareDisabled() )
		{
			if( !CurrentUser.isGuest() )
			{
				setupButton(share, shareSections, showing, context);
			}
		}

		return viewFactory.createResult("resultactions.ftl", this);
	}

	private void setupButton(Button button, List<SectionId> childSections, Showing mode, RenderEventContext context)
	{
		if( !Check.isEmpty(childSections) )
		{
			Showing buttonMode = context.getTree().getAttribute(button);
			HtmlComponentState state = button.getState(context);
			if( mode.equals(buttonMode) )
			{
				button.setClickHandler(context, new OverrideHandler(hideFunc));
				state.setAttribute(Icon.class, Icon.UP);
				state.addClass("active");
				getModel(context).setChildSections(SectionUtils.renderSectionIds(context, childSections));
			}
			else
			{
				button.setClickHandler(context, new OverrideHandler(showFunc, buttonMode.toString()));
				state.setAttribute(Icon.class, Icon.DOWN);
			}
			getModel(context).addButton(button);
		}
	}

	public void processResults(SectionInfo info, RE results)
	{
		int filteredOut = results.getFilteredOut();
		HtmlComponentState state = filter.getState(info);
		if( filteredOut > 0 || filteredOut == Integer.MIN_VALUE )
		{
			state.addClass("filtered");
		}
	}

	@EventHandlerMethod
	public void show(SectionInfo info, String mode)
	{
		getModel(info).setShowing(parseShowing(mode));
	}

	@EventHandlerMethod
	public void hide(SectionInfo info)
	{
		getModel(info).setShowing(Showing.NONE);
	}

	private Showing parseShowing(String mode)
	{
		if( !Check.isEmpty(mode) )
		{
			try
			{
				return Showing.valueOf(mode);
			}
			catch( IllegalArgumentException ex )
			{
				// Not matching - that's fine
			}
		}
		return Showing.NONE;
	}

	public String[] getResetFilterAjaxIds()
	{
		return new String[]{ACTIONS_AJAX_ID, BUTTON_AJAX_ID};
	}

	public void disableSearch(SectionInfo info)
	{
		getModel(info).setSearchDisabled(true);
	}

	public void disableSaveAndShare(SectionInfo info)
	{
		getModel(info).setSaveAndShareDisabled(true);
	}

	@Override
	public Class<SearchResultsActionsModel> getModelClass()
	{
		return SearchResultsActionsModel.class;
	}

	public static class SearchResultsActionsModel
	{
		@Bookmarked(stateful = false)
		private Showing showing = Showing.NONE;

		private List<Button> buttons = Lists.newArrayList();
		private List<SectionRenderable> childSections;

		private boolean searchDisabled;
		private boolean saveAndShareDisabled;

		public Showing getShowing()
		{
			return showing;
		}

		public void setShowing(Showing showing)
		{
			this.showing = showing;
		}

		public List<SectionRenderable> getChildSections()
		{
			return childSections;
		}

		public void setChildSections(List<SectionRenderable> childSections)
		{
			this.childSections = childSections;
		}

		public boolean isSearchDisabled()
		{
			return searchDisabled;
		}

		public void setSearchDisabled(boolean searchDisabled)
		{
			this.searchDisabled = searchDisabled;
		}

		public boolean isSaveAndShareDisabled()
		{
			return saveAndShareDisabled;
		}

		public void setSaveAndShareDisabled(boolean saveAndShareDisabled)
		{
			this.saveAndShareDisabled = saveAndShareDisabled;
		}

		public List<Button> getButtons()
		{
			return buttons;
		}

		public void addButton(Button button)
		{
			this.buttons.add(button);
		}
	}
}
