package com.tle.webtests.pageobject.remoterepo.z3950;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.remoterepo.RemoteRepoViewResultPage;

public class RemoteRepoViewZ3950ResultPage extends RemoteRepoViewResultPage<RemoteRepoViewZ3950ResultPage>
{
	public RemoteRepoViewZ3950ResultPage(PageContext context)
	{
		super(context, By.id("z_importButton"));
	}

	public RemoteRepoBasicZ3950SearchPage clickBasicRemoteRepoBreadcrumb(String repo)
	{
		WebElement bc = driver.findElement(By.xpath("id('breadcrumbs')//a[text()='" + repo + "']"));
		bc.click();
		return new RemoteRepoBasicZ3950SearchPage(context);
	}

	public RemoteRepoAdvancedZ3950SearchPage clickAdvancedRemoteRepoBreadcrumb(String repo)
	{
		WebElement bc = driver.findElement(By.xpath("id('breadcrumbs')//a[text()='" + repo + "']"));
		bc.click();
		return new RemoteRepoAdvancedZ3950SearchPage(context);
	}

}
