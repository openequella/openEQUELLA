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

package com.tle.web.searching.section;

import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.searching.SearchTab;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractSearchTabsSection<M extends AbstractSearchTabsSection.SearchTabsModel>
	extends
		AbstractPrototypeSection<M> implements HtmlRenderer
{
	@Inject
	private PluginTracker<SearchTab> tabsTracker;
	private List<SearchTab> searchTabs;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@AjaxFactory
	private AjaxGenerator ajax;
	@ViewFactory
	protected FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final SearchTabsModel model = getModel(context);

		final List<SearchTabModel> tabs = Lists.newArrayList();
		for( SearchTab tab : searchTabs )
		{
			final SectionRenderable renderable = SectionUtils.renderSection(context, tab);
			if( renderable != null )
			{
				final SearchTabModel tabModel = new SearchTabModel();
				tabModel.setId(tab.getId());
				HtmlLinkState link = new HtmlLinkState(events.getNamedHandler("onTabChange", tab.getId()));
				link.addClass(tab.getId());
				tabModel.setLink(link);
				tabModel.setRenderable(renderable);
				tabModel.setActive(tab.isActive());
				tabs.add(tabModel);
			}
		}
		if( tabs.size() <= 1 )
		{
			return null;
		}

		model.setTabs(tabs);
		return view.createResult("searching-tabs.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		searchTabs = Lists.newArrayList(tabsTracker.getNewBeanList());
		for( SearchTab tab : searchTabs )
		{
			if( tab.getClass() == getActiveSearchTabClass() )
			{
				tab.setActive();
			}
			tree.registerInnerSection(tab, id);
		}
	}

	@EventHandlerMethod
	public void onTabChange(SectionInfo info, String tabId)
	{
		for( SearchTab tab : searchTabs )
		{
			if( tab.getId().equals(tabId) )
			{
				// A bit ghetto
				SectionInfo fwd = tab.getForward(info);
				AbstractQuerySection thisQs = info.lookupSection(AbstractQuerySection.class);
				AbstractQuerySection qs = fwd.lookupSection(AbstractQuerySection.class);
				qs.setQuery(fwd, thisQs.getParsedQuery(info));
				info.forwardAsBookmark(fwd);
				return;
			}
		}
	}

	protected abstract Class<? extends SearchTab> getActiveSearchTabClass();

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SearchTabsModel();
	}

	@NonNullByDefault(false)
	public static class SearchTabModel
	{
		private String id;
		private boolean active;
		private HtmlLinkState link;
		private SectionRenderable renderable;

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public boolean isActive()
		{
			return active;
		}

		public void setActive(boolean active)
		{
			this.active = active;
		}

		public HtmlLinkState getLink()
		{
			return link;
		}

		public void setLink(HtmlLinkState link)
		{
			this.link = link;
		}

		public SectionRenderable getRenderable()
		{
			return renderable;
		}

		public void setRenderable(SectionRenderable renderable)
		{
			this.renderable = renderable;
		}
	}

	@NonNullByDefault(false)
	public static class SearchTabsModel
	{
		private List<SearchTabModel> tabs;

		public List<SearchTabModel> getTabs()
		{
			return tabs;
		}

		public void setTabs(List<SearchTabModel> tabs)
		{
			this.tabs = tabs;
		}
	}
}
