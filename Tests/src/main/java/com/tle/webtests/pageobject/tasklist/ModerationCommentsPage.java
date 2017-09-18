package com.tle.webtests.pageobject.tasklist;

import org.openqa.selenium.By;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ModerationCommentsPage extends AbstractPage<ModerationCommentsPage>
{

	public ModerationCommentsPage(PageContext context)
	{
		super(context, By.id("moderate"));
	}

	private String xpathForComment(String comment)
	{
		if( Check.isEmpty(comment) )
		{
			return "//div[@class='comment' and count(div[@class='comment-content']) = 0]";
		}
		return "//div[contains(@class,'comment') and div[@class='comment-content']/p/text() = "+quoteXPath(comment)+"]";
	}

	public boolean containsComment(String comment)
	{
		return isPresent(By.xpath(xpathForComment(comment)));

	}

	public String getCommentClass(String comment)
	{
		return driver.findElement(By.xpath(xpathForComment(comment))).getAttribute("class");
	}

}
