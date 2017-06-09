package com.tle.webtests.pageobject.userscripts;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.entities.AbstractShowEntitiesPage;

public class ShowUserScriptsPage extends AbstractShowEntitiesPage<ShowUserScriptsPage>
{

	public ShowUserScriptsPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected String getSectionId()
	{
		return "sus";
	}

	@Override
	protected String getH2Title()
	{
		return "User scripts";
	}

	@Override
	protected String getEmptyText()
	{
		return "There are no available scripts to edit";
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/userscripts.do");
	}
	
	public EditUserScriptPage createScript()
	{
		return createEntity(new EditUserScriptPage(this));
	}

	public EditUserScriptPage cloneScript(PrefixedName script)
	{
		return cloneEntity(new EditUserScriptPage(this), script);
	}

	public EditUserScriptPage editScript(PrefixedName script)
	{
		return editEntity(new EditUserScriptPage(this), script);
	}


}
