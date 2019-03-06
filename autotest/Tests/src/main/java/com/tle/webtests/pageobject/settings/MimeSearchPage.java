package com.tle.webtests.pageobject.settings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.searching.FilterSearchResultsPage;

public class MimeSearchPage extends AbstractPage<MimeSearchPage>
{
	@FindBy(id = "ama_addMimeButton")
	private WebElement addButton;

	public MimeSearchPage(PageContext context)
	{
		super(context, By.id("ama_addMimeButton"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/mime.do");
	}

	public FilterSearchResultsPage all()
	{
		return new FilterSearchResultsPage(context).get();
	}

	public FilterSearchResultsPage search(String query)
	{
		FilterSearchResultsPage filterSearchResultsPage = new FilterSearchResultsPage(context).get();
		filterSearchResultsPage.search(query);
		return filterSearchResultsPage;
	}

	public MimeSearchPage deleteMime(int index)
	{
		WebElement result = driver.findElement(By.xpath("//div[normalize-space(@class)='itemresult-wrapper']["
			+ String.valueOf(index) + "]"));
		result.findElement(EBy.buttonText("Delete")).click();
		acceptConfirmation();
		return removalWaiter(result).get();
	}

	public MimeEditorPage editMime(int index)
	{
		driver
			.findElement(By.xpath("//div[normalize-space(@class)='itemresult-wrapper'][" + String.valueOf(index) + "]"))
			.findElement(EBy.buttonText("Edit")).click();
		return new MimeEditorPage(context).get();
	}

	public MimeEditorPage editMime(String mimeType)
	{
		driver.findElement(By.xpath("//a[normalize-space(text())=" + AbstractPage.quoteXPath(mimeType) + "]")).click();
		return new MimeEditorPage(context).get();
	}

	public MimeEditorPage addMime()
	{
		addButton.click();
		return new MimeEditorPage(context).get();
	}
}
