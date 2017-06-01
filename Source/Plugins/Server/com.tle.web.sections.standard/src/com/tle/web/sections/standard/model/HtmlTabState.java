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

package com.tle.web.sections.standard.model;

public class HtmlTabState extends HtmlComponentState
{
	private String currentTab;
	private TabModel tabModel;
	private boolean renderSelectedOnly;

	public boolean isRenderSelectedOnly()
	{
		return renderSelectedOnly;
	}

	public void setRenderSelectedOnly(boolean renderSelectedOnly)
	{
		this.renderSelectedOnly = renderSelectedOnly;
	}

	public String getCurrentTab()
	{
		return currentTab;
	}

	public void setCurrentTab(String currentTab)
	{
		this.currentTab = currentTab;
	}

	public TabModel getTabModel()
	{
		return tabModel;
	}

	public void setTabModel(TabModel tabModel)
	{
		this.tabModel = tabModel;
	}
}
