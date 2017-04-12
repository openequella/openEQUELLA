package com.tle.web.searching.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionNavAction;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@Bind
@Singleton
public class CourseSearchSelectable implements SelectionNavAction
{
	private static final String FORWARD_PATH = "/access/course/searching.do";
	public static final String ID = "coursesearch";

	static
	{
		PluginResourceHandler.init(CourseSearchSelectable.class);
	}

	@PlugKey("selection.navaction")
	private static Label NAV_ACTION_LABEL;

	@Inject
	private SectionsController controller;

	@Override
	public SectionInfo createSectionInfo(SectionInfo info, SelectionSession session)
	{
		session.setSelectMultiple(true);
		session.setSkipCheckoutPage(true);

		return getSearchTree(info);
	}

	@Override
	public String getActionType()
	{
		return ID;
	}

	protected SectionInfo getSearchTree(SectionInfo info)
	{
		return controller.createForward(info, FORWARD_PATH);
	}

	public void setController(SectionsController controller)
	{
		this.controller = controller;
	}

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
	public boolean isActionAvailable(SectionInfo info, SelectionSession session)
	{
		return session.getLayout() == Layout.COURSE;
	}

	@Override
	public boolean isShowBreadcrumbs()
	{
		return false;
	}
}