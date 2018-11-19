package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class EditBoxControl extends AbstractWizardControl<EditBoxControl>
{
	private WebElement getInput()
	{
		return byWizIdXPath("//div[@class='input text']/input");
	}

	private WebElement getTitleWE()
	{
		return byWizIdXPath("//h3");
	}

	public EditBoxControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getInput();
	}

	public boolean isEnabled()
	{
		return getInput().isEnabled();
	}

	public void setText(String text)
	{
		getInput().sendKeys(text);
	}

	public String getText()
	{
		return getInput().getAttribute("value");
	}

	public String getTitle()
	{
		return getTitleWE().getText().trim();
	}
}
