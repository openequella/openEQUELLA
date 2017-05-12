package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleSearchBlockPage extends AbstractPage<MoodleSearchBlockPage>
{

	public MoodleSearchBlockPage(PageContext context)
	{
		super(context, By.xpath("//div[contains(@class, 'block_equella_search')]"));
	}

	public MoodleSearchBlockResultsPage search(String search)
	{
		driver.findElement(By.xpath("//a[text()='Search EQUELLA']")).click();
		return new MoodleSearchBlockResultsPage(context).get().search(search);
	}

}
