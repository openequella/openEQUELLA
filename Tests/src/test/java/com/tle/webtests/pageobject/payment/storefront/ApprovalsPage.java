package com.tle.webtests.pageobject.payment.storefront;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;

public class ApprovalsPage extends AbstractPage<ApprovalsPage>
{
	@FindBy(id = "sa_e")
	private WebElement enabled;
	@FindBy(id = "sa_approvalsTable")
	protected WebElement approvalTable;
	@FindBy(id = "sa_paymentsTable")
	protected WebElement paymentTable;

	public ApprovalsPage(PageContext context)
	{
		super(context, By.xpath("//div[@class='area']/h2[text() = 'Approvals and payments']"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/approvals.do");
	}

	private static By getByForNames(String first, String second)
	{
		return By.xpath(".//td[text() = " + quoteXPath(first) + "]/following-sibling::td[text() = "
			+ quoteXPath(second) + "]/parent::tr");
	}

	private static WebElement getRowForNames(WebElement table, String first, String second)
	{
		return table.findElement(getByForNames(first, second));
	}
	
	public ApprovalEditPage edit(String first, String second, boolean approval)
	{
		return new ApprovalRow(first, second, approval).edit();
	}

	public boolean rowExists(String first, String second, boolean approval)
	{
		WebElement table = approval ? approvalTable : paymentTable;
		return isPresent(table.findElement(getByForNames(first, second)));
	}

	private class ApprovalRow extends AbstractPage<ApprovalRow>
	{
		@FindBy(linkText = "Edit")
		private WebElement editLink;

		private boolean approval;

		public ApprovalRow(String first, String second, boolean approval)
		{
			super(ApprovalsPage.this.context, getRowForNames(approval ? approvalTable : paymentTable, first, second),
				getByForNames(first, second));
			this.approval = approval;
		}

		public ApprovalEditPage edit()
		{
			editLink.click();
			return new ApprovalEditPage(context, true, approval).get();
		}
	}

	public class ApprovalEditPage extends AbstractPage<ApprovalEditPage>
	{
		@FindBy(id = "ea_cl")
		private WebElement cancel;

		public ApprovalEditPage(PageContext context, boolean edit, boolean approval)
		{
			super(context, By.xpath("//div[@class='area']/h2[text() = "
				+ quoteXPath((edit ? "Edit " : "New ") + (approval ? "approval " : "payment ") + "rule") + "]"));
			// Take that!
		}

		public ApprovalsPage cancel()
		{
			cancel.click();
			return new ApprovalsPage(context).get();
		}

		public String getFirst()
		{
			return driver.findElement(By.id("userDiv")).getText().replace("\nSelect", "");
		}

		public String getSecond()
		{
			return driver.findElement(By.id("approverDiv")).getText().replace("\nSelect", "");
		}
	}
}
