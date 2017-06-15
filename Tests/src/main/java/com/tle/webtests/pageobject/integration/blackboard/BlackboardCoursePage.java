package com.tle.webtests.pageobject.integration.blackboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;

public class BlackboardCoursePage extends AbstractBlackboardCoursePage<BlackboardCoursePage>
{
	@FindBy(xpath = "//span[@title='Content']")
	private WebElement content;
	@FindBy(xpath = "//span[@title='Information']")
	private WebElement information;
	private final String courseName;

	public BlackboardCoursePage(PageContext context, String courseName)
	{
		super(context, courseName, "Home Page");
		this.courseName = courseName;
	}

	public BlackboardContentPage content()
	{
		content.click();
		return new BlackboardContentPage(context, courseName, "Content").get();
	}

	public BlackboardContentPage information()
	{
		information.click();
		return new BlackboardContentPage(context, courseName, "Information").get();
	}

	public void bulkDelete()
	{
		By section = By.id("controlpanel.packages.and.utilities_groupExpanderLink");

		waitForElement(section);
		driver.findElement(By.xpath("//img[@alt='Packages & Utilities Overview Page']")).click();

		waitForElement(By.linkText("Bulk Delete"));
		driver.findElement(By.linkText("Bulk Delete")).click();
		new BlackboardBulkDeletePage(context, courseName).get().delete();
	}

	public void setAvailible()
	{
		By section = By.id("controlpanel.customization_groupExpanderLink");

		waitForElement(section);
		driver.findElement(By.xpath("//img[@alt='Customization Overview Page']")).click();

		waitForElement(By.linkText("Tool Availability"));
		driver.findElement(By.linkText("Tool Availability")).click();
		new BlackboardToolAvailabilityPage(context, courseName).get().availible();
	}
}
