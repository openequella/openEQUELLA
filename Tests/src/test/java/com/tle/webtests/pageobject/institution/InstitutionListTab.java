package com.tle.webtests.pageobject.institution;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;

public class InstitutionListTab extends InstitutionTab<InstitutionListTab>
{
	public InstitutionListTab(PageContext context, String password)
	{
		super(context, "Institutions", "Institutions");
		setPassword(password);
	}

	public boolean institutionExists(String instutionUrl)
	{
		return isPresent(instXPath(instutionUrl));
	}

	private By instXPath(String instUrl)
	{
		return By.xpath("//tr[td/span[@class='insturl' and text()=" + quoteXPath(instUrl) + "]]");
	}

	private WebElement getInstRow(String instUrl)
	{
		return driver.findElement(instXPath(instUrl));
	}

	public StatusPage<InstitutionListTab> delete(String instutionUrl)
	{
		return delete(instutionUrl, this);
	}

	public <T extends InstitutionTabInterface> StatusPage<T> delete(String instutionUrl, WaitingPageObject<T> backTab)
	{
		WebElement deleteButton = getInstRow(instutionUrl).findElement(By.linkText("Delete"));
		deleteButton.click();
		acceptConfirmation();
		return new StatusPage<T>(context, backTab).get();
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getTestConfig().getAdminUrl() + "institutions.do?method=admin");
	}

	public ExportPage export(String instutionUrl)
	{
		WebElement exportButton = getInstRow(instutionUrl).findElement(By.linkText("Export"));
		exportButton.click();
		return new ExportPage(context, this).get();
	}

	public ClonePage clone(String instutionUrl)
	{
		WebElement cloneButton = getInstRow(instutionUrl).findElement(By.linkText("Clone"));
		cloneButton.click();
		return new ClonePage(context, this).get();
	}
}
