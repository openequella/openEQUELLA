package com.tle.web.hierarchy.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class BrowseSelectable extends AbstractSelectionNavAction
{
	static
	{
		PluginResourceHandler.init(BrowseSelectable.class);
	}
	@PlugKey("label.selection.action")
	private static Label LABEL_BROWSE;

	@Inject
	private HierarchyService hierarchyService;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return createForwardForNavAction(info, session);
	}

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return LABEL_BROWSE;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session)
	{
		return fromInfo.createForward("/hierarchy.do");
	}

	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		if( !super.isActionAvailable(info, session) )
		{
			return false;
		}
		if( session.getLayout() != Layout.NORMAL )
		{
			return false;
		}
		// This seems really expensive...
		return (session.isAllCollections() || !Check.isEmpty(session.getCollectionUuids()))
			&& !hierarchyService.expandVirtualisedTopics(hierarchyService.getChildTopics(null), null, null).isEmpty();
	}

	@Override
	public String getActionType()
	{
		return "browse";
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}
}
