package com.tle.webtests.pageobject.viewitem;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;

public class ShareWithOthersPage extends AbstractPage<ShareWithOthersPage>
{
	@FindBy(id = "swoc_selectUserToNotify")
	private WebElement newUserToNotify;
	@FindBy(id = "swoc_ot")
	private WebElement tableElem;

	public ShareWithOthersPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return tableElem;
	}

	public ShareWithOthersPage selectUser(String query, String username, String displayName)
	{
		newUserToNotify.click();
		SelectUserDialog dialog = new SelectUserDialog(context, "3").get();
		dialog.search(query);
		return dialog.selectAndFinish(username, userWaiter(displayName));
	}

	private WaitingPageObject<ShareWithOthersPage> userWaiter(String displayName)
	{
		return ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(tableElem, getByForDisplayName(displayName)), this);
	}

	private By getByForDisplayName(String user)
	{
		return By.xpath(".//tr[td/span/text()=" + quoteXPath(user) + "]");
	}

	public boolean checkIfListed(String user)
	{
		return isPresent(tableElem, getByForDisplayName(user));
	}
}
