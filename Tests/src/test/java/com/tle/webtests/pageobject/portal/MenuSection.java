package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.UndeterminedPage;
import com.tle.webtests.pageobject.hierarchy.TopicPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;

public class MenuSection extends AbstractPage<MenuSection>
{
	public MenuSection(PageContext context)
	{
		super(context, By.id("menu"));
	}

	public <T extends AbstractPage<T>> T clickMenu(String title, T page)
	{
		driver.findElement(By.xpath("id('menu')//a[text()=" + quoteXPath(title) + "]")).click();
		return page.get();
	}

	// Handles the case of a single wizard...
	public WizardPageTab clickContribute(String wizardName)
	{
		driver.findElement(By.xpath("id('menu')//a[text()=" + quoteXPath("Contribute") + "]")).click();
		WizardPageTab wpt = new WizardPageTab(context, 0);
		ContributePage cp = new ContributePage(context);
		UndeterminedPage<PageObject> unknown = new UndeterminedPage<PageObject>(context, cp, wpt);

		PageObject po = unknown.get();

		if( po == cp )
		{
			return cp.openWizard(wizardName);
		}

		return wpt;
	}

	public <T extends AbstractPage<T>> T clickMenuLink(String title, T page)
	{
		driver.findElement(By.xpath("id('menu')//a[@href=" + quoteXPath(title) + "]")).click();
		return page.get();
	}

	public TopicPage clickTopic(String title)
	{
		if( hasMenuOption(title) )
		{
			driver.findElement(By.xpath("id('menu')//a[text()=" + quoteXPath(title) + "]"))
				.click();
			return new TopicPage(context, title).get();
		}
		else
		{
			return new TopicPage(context, "Browse").load().clickSubTopic(title);
		}
	}

	public HomePage home()
	{
		return clickMenu("Dashboard", new HomePage(context));
	}

	public boolean hasMenuOption(String title)
	{
		return isPresent(By.xpath("id('menu')//a[text()=" + quoteXPath(title) + "]"));
	}

	public boolean hasHierarchyTopic(String title, int position)
	{
		return isPresent(By.xpath("id('menu')/ul[2]/li[" + position + "]/a[text()="
			+ quoteXPath(title) + "]"));
	}

	public int getNumberOfTasks()
	{
		WebElement span = driver.findElement(By.xpath("id('menu')//a[text()='My tasks']//span"));
		return Integer.parseInt(span.getText());
	}

	public int getNumberOfNotifications()
	{
		WebElement span = driver.findElement(By.xpath("id('menu')//a[text()='Notifications']//span"));
		return Integer.parseInt(span.getText());
	}
}
