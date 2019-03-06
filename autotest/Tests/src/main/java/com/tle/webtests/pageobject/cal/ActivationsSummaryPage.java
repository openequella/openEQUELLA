package com.tle.webtests.pageobject.cal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class ActivationsSummaryPage extends AbstractPage<ActivationsSummaryPage>
{

	public ActivationsSummaryPage(PageContext context)
	{
		super(context, By.id("activations-list"));
	}

	public String getStatus(int index)
	{
		return driver.findElement(By.xpath(getRowXpath(index, "//td[contains(@class, 'status')]"))).getText().trim();
	}

	private String getRowXpath(int index, String offset)
	{
		return "//div[@id='activations-list']//table/tbody/tr[" + (index + 1) + "]" + offset;
	}

	public boolean containsLink(int index, String href)
	{
		return isPresent(By.xpath(getRowXpath(index, "//a[@href=" + quoteXPath(href) + "]")));
	}

	public boolean canEdit(int index)
	{
		return isPresent(editXPath(index));
	}

	public void delete(int index)
	{
		WebElement element = driver.findElement(deleteXpath(index));
		WaitingPageObject<ActivationsSummaryPage> removalWaiter = removalWaiter(element);
		element.click();
		acceptConfirmation();
		removalWaiter.get();
	}

	private By deleteXpath(int index)
	{
		return By.xpath(getRowXpath(index, "//td[@class='actions']//a[text()='Delete']"));
	}

	private By editXPath(int index)
	{
		return By.xpath(getRowXpath(index, "//td[@class='actions']//a[text()='Edit']"));
	}

	public boolean canDelete(int index)
	{
		return isPresent(deleteXpath(index));
	}

	public EditActivationPage edit(int index)
	{
		driver.findElement(editXPath(index)).click();
		return new EditActivationPage(context, this);
	}

	public String getInfo(int index)
	{
		return driver.findElement(By.xpath(getRowXpath(index, "//td[contains(@class, 'info')]"))).getText().trim();
	}

}
