package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.WaitingPageObject;

public abstract class AbstractPortalSection<T extends AbstractPortalSection<T>> extends AbstractPage<T>
{
	protected String title;

	@FindBy(xpath = "//h3[normalize-space(text())='{title}']/ancestor::div[contains(@class, 'box_head')][1]")
	protected WebElement boxHead;
	@FindBy(xpath = "//h3[normalize-space(text())='{title}']/ancestor::div[contains(@class, 'box_head')][1]/following-sibling::div[contains(@class, 'box_content')]/div")
	protected WebElement boxContent;

	public AbstractPortalSection(PageContext context, String title)
	{
		super(context);
		this.title = title;
	}

	protected WebElement findLoadedElement()
	{
		return boxHead;
	}

	public HomePage delete()
	{
		ExpectedCondition<Boolean> removalCondition = removalCondition(boxHead);
		showButtons();
		boxHead.findElement(By.className("box_close")).click();
		acceptConfirmation();
		waiter.until(removalCondition);
		return new HomePage(context).get();
	}

	public T minMax()
	{
		boolean present = isPresent(boxContent);
		WaitingPageObject<T> aWaiter;
		if( present )
		{
			aWaiter = removalWaiter(boxContent);
		}
		else
		{
			aWaiter = visibilityWaiter(boxContent);
		}

		boxHead.findElement(By.className("box_minimise")).click();
		return aWaiter.get();
	}

	public boolean isMinimisable()
	{
		return isPresent(boxHead, By.className("box_minimise"));
	}

	public boolean isCloseable()
	{
		return isPresent(boxHead, By.className("box_close"));
	}

	public <P extends AbstractPortalEditPage<P>> P edit(P portal)
	{
		showButtons();
		boxHead.findElement(By.className("box_edit")).click();
		return portal.get();
	}

	private void showButtons()
	{
		// hover doesn't work correctly so just force the buttons to show
		((JavascriptExecutor) driver).executeScript("$('img.action').show();");
	}

	public String getTitle()
	{
		return title;
	}
}
