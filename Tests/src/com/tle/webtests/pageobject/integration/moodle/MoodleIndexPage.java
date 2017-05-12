package com.tle.webtests.pageobject.integration.moodle;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleIndexPage extends AbstractPage<MoodleIndexPage>
{
	@FindBy(linkText = "Logout")
	private WebElement logoutLink;

	public MoodleIndexPage(PageContext context)
	{
		super(context, By.xpath("//h2[contains(text(), 'ourses')]"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getIntegUrl());
	}

	public boolean isLoggedIn()
	{
		return isPresent(By.xpath("//a[text()='Logout']"));
	}

	public MoodleCoursePage clickCourse(String name)
	{
		if( isPresent(By.xpath("//a[text()=" + quoteXPath(name) + "]")) )
		{
			driver.findElement(By.xpath("//a[text()=" + quoteXPath(name) + "]")).click();
		}
		else
		{
			driver.findElement(By.id("shortsearchbox")).sendKeys(name);
			driver.findElement(By.xpath("//form[@id='coursesearch']//input[@value = 'Go']")).click();
			waitForElement(By.className("course-search-result"));
			driver.findElement(
				By.xpath("//span[@class='highlight' and text() = " + quoteXPath(name.split(" ")[0]) + "]/.."))
				.click();
		}

		return new MoodleCoursePage(context, name).get();
	}

	public List<String> listCourses()
	{
		List<WebElement> courses = driver.findElements(By.xpath("//div[contains(@class,'coursebox')]//a"));
		ArrayList<String> c = new ArrayList<String>();
		for( WebElement ele : courses )
		{
			c.add(ele.getText());
		}
		return c;
	}

	public MoodleLoginPage logout()
	{
		logoutLink.click();
		return new MoodleLoginPage(context).get();
	}
}
