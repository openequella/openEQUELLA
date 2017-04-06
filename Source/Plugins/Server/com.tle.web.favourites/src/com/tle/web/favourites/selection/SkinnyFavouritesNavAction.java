package com.tle.web.favourites.selection;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@Bind
@Singleton
@SuppressWarnings("nls")
public class SkinnyFavouritesNavAction extends AbstractSelectionNavAction
{
	private static final String FORWARD_PATH = "/access/skinny/favourites.do";

	@PlugKey("selection.navaction")
	private static Label NAV_ACTION_LABEL;

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return NAV_ACTION_LABEL;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo fromInfo, SelectionSession session)
	{
		return fromInfo.createForward(FORWARD_PATH);
	}

	@Override
	public String getActionType()
	{
		return "skinnyfavourites";
	}

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return createForwardForNavAction(info, session);
	}

	@Override
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		if( !super.isActionAvailable(info, session) )
		{
			return false;
		}
		Layout layout = session.getLayout();
		return layout == Layout.SKINNY || layout == Layout.COURSE;
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return false;
	}
}
