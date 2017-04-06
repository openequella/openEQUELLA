package com.tle.web.selection.home;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

/**
 * @author aholland
 */
@Bind
@Singleton
public class SelectionHomeSelectable extends AbstractSelectionNavAction
{
	static
	{
		PluginResourceHandler.init(SelectionHomeSelectable.class);
	}

	@PlugKey("label.home.action")
	private static Label LABEL_HOME_ACTION;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return createForwardForNavAction(info, session);
	}

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return LABEL_HOME_ACTION;
	}

	@SuppressWarnings("nls")
	@Override
	public SectionInfo createForwardForNavAction(SectionInfo info, SelectionSession session)
	{
		return info.createForward("/access/selection/home.do");
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
		String home = session.getHomeSelectable();
		return (home != null && home.equals("home"));
	}

	@Override
	public String getActionType()
	{
		return "home";
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}
}
