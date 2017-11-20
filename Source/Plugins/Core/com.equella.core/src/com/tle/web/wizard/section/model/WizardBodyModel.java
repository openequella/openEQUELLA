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

package com.tle.web.wizard.section.model;

import java.util.List;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.generic.CachedData;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.SectionTab;

/**
 * @author jmaginnis
 */
public class WizardBodyModel
{
	@Bookmarked
	private int currentTab;

	// Tab/section to display
	private SectionId displaySection;

	private boolean tabNavigation = true;
	private boolean standardCommands = true;

	private List<SectionResult> sections;
	private List<Tab> tabs;
	private List<HtmlComponentState> majorActions;
	private List<HtmlComponentState> minorActions;
	private List<HtmlComponentState> moreActions;
	private List<SectionRenderable> additionalActions;

	private final CachedData<List<SectionTab>> displayableTabs = new CachedData<List<SectionTab>>();
	private final CachedData<List<WizardCommand>> displayableCommands = new CachedData<List<WizardCommand>>();

	public List<HtmlComponentState> getMajorActions()
	{
		return majorActions;
	}

	public void setMajorActions(List<HtmlComponentState> majorActions)
	{
		this.majorActions = majorActions;
	}

	public List<HtmlComponentState> getMinorActions()
	{
		return minorActions;
	}

	public void setMinorActions(List<HtmlComponentState> minorActions)
	{
		this.minorActions = minorActions;
	}

	public List<SectionResult> getSections()
	{
		return sections;
	}

	public void setSections(List<SectionResult> sections)
	{
		this.sections = sections;
	}

	public boolean isStandardCommands()
	{
		return standardCommands;
	}

	public void setStandardCommands(boolean standardCommands)
	{
		this.standardCommands = standardCommands;
	}

	public int getCurrentTab()
	{
		return currentTab;
	}

	public void setCurrentTab(int currentTab)
	{
		this.currentTab = currentTab;
	}

	public boolean isTabNavigation()
	{
		return tabNavigation;
	}

	public void setTabNavigation(boolean tabNavigation)
	{
		this.tabNavigation = tabNavigation;
	}

	public SectionId getDisplaySection()
	{
		return displaySection;
	}

	public void setDisplaySection(SectionId displaySection)
	{
		this.displaySection = displaySection;
	}

	public CachedData<List<SectionTab>> getDisplayableTabs()
	{
		return displayableTabs;
	}

	public CachedData<List<WizardCommand>> getDisplayableCommands()
	{
		return displayableCommands;
	}

	public List<Tab> getTabs()
	{
		return tabs;
	}

	public void setTabs(List<Tab> tabs)
	{
		this.tabs = tabs;
	}

	public static class Tab
	{
		private final boolean active;
		private final String id;
		private final HtmlComponentState content;

		public Tab(boolean active, HtmlComponentState content, String id)
		{
			this.active = active;
			this.content = content;
			this.id = id;
		}

		public boolean isActive()
		{
			return active;
		}

		public HtmlComponentState getContent()
		{
			return content;
		}

		public String getId()
		{
			return id;
		}
	}

	public List<SectionRenderable> getAdditionalActions()
	{
		return additionalActions;
	}

	public void setAdditionalActions(List<SectionRenderable> additionalActions)
	{
		this.additionalActions = additionalActions;
	}

	public List<HtmlComponentState> getMoreActions()
	{
		return moreActions;
	}

	public void setMoreActions(List<HtmlComponentState> moreActions)
	{
		this.moreActions = moreActions;
	}
}
