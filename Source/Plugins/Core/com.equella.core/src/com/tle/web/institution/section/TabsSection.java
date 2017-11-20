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

package com.tle.web.institution.section;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.institution.InstitutionSection.TabDisplay;
import com.tle.web.institution.Tab;
import com.tle.web.institution.Tabable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.Bootstrap;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@SuppressWarnings("nls")
public class TabsSection extends AbstractPrototypeSection<TabsSection.TabModel> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@TreeLookup
	private InstitutionSection institutionSection;

	@Inject
	private ReceiptService receiptService;

	@Override
	public Class<TabModel> getModelClass()
	{
		return TabModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "tabs";
	}

	public void changeTab(SectionInfo info, String tabId)
	{
		TabModel model = getModel(info);
		String tab = model.getTab();
		if( tab != null && !tab.equals(tabId) )
		{
			Tabable tabLost = getSectionForTab(info, tab);
			if( tabLost != null )
			{
				tabLost.lostFocus(info, tab);
			}
		}
		model.setTab(tabId);
		if( tabId != null )
		{
			getSectionForTab(info, tabId).gainedFocus(info, tabId);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final TabModel model = getModel(context);
		final List<Tab> tabs = getAllTabs(context);

		String currentTab = model.getTab();
		if( currentTab == null || !getTabMap(context).containsKey(currentTab) )
		{
			for( Tab tab : tabs )
			{
				if( tab.shouldDefault(context) )
				{
					currentTab = tab.getId();
					break;
				}
			}
			changeTab(context, currentTab);
		}

		final List<TabDisplay> tabDisplays = new ArrayList<TabDisplay>();
		for( Tab tab : tabs )
		{
			TabDisplay tabDisplay = new TabDisplay();
			String tid = tab.getId();
			// Not possible
			if( currentTab == null )
			{
				throw new Error("Impressive");
			}
			boolean selected = currentTab.equals(tid);
			tabDisplay.setClazz(selected ? " selected" : "");
			HtmlLinkState link = new HtmlLinkState();
			if( !selected )
			{
				link.setClickHandler(tab.getClickHandler());
			}
			link.setDisabled(selected);
			link.setLabel(tab.getName());
			tabDisplay.setLink(link);
			tabDisplays.add(tabDisplay);
		}
		model.setTabs(tabDisplays);
		model.setSelectedTab(SectionUtils.renderSectionResult(context, getSectionForTab(context, currentTab)));

		Label receipt = receiptService.getReceipt();
		if( receipt != null )
		{
			context.preRender(Bootstrap.PRERENDER);
			model.setReceipt(receipt.getText());
		}

		return viewFactory.createTemplateResult("tabs.ftl", context);
	}

	public Tabable getSectionForTab(SectionInfo info, String currentTab)
	{
		return getTabMap(info).get(currentTab);
	}

	private Map<String, Tabable> getTabMap(SectionInfo info)
	{
		getAllTabs(info);
		return getModel(info).getTabMap();
	}

	public List<Tab> getAllTabs(SectionInfo info)
	{
		TabModel model = getModel(info);
		List<Tab> allTabs = model.getAllTabs();
		if( allTabs == null )
		{
			allTabs = new ArrayList<Tab>();
			Map<String, Tabable> tabMap = new HashMap<String, Tabable>();
			List<Tabable> tabables = institutionSection.getTabInterfaces().getAllImplementors(info);
			for( Tabable tabable : tabables )
			{
				List<Tab> tabs = tabable.getTabs(info);
				for( Tab tab : tabs )
				{
					tabMap.put(tab.getId(), tabable);
				}
				allTabs.addAll(tabs);
			}
			model.setTabMap(tabMap);
		}
		return allTabs;
	}

	public static class TabModel
	{
		@Bookmarked
		private String tab;

		private SectionResult selectedTab;

		private Map<String, Tabable> tabMap;
		private List<Tab> allTabs;
		private List<TabDisplay> tabs;

		private String receipt;

		public String getTab()
		{
			return tab;
		}

		public void setTab(String tab)
		{
			this.tab = tab;
		}

		public SectionResult getSelectedTab()
		{
			return selectedTab;
		}

		public void setSelectedTab(SectionResult selectedTab)
		{
			this.selectedTab = selectedTab;
		}

		public Map<String, Tabable> getTabMap()
		{
			return tabMap;
		}

		public void setTabMap(Map<String, Tabable> tabMap)
		{
			this.tabMap = tabMap;
		}

		public List<Tab> getAllTabs()
		{
			return allTabs;
		}

		public void setAllTabs(List<Tab> allTabs)
		{
			this.allTabs = allTabs;
		}

		public List<TabDisplay> getTabs()
		{
			return tabs;
		}

		public void setTabs(List<TabDisplay> tabs)
		{
			this.tabs = tabs;
		}

		public String getReceipt()
		{
			return receipt;
		}

		public void setReceipt(String receipt)
		{
			this.receipt = receipt;
		}
	}
}
