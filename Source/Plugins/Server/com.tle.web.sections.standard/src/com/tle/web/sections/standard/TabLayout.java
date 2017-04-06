package com.tle.web.sections.standard;

import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.standard.model.HtmlTabState;
import com.tle.web.sections.standard.model.TabContent;
import com.tle.web.sections.standard.model.TabModel;
import com.tle.web.sections.standard.model.TabSection;
import com.tle.web.sections.standard.model.TabSectionTabModel;

public class TabLayout extends AbstractRenderedComponent<HtmlTabState>
	implements
		ParametersEventListener,
		BookmarkEventListener
{
	private boolean renderSelectedOnly;
	private TabModel tabModel = new TabSectionTabModel();

	public TabLayout()
	{
		super(RendererConstants.TABS);
	}

	public void addTabSection(TabSection section)
	{
		((TabSectionTabModel) tabModel).addTabSection(section);
	}

	public void addTabSections(List<TabSection> sections)
	{
		((TabSectionTabModel) tabModel).addTabSections(sections);
	}

	@Override
	protected void prepareModel(RenderContext info)
	{
		HtmlTabState state = getState(info);
		state.setRenderSelectedOnly(renderSelectedOnly);
		state.setTabModel(tabModel);
		if( renderSelectedOnly )
		{
			state.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
		}
		String currentTab = state.getCurrentTab();
		if( currentTab == null || tabModel.getIndexForTab(info, currentTab) < 0 )
		{
			List<TabContent> tabs = tabModel.getVisibleTabs(info);
			if( !tabs.isEmpty() )
			{
				state.setCurrentTab(tabs.get(0).getValue());
			}
		}
		super.prepareModel(info);
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event)
	{
		String currentTab = event.getParameter(getParameterId(), false);
		getState(info).setCurrentTab(currentTab);
	}

	@Override
	public void bookmark(SectionInfo info, BookmarkEvent event)
	{
		if( addToThisBookmark(info, event) )
		{
			event.setParam(getParameterId(), getState(info).getCurrentTab());
		}
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		addDocumentedParam(event, getParameterId(), String.class.getName());
	}

	@Override
	public Class<HtmlTabState> getModelClass()
	{
		return HtmlTabState.class;
	}

	public boolean isRenderSelectedOnly()
	{
		return renderSelectedOnly;
	}

	public void setRenderSelectedOnly(boolean renderSelectedOnly)
	{
		this.renderSelectedOnly = renderSelectedOnly;
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
