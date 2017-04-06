package com.tle.web.myresources.selection;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.AbstractSelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MyResourcesSelectable extends AbstractSelectionNavAction
{
	static
	{
		PluginResourceHandler.init(MyResourcesSelectable.class);
	}

	@PlugKey("label.myresources.action")
	private static Label LABEL_MYRESOURCES_ACTION;

	@Override
	public Label getLabelForNavAction(SectionInfo info)
	{
		return LABEL_MYRESOURCES_ACTION;
	}

	@Override
	public SectionInfo createForwardForNavAction(SectionInfo info, SelectionSession session)
	{
		return getMyResourcesTree(info);
	}

	protected SectionInfo getMyResourcesTree(SectionInfo info)
	{
		return info.createForward("/access/myresources.do");
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
	public String getActionType()
	{
		return "myresources";
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return true;
	}

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		return getMyResourcesTree(info);
	}

}
