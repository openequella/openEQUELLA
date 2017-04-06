package com.tle.mypages.web.section;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.js.generic.OverrideHandler;

public class MyPagesPageActionsSection extends AbstractMyPagesPageActionsSection
{
	@AjaxFactory
	private AjaxGenerator ajax;

	@SuppressWarnings("nls")
	@Override
	protected void setupAddHandler(SectionTree tree)
	{
		addPage.setClickHandler(new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, contribSection,
			events.getEventHandler("addPage"), "page-edit", "pages-table-ajax")));
	}

}
