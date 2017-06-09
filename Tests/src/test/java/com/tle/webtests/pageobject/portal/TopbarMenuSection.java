package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;

public class TopbarMenuSection extends AbstractPage<TopbarMenuSection>
{
	@FindBy(id = "topmenu")
	private WebElement menuDiv;

	public TopbarMenuSection(PageContext context)
	{
		super(context, By.id("topmenu"));
	}

	public int getNumberOfNotifications()
	{
		String elemText = menuDiv.findElement(By.xpath("//*[@title = 'Notifications']")).getText();
		return Integer.parseInt(elemText);
	}

	public int getNumberOfTasks()
	{
		String elemText = menuDiv.findElement(By.xpath("//*[@title = 'Tasks']")).getText();
		return Integer.parseInt(elemText);
	}

	public UserProfilePage editMyDetails()
	{
		menuDiv.findElement(By.xpath("//a[@href = 'access/user.do']")).click();
		return new UserProfilePage(context).get();
	}

}
