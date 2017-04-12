package com.tle.web.wizard.scripting.objects.impl;

import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.wizard.scripting.objects.ControlScriptObject;
import com.tle.web.wizard.scripting.objects.PageScriptObject;

/**
 * @author aholland
 */
public class PageScriptWrapper implements PageScriptObject
{
	private static final long serialVersionUID = 1L;

	private final WizardPage page;

	public PageScriptWrapper(WizardPage page)
	{
		this.page = page;
	}

	@Override
	public boolean isEnabled()
	{
		return page.isEnabled();
	}

	@Override
	public int getPageNumber()
	{
		return page.getPageNumber();
	}

	@Override
	public String getPageTitle()
	{
		return page.getPageTitle();
	}

	@Override
	public boolean isValid()
	{
		return page.isValid();
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		page.setEnabled(enabled);
	}

	@Override
	public void setPageTitle(String title)
	{
		page.setPageTitle(title);
	}

	@Override
	public void setValid(boolean valid)
	{
		page.setValid(valid);
	}

	@Override
	public int getControlCount()
	{
		return page.getControls().size();
	}

	@Override
	public ControlScriptObject getControlByIndex(int index)
	{
		if( index < 0 || index >= page.getControls().size() )
		{
			return null;
		}
		return new ControlScriptWrapper(page.getControls().get(index), page);
	}

	@Override
	public void scriptEnter()
	{
	}

	@Override
	public void scriptExit()
	{
	}
}
