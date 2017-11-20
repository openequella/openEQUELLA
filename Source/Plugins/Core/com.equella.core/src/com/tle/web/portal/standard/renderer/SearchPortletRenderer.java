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

package com.tle.web.portal.standard.renderer;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.search.LiveItemSearch;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.service.AutoCompleteResult;
import com.tle.web.search.service.AutoCompleteService;
import com.tle.web.searching.section.SearchQuerySection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.libraries.JQueryTextFieldHint;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class SearchPortletRenderer extends PortletContentRenderer<Object>
{
	protected static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(SearchPortletRenderer.class);

	@PlugKey("search.textfield.hint")
	private static Label TEXTFIELD_HINT;

	@ViewFactory
	private FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private AutoCompleteService autoCompleteService;
	@Inject
	private SelectionService selectionService;

	@Component(name = "q", stateful = false)
	private TextField query;
	@Component(name = "s")
	private Button search;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		search.setClickHandler(events.getNamedHandler("doSearch"));
		query.setAutoCompleteCallback(ajax.getAjaxFunction("updateSearchTerms"));
		query.addTagProcessor(new JQueryTextFieldHint(TEXTFIELD_HINT, query));
	}

	@Override
	public SectionRenderable renderHtml(RenderEventContext context)
	{
		return view.createResult("searchportlet.ftl", context);
	}

	@AjaxMethod
	public AutoCompleteResult[] updateSearchTerms(SectionInfo info)
	{
		// Restrict to collection in selection session if set
		LiveItemSearch searchRequest = new LiveItemSearch();
		SelectionSession session = selectionService.getCurrentSession(info);
		if( session != null )
		{
			searchRequest.setCollectionUuids(session.getCollectionUuids());
		}
		return autoCompleteService.getAutoCompleteResults(searchRequest, query.getValue(info));
	}

	/**
	 * When in a session, viewable if all or any collections, or if any
	 * powerSearches or any RemoteRepositorySearches
	 */
	@Override
	public boolean canView(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		if( session == null )
		{
			return true;
		}
		return session.isAllCollections() || !Check.isEmpty(session.getCollectionUuids())
			|| session.isAllPowerSearches() || !Check.isEmpty(session.getPowerSearchIds())
			|| session.isAllRemoteRepositories() || !Check.isEmpty(session.getRemoteRepositoryIds());
	}

	@EventHandlerMethod
	public void doSearch(SectionInfo info)
	{
		SearchQuerySection.basicSearch(info, query.getValue(info));
	}

	public TextField getQuery()
	{
		return query;
	}

	public Button getSearchButton()
	{
		return search;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "psp";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
