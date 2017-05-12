package com.tle.webtests.pageobject.institution;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;

public class StatusPage<T extends InstitutionTabInterface> extends AbstractPage<StatusPage<T>>
{
	@FindBy(id = "returnLink")
	private WebElement returnLink;
	@FindBy(id = "error-div")
	private WebElement errorText;
	@FindBy(id = "error-list")
	private WebElement errorContent;
	@FindBy(id = "downloadLink")
	private WebElement downloadLink;
	private final WaitingPageObject<T> tab;

	public StatusPage(PageContext context, WaitingPageObject<T> tab)
	{
		this(context, tab, 500);
	}

	public StatusPage(PageContext context, WaitingPageObject<T> tab, long timeout)
	{
		super(context, new WebDriverWait(context.getDriver(), timeout));
		mustBeVisible = false;
		this.tab = tab;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return returnLink;
	}

	public boolean waitForFinish()
	{
		WebElement finishedBy = waiter.until(new ExpectedCondition<WebElement>()
		{
			@Override
			public WebElement apply(WebDriver arg0)
			{
				if( errorText.isDisplayed() )
				{
					return errorText;
				}
				if( returnLink.isDisplayed() )
				{
					return returnLink;
				}
				return null;
			}
		});
		return finishedBy == returnLink;
	}

	public T back()
	{
		returnLink.click();
		return tab.get();
	}

	public String getDownloadLink()
	{
		return downloadLink.getAttribute("href");
	}

	public String getErrorText()
	{
		try
		{
			return errorContent.getText();
		}
		catch( Exception e )
		{
			return "An error occurred, check the resource center logs";
		}
	}
}
