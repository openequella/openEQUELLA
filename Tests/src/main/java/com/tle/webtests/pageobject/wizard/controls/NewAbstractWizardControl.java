package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class NewAbstractWizardControl<T extends PageObject> extends AbstractPage<T>
{
	protected final int ctrlnum;
	protected final AbstractWizardControlPage<?> page;


	public NewAbstractWizardControl(PageContext context, int ctrlNum, AbstractWizardControlPage<?> page)
	{
		this(context, ctrlNum, page, null);
	}

	public NewAbstractWizardControl(PageContext context, int ctrlNum, AbstractWizardControlPage<?> page, By by)
	{
		super(context, by);
		this.ctrlnum = ctrlNum;
		this.page = page;
	}

	public String getWizid()
	{
		return page.getControlId(ctrlnum);
	}

	public int getCtrlNum()
	{
		return ctrlnum;
	}

	public AbstractWizardControlPage<?> getPage()
	{
		return page;
	}
}
