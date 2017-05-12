package com.tle.webtests.pageobject.kaltura;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class KalturaServerList extends AbstractPage<KalturaServerList>
{
	@FindBy(id = "kalturaservers")
	private WebElement serverDiv;
	@FindBy(id = "ksl_addServerLink")
	private WebElement addServerLink;

	public KalturaServerList(PageContext context)
	{
		super(context, By.id("kalturaservers"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/kalturasettings.do");
	}

	public KalturaServerList enableServer(String server, boolean enable)
	{
		return clickAndRemove(serverDiv.findElement(getServerBy(server)).findElement(
			By.linkText(enable ? "Enable" : "Disable")));
	}

	public boolean isEnabled(String server)
	{
		return isPresent(serverDiv.findElement(getServerBy(server)), By.linkText("Disable"));
	}

	public KalturaServerEditor editServer(String server)
	{
		serverDiv.findElement(getServerBy(server)).findElement(By.id("edit")).click();
		return new KalturaServerEditor(context, false).get();
	}

	public KalturaServerList deleteServer(String server)
	{
		return clickAndRemove(serverDiv.findElement(getServerBy(server)).findElement(By.id("delete")));
	}

	public KalturaServerEditor addKalturaServer()
	{
		addServerLink.click();
		return new KalturaServerEditor(context, true).get();
	}

	public boolean hasServer(String server)
	{
		return isPresent(getServerBy(server));
	}

	private By getServerBy(String server)
	{
		return By.xpath("./table/tbody/tr/td[@class='name' and text()='" + server + "']/..");
	}

}
