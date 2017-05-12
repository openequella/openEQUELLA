package com.tle.webtests.pageobject.portal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;

public class FreemarkerPortalEditPage extends AbstractPortalEditPage<FreemarkerPortalEditPage>
{

	@FindBy(id = "fm-editor")
	private WebElement freeMarkerAjax;
	@FindBy(id = "client-editor")
	private WebElement clientScriptingAjax;
	@FindBy(id = "freemarkerEditor_tabLayout")
	private WebElement tabIndicator;

	private WaitingPageObject<FreemarkerPortalEditPage> ajaxUpdate;

	public FreemarkerPortalEditPage(PageContext context)
	{
		super(context);
	}

	@Override
	public String getType()
	{
		return "Scripted";
	}

	@Override
	public String getId()
	{
		return "freemarkerEditor";
	}

	public void loadFreemarkerScript(PrefixedName scriptName)
	{
		ajaxUpdate = ajaxUpdate(freeMarkerAjax);
		driver.findElement(By.xpath("//div[@id = 'fm-editor']/div/button[contains(@class,'dropdown-toggle')]")).click();
		freeMarkerAjax.findElement(
			By.xpath("//ul[@class = 'dropdown-menu']/li/a[text() =" + quoteXPath(scriptName.toString()) + "]")).click();
		ajaxUpdate.get();
	}

	public void loadClientSideScript(PrefixedName scriptName)
	{
		ajaxUpdate = ajaxUpdate(clientScriptingAjax);
		driver.findElement(By.xpath("//div[@id = 'client-editor']/div/button[contains(@class,'dropdown-toggle')]"))
			.click();
		driver.findElement(
			By.xpath("//div[@id='client-editor']/div/ul[@class = 'dropdown-menu']/li/a[text() ="
				+ quoteXPath(scriptName.toString()) + "]")).click();
		ajaxUpdate.get();
	}

	public FreemarkerPortalEditPage switchToClientScript()
	{
		driver.findElement(By.xpath("//div[@id='freemarkerEditor_tabLayoutdiv']/ul/li[3]/a")).click();
		return visibilityWaiter(clientScriptingAjax).get();
	}
}
