package com.tle.webtests.pageobject.integration.canvas.course;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.integration.canvas.CanvasLoginPage;

public abstract class AbstractCanvasPage<T extends AbstractCanvasPage<T>> extends AbstractPage<T>
{
	// common top bar links
	@FindBy(xpath = "id('courses_menu_item')/a")
	private WebElement courseMenu;
	@FindBy(xpath = "//li[@class='logout']//a")
	private WebElement logoutLink;

	public AbstractCanvasPage(PageContext context, By loadedBy)
	{
		super(context, loadedBy);
	}

	public CanvasLoginPage logout()
	{
		logoutLink.click();
		return new CanvasLoginPage(context).get();
	}

	public void dumbCanvasClickHack(WebElement toClick)
	{
		Actions action = new Actions(driver);
		action.clickAndHold(toClick);
		action.build().perform();
		try
		{
			Thread.sleep(2000);
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
		action.release();
		action.perform();
	}

}
