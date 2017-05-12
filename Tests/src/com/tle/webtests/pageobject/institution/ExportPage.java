package com.tle.webtests.pageobject.institution;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ExportPage extends AbstractPage<ExportPage>
{
	@FindBy(id = "isexp_actionButton")
	private WebElement exportButton;

	private final InstitutionListTab listTab;

	public ExportPage(PageContext context, InstitutionListTab institutionListTab)
	{
		super(context, By.id("isexp_actionButton"));
		this.listTab = institutionListTab;
	}

	public void removeAuditLogs()
	{
		driver.findElement(By.id("isexp_auditlogsCheck")).click();
	}

	public StatusPage<InstitutionListTab> export()
	{
		exportButton.click();
		acceptConfirmation();
		return new StatusPage<InstitutionListTab>(context, listTab).get();
	}

}
