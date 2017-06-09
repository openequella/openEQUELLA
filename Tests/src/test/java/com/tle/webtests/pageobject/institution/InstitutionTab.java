package com.tle.webtests.pageobject.institution;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public abstract class InstitutionTab<T extends InstitutionTab<T>> extends AbstractPage<T>
	implements
		InstitutionTabInterface
{
	private String password;

	@FindBy(id = "islm_password")
	private WebElement passwordField;
	@FindBy(id = "islm_loginButton")
	private WebElement loginButton;

	private final String tabName;

	protected InstitutionTab(PageContext context, String tabName, String title)
	{
		super(context, By.xpath("//h2[normalize-space(text())=" + quoteXPath(title) + "]"));
		this.tabName = tabName;
	}

	@Override
	public void checkLoaded()
	{
		try
		{
			passwordField.getAttribute("value");
		}
		catch( NotFoundException nfe )
		{
			super.checkLoaded();
			return;
		}
		passwordField.clear();
		passwordField.sendKeys(password);
		loginButton.click();
		throw new NotFoundException("Showing password");
	}

	public ImportTab importTab()
	{
		return clickTab(new ImportTab(context));
	}

	public ServerSettingsTab serverSettingsTab()
	{
		return clickTab(new ServerSettingsTab(context));
	}

	public <TA extends InstitutionTab<TA>> TA clickTab(InstitutionTab<? extends TA> tab)
	{
		driver.findElement(By.xpath("//a[text()=" + quoteXPath(tab.getTabName()) + "]")).click();
		return (TA) tab.get();
	}

	@Override
	public String getTabName()
	{
		return tabName;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
