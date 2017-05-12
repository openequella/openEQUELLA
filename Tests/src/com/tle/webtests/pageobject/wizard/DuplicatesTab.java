package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;

import com.tle.webtests.framework.PageContext;

public class DuplicatesTab extends AbstractWizardTab<DuplicatesTab>
{
	public DuplicatesTab(PageContext context)
	{
		super(context, By.xpath("//h2[text() = 'Duplicate data']"));
	}

	public void checkWarning(String data)
	{
		driver.findElement(
			By.xpath("//div[./label[normalize-space(text())=" + quoteXPath("'" + data + "' is also used by:")
				+ "]]/input[@type='checkbox']")).click();

	}

}
