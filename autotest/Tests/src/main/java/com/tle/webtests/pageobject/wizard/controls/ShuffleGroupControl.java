package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;

public class ShuffleGroupControl extends AbstractWizardControl<ShuffleGroupControl>
{
	private WebElement getDiv()
	{
		return byWizId("_div");
	}

	private WebElement getAddLink()
	{
		return byWizId("_addLink");
	}

	private WebElement getDialogDiv()
	{
		return byWizId("_controlDialog");
	}

	private WebElement getOkButton()
	{
		return byWizId("_controlDialog_ok");
	}

	private final int treenum;

	public ShuffleGroupControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page, int treenum)
	{
		super(context, ctrlnum, page);
		this.treenum = treenum;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return getDiv();
	}

	public SubWizardPage add()
	{
		getAddLink().click();
		waitForElement(getDialogDiv(), By.xpath("."));
		return new SubWizardPage(context, page, treenum, ctrlnum);
	}

	public void ok()
	{
		getOkButton().click();
		waitForElementInvisibility(getDialogDiv());
	}

	public void remove(String value)
	{
		getDiv().findElement(
			By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
				+ "]/../td[@class='actions']/a[@class='shuffle-remove']")).click();
	}

	public void edit(String value)
	{
		getDiv().findElement(
			By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
				+ "]/../td[@class='actions']/a[@class='shuffle-edit']")).click();
	}

	public boolean isDisabled()
	{
		return (" " + getAddLink().getAttribute("class").trim()).contains(" disabled");
	}

	public boolean hasValue(String value)
	{
		return isPresent(By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
			+ "]"));
	}

}
