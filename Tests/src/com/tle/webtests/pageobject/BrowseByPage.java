package com.tle.webtests.pageobject;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class BrowseByPage extends AbstractPage<BrowseByPage>
{

	private final String[] nodes;
	private final String pageName;

	public BrowseByPage(PageContext context, String pageName, String[] nodes)
	{
		super(context, By.id("matrix-results"));
		this.pageName = pageName;
		this.nodes = nodes;
	}

	@Override
	protected void loadUrl()
	{
		get("access/browseby.do", "pageName", pageName, "nodes", nodes);
	}

	public boolean isCategoryPresent(String category)
	{
		return isPresent(By.xpath("//li/a[text()=" + quoteXPath(category) + "]"));
	}

}
