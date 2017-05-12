package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;

public class ShuffleGroupControl extends AbstractWizardControl<ShuffleGroupControl>
{
	@FindBy(id = "{wizid}_div")
	private WebElement div;
	@FindBy(id = "{wizid}_addLink")
	private WebElement addLink;
	@FindBy(id = "{wizid}_controlDialog")
	private WebElement dialogDiv;
	@FindBy(id = "{wizid}_controlDialog_ok")
	private WebElement okButton;
	private final int treenum;

	public ShuffleGroupControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page, int treenum)
	{
		super(context, ctrlnum, page);
		this.treenum = treenum;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return div;
	}

	public SubWizardPage add()
	{
		addLink.click();
		waitForElement(dialogDiv, By.xpath("."));
		return new SubWizardPage(context, page, treenum, ctrlnum);
	}

	public void ok()
	{
		okButton.click();
		waitForElementInvisibility(dialogDiv);
	}

	public void remove(String value)
	{
		div.findElement(
			By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
				+ "]/../td[@class='actions']/a[@class='shuffle-remove']")).click();
	}

	public void edit(String value)
	{
		div.findElement(
			By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
				+ "]/../td[@class='actions']/a[@class='shuffle-edit']")).click();
	}

	public boolean isDisabled()
	{
		return (" " + addLink.getAttribute("class").trim()).contains(" disabled");
	}

	public boolean hasValue(String value)
	{
		return isPresent(By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value)
			+ "]"));
	}

}
