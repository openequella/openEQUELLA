package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public class SearchResult<T extends SearchResult<T>> extends AbstractPage<T>
{
	protected WebElement resultDiv;
	protected AbstractResultList<?, ?> resultPage;

	public SearchResult(AbstractResultList<?, ?> resultPage, SearchContext searchContext, By by)
	{
		super(resultPage.getContext(), by);
		this.resultPage = resultPage;
		this.relativeTo = searchContext;
	}

	@Override
	protected WebElement findLoadedElement()
	{
		resultDiv = super.findLoadedElement();
		return resultDiv;
	}

	public void clickTitle()
	{
		resultDiv.findElement(By.xpath(".//h3/a")).click();
	}

	public String getTitle()
	{
		return resultDiv.findElement(By.xpath(".//h3/a")).getText();
	}

	public void clickLink(String text)
	{
		resultDiv.findElement(By.xpath(".//a[text()=" + quoteXPath(text) + "]")).click();
	}

	public static String getDetailMatcherXPath(String detail, String value)
	{
		return "//div[@class='itemresult-metaline' and normalize-space(string())=" + quoteXPath(detail + ": " + value)
			+ "]";
	}

	public static String getDetailXPath(String detail)
	{
		return "//div[@class='itemresult-metaline' and ./strong[starts-with(text()," + quoteXPath(detail) + ")]]";
	}

	/**
	 * The assumption is that the key-value structure we're looking at is key +
	 * [space] + [colon] + value, hence the default offset after key from which
	 * point the value starts is 2.
	 * 
	 * @param detail
	 * @return
	 */
	public String getDetailText(String detail)
	{
		return getDetailText(detail, 2);
	}

	/**
	 * Override assumption with explicit term, allows for search when only the
	 * start of the key is sent.
	 * 
	 * @param detail
	 * @param offsetAfterDetail
	 * @return
	 */
	public String getDetailText(String detail, int offsetAfterDetail)
	{
		String detailLineText = resultDiv.findElement(By.xpath("." + getDetailXPath(detail))).getText();
		return detailLineText.substring(detail.length() + offsetAfterDetail);
	}

	public String getDetailLinkText(String detail)
	{
		String detailLineText = resultDiv.findElement(By.xpath("." + getDetailXPath(detail) + "/span/a")).getText();
		return detailLineText;
	}

	public boolean isDetailLinkPresent(String detail, String linkText)
	{
		String xpath = "." + getDetailXPath(detail) + "/span/a[text()=" + quoteXPath(linkText) + "]";
		return isPresent(resultDiv, By.xpath(xpath));
	}

	public void setChecked(boolean checked)
	{
		setChecked(checked, false);
	}

	public void setChecked(boolean checked, boolean selection)
	{
		String buttonName = getSelectButtonName(checked, selection);
		String otherButton = getSelectButtonName(!checked, selection);
		clickAction(buttonName, ExpectWaiter.waiter(
			ExpectedConditions2.visibilityOfElementLocated(resultDiv, EBy.buttonText(otherButton)), this));
	}

	private String getSelectButtonName(boolean checked, boolean selection)
	{
		return checked ? (selection ? "Select summary page" : "Select") : "Unselect";
	}

	public String getStatus()
	{
		String statusLine = getDetailText("Status");
		int firstDivider = statusLine.indexOf('|');
		if( firstDivider != -1 )
		{
			statusLine = statusLine.substring(0, firstDivider);
		}
		return statusLine.trim().toLowerCase();
	}

	public <P extends PageObject> P clickAction(String action, WaitingPageObject<P> resultOfAction)
	{
		return clickActionConfirm(action, null, resultOfAction);
	}

	public <P extends PageObject> P clickActionConfirm(String action, Boolean confirm,
		WaitingPageObject<P> resultOfAction)
	{
		WebElement checkBox = resultDiv.findElement(EBy.buttonText(action));
		checkBox.click();
		if( confirm == null )
		{
			return resultOfAction.get();
		}
		if( confirm )
		{
			acceptConfirmation();
		}
		else
		{
			cancelConfirmation();
		}
		return resultOfAction.get();
	}

	public <P extends PageObject> P clickActionConfirmAndRemove(String action, Boolean confirm,
		AbstractPage<P> resultOfAction)
	{
		WebElement checkBox = resultDiv.findElement(EBy.buttonText(action));
		WaitingPageObject<P> removalWaiter = resultOfAction.removalWaiter(checkBox);
		checkBox.click();
		if( confirm == null )
		{
			return removalWaiter.get();
		}
		if( confirm )
		{
			acceptConfirmation();
		}
		else
		{
			cancelConfirmation();
		}
		return removalWaiter.get();
	}
}
