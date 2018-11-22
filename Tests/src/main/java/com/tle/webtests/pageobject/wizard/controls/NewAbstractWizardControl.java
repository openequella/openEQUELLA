package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

	protected By wizIdBy(String postfix)
	{
		return By.id(getWizid()+postfix);
	}

	protected WebElement byWizId(String postfix)
	{
		return driver.findElement(wizIdBy(postfix));
	}

	protected WebElement byWizIdXPath(String xpath)
	{
		return byWizIdIdXPath("", xpath);
	}

	protected By wizIdIdXPath(String postId, String xpath)
	{
		return By.xpath("id("+quoteXPath(getWizid()+postId)+")"+xpath);
	}

	protected WebElement byWizIdIdXPath(String postId, String xpath)
	{
		return driver.findElement(wizIdIdXPath(postId, xpath));
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

	public ExpectedCondition<?> updatedCondition()
	{
		return ajaxUpdateCondition(wizIdBy(""));
	}
}
