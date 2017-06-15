package com.tle.webtests.pageobject.remoterepo.merlot;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoViewResultPage;

public class RemoteRepoViewMerlotResultPage extends RemoteRepoViewResultPage<RemoteRepoViewMerlotResultPage>
{
	public RemoteRepoViewMerlotResultPage(PageContext context)
	{
		super(context, By.xpath("//img[@class='merlotlogo']"));
	}

	public RemoteRepoMerlotSearchPage clickRemoteRepoBreadcrumb(String repo)
	{
		driver.findElement(By.xpath("id('breadcrumbs')//a[text()='" + repo + "']")).click();
		return new RemoteRepoMerlotSearchPage(context);
	}

	// TODO Add Details
}