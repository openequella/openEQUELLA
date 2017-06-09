package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class RssPortalEditPage extends AbstractPortalEditPage<RssPortalEditPage>
{
	@FindBy(id = "rpe_u")
	private WebElement urlField;
	@FindBy(id = "rpe_r")
	private WebElement entriesField;

	public RssPortalEditPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getType()
	{
		return "RSS or Atom feed";
	}

	@Override
	public String getId()
	{
		return "rpe";
	}

	public void setUrl(String url)
	{
		urlField.clear();
		urlField.sendKeys(url);
	}

	public void setEntries(int number)
	{
		entriesField.clear();
		entriesField.sendKeys(String.valueOf(number));
	}
}
