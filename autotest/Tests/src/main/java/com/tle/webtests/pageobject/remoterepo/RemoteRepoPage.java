package com.tle.webtests.pageobject.remoterepo;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class RemoteRepoPage extends AbstractPage<RemoteRepoPage>
{

	public RemoteRepoPage(PageContext context)
	{
		super(context, By.xpath("//table/thead/tr/th[text()='Remote repository']"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/remoterepo.do");
	}

	public <T extends AbstractPage<T>> T clickRemoteRepository(String title, T page)
	{
		driver.findElement(
			By.xpath("id('content-body')//a[normalize-space(text())=" + AbstractPage.quoteXPath(title) + "]")).click();
		return page.get();
	}

	public boolean isRemoteRepositoryVisible(String title)
	{
		return isPresent(By.xpath("id('content-body')//a[normalize-space(text())=" + AbstractPage.quoteXPath(title)
			+ "]"));
	}

}
