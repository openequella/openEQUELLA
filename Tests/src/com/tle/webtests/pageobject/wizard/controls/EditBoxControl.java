package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class EditBoxControl extends AbstractWizardControl<EditBoxControl>
{
	@FindBy(xpath = "id('{wizid}')//div[@class=\"input text\"]/input")
	private WebElement input;
	@FindBy(xpath = "id('{wizid}')//h3")
	private WebElement title;

	public EditBoxControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return input;
	}

	public boolean isEnabled()
	{
		return input.isEnabled();
	}

	public void setText(String text)
	{
		input.sendKeys(text);
	}

	public String getText()
	{
		return input.getAttribute("value");
	}

	public String getTitle()
	{
		return title.getText().trim();
	}
}
