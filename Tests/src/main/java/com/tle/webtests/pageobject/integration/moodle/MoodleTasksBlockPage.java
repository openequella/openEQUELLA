package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleTasksBlockPage extends AbstractPage<MoodleTasksBlockPage>
{
	@FindBy(xpath = "//div[contains(@class, 'block_equella_tasks')]")
	private WebElement block;

	public MoodleTasksBlockPage(PageContext context)
	{
		super(context, By.xpath("//div[contains(@class, 'block_equella_tasks')]"));
	}

	public boolean hasTasks()
	{
		return block.findElements(By.xpath(".//div[text()='You currently have no tasks']")).isEmpty();
	}

	public String getTaskUrl(String type)
	{
		return block.findElement(By.xpath(".//a[contains(text(), " + quoteXPath(type) + ")]")).getAttribute("href");
	}

	public int getTaskCount(String type)
	{
		String task = block.findElement(By.xpath(".//a[contains(text(), " + quoteXPath(type) + ")]")).getText();

		return Integer.parseInt(task.substring(task.lastIndexOf("-") + 2));
	}

}
