package com.tle.webtests.pageobject.integration.moodle;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class MoodleExternalToolConfigPage extends AbstractPage<MoodleExternalToolConfigPage>
{
	@FindBy(id = "id_lti_typename")
	private WebElement toolName;
	@FindBy(id = "id_lti_toolurl")
	private WebElement baseURL;
	@FindBy(id = "id_lti_resourcekey")
	private WebElement consumerKey;
	@FindBy(id = "id_lti_password")
	private WebElement sharedSecret;
	@FindBy(id = "id_lti_coursevisible")
	private WebElement showTool;
	@FindBy(id = "id_submitbutton")
	private WebElement save;
	@FindBy(id = "id_cancel")
	private WebElement cancel;

	public MoodleExternalToolConfigPage(PageContext context)
	{
		super(context, By.xpath("//h2[contains(text(),'External')]"));

	}

	// When adding via administration the path is different
	public MoodleExternalToolPage createExternalTool(String name, String url, String key, String secret, boolean show)
	{

		setToolName(name);
		baseURL.clear();
		baseURL.sendKeys(url);
		consumerKey.clear();
		consumerKey.sendKeys(key);
		setSharedSecret(secret);
		showTool.click();

		scrollIntoViewAndClick(save);

		return new MoodleExternalToolPage(context).get();
	}

	public WebElement getSharedSecret()
	{
		return sharedSecret;
	}

	public void setSharedSecret(String secret)
	{
		sharedSecret.clear();
		sharedSecret.sendKeys(secret);
	}

	public void setToolName(String name)
	{
		toolName.clear();
		toolName.sendKeys(name);
	}

	public MoodleExternalToolPage save()
	{
		scrollIntoViewAndClick(save);
		return new MoodleExternalToolPage(context).get();
	}

	private void scrollIntoViewAndClick(WebElement toClick)
	{
		Point loc = toClick.getLocation();
		((JavascriptExecutor) driver).executeScript("window.scrollBy(0," + (loc.y - 80) + ")", "");
		toClick.click();
	}
}
