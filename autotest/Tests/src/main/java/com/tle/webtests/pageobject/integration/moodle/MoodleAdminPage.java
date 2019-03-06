package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class MoodleAdminPage extends AbstractPage<MoodleAdminPage>
{

	public MoodleAdminPage(PageContext context)
	{
		super(context, By.xpath("//body[contains(@id,'admin-index')]"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl() + "admin/index.php");
	}

	public <T extends PageObject> T upgrade(WaitingPageObject<T> targetPage)
	{
		if( isPresent(By.xpath("//input[contains(@value, 'Upgrade')]")) )
		{
			driver.findElement(By.xpath("//input[contains(@value, 'Upgrade')]")).click();
		}
		return new MoodleNoticePage<T>(targetPage).get();
	}
}
