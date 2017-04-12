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
