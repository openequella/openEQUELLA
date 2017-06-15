package com.tle.webtests.pageobject.viewitem;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class VideoPage extends AbstractPage<VideoPage>
{

	public VideoPage(PageContext context)
	{
		super(context, By.id("html5player"));
	}

	public String videoSource()
	{
		if( isPresent(By.id("html5player_html5_api")) )
		{
			return driver.findElement(By.xpath("//video[@id = 'html5player_html5_api']/source")).getAttribute("src");
		}
		return "flash";
	}

	public boolean ensureLibrariesLoaded()
	{
		return isPresent(By.xpath("/html/head/script[contains(@src, 'video.js')]"))
			&& isPresent(By.xpath("/html/head/link[contains(@href, 'video-js.css')]"));
	}

}
