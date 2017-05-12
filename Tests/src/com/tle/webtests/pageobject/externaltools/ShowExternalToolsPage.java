package com.tle.webtests.pageobject.externaltools;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

public class ShowExternalToolsPage extends AbstractShowEntitiesPage<ShowExternalToolsPage>
{

	public ShowExternalToolsPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected String getSectionId()
	{
		return "set";
	}

	@Override
	protected String getH2Title()
	{
		return "External tool providers (LTI)";
	}

	@Override
	protected String getEmptyText()
	{
		return "There are no editable tools";
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/externaltools.do");
	}

	public EditExternalToolPage createTool()
	{
		return createEntity(new EditExternalToolPage(this));
	}

	public EditExternalToolPage editTool(PrefixedName tool)
	{
		return editEntity(new EditExternalToolPage(this), tool);
	}

}
