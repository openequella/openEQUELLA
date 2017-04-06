package com.tle.web.controls.mypages;

import com.tle.mypages.web.section.AbstractMyPagesPageActionsSection;
import com.tle.web.sections.SectionTree;

public class MyPagesHandlerPageActionsSection extends AbstractMyPagesPageActionsSection
{

	@SuppressWarnings("nls")
	@Override
	protected void setupAddHandler(SectionTree tree)
	{
		addPage.setClickHandler(events.getSubmitValuesHandler("addPage"));
	}

}
