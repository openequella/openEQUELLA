package com.tle.webtests.pageobject.wizard;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

import com.tle.common.Utils;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.factory.RefreshableElement;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;

public class WizardPageTab extends AbstractWizardControlPage<WizardPageTab>
{
	@FindBy(id = "nav_nextButton")
	private WebElement nextButton;
	@FindBy(id = "nav_previousButton")
	private WebElement prevButton;
	@FindBy(id = "wizard-major-actions")
	private WebElement updateAjax;
	@FindBy(xpath = "//div[@id='wizard-pagelist']/ul")
	private WebElement pageList;

	public WizardPageTab(PageContext context, int pageNum)
	{
		super(context, By.className("wizard-layout"), pageNum);
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		try
		{
			String pageNumStr = driver.findElement(By.xpath("//input[@name='pages.pg']")).getAttribute("value");
			if( !pageNumStr.equals(Integer.toString(pageNum)) )
			{
				throw new Error("Wrong page: " + pageNum + ":" + pageNumStr);
			}
		}
		catch( NoSuchElementException nse )
		{
			if( pageNum != 0 )
			{
				throw new Error(nse);
			}
		}
	}

	@Override
	public String getControlId(int ctrlNum)
	{
		return "p" + pageNum + "c" + ctrlNum;
	}

	@Override
	public WaitingPageObject<WizardPageTab> getGeneralWaiter()
	{
		return ajaxUpdate(updateAjax);
	}

	public WizardPageTab setCheckNextAppear(int ctrlnum, String value, boolean checked)
	{
		return setCheckWaiter(ctrlnum, value, checked,
			ExpectWaiter.waiter(ExpectedConditions2.presenceOfElement(nextButton), this));
	}

	public WizardPageTab setCheckNextDisappear(int ctrlnum, String value, boolean checked)
	{
		return setCheckWaiter(ctrlnum, value, checked,
			ExpectWaiter.waiter(ExpectedConditions2.stalenessOrNonPresenceOf(nextButton), this));
	}

	public WizardPageTab next(WaitingPageObject<WizardPageTab> expect)
	{
		nextButton.click();
		pageNum++;
		return expect.get();
	}

	public WizardPageTab next()
	{
		int currentPage = getCurrentPageIndex();
		return next(ExpectWaiter.waiter(new PageCondition(null, currentPage + 1), this));
	}

	public WizardPageTab prev(WaitingPageObject<WizardPageTab> expect)
	{
		prevButton.click();
		pageNum--;
		return get();
	}

	public WizardPageTab prev()
	{
		int currentPage = getCurrentPageIndex();
		return prev(ExpectWaiter.waiter(new PageCondition(null, currentPage - 1), this));
	}

	public boolean hasControl(int ctrlNum)
	{
		try
		{
			driver.findElement(By.id(getControlId(ctrlNum)));
			return true;
		}
		catch( NoSuchElementException e )
		{
			return false;
		}
	}

	public String getNextButtonText()
	{
		if( isPresent(nextButton) )
		{
			return nextButton.getText().replaceAll("<i.*</i>", "").trim();
		}
		return "";
	}

	public String getPrevButtonText()
	{
		if( isPresent(prevButton) )
		{
			return prevButton.getText().replaceAll("<i.*</i>", "").trim();
		}
		return "";
	}

	public boolean hasPage(String text, boolean isLink)
	{
		return isPresent(pageList, By.xpath("li/" + (isLink ? "a" : "span") + "[text()=" + quoteXPath(text) + "]"));
	}

	public boolean deletestatus(String change)
	{
		String trackerStatus = driver.findElement(By.xpath("//div[@id='adjacentuls']/ul[1]/li[4]")).getText();
		return trackerStatus.equals(change);
	}

	public int getCurrentPageIndex()
	{
		final WebElement realElement;
		if( pageList instanceof RefreshableElement )
		{
			realElement = ((RefreshableElement) pageList).findNonWrapped();
		}
		else
		{
			realElement = pageList;
		}

		WebElement currentPageElement = realElement.findElement(By.xpath("li[@class='active']"));
		String id = currentPageElement.getAttribute("id");
		if( id.startsWith("pages_") )
		{
			String numId = Utils.safeSubstring(id, "pages_".length());
			return Integer.parseInt(numId);
		}
		else
		{
			throw new RuntimeException("Current page ID '" + id + "' is not prefixed with 'pages_'");
		}
	}

	public String getCurrentPageName()
	{
		return pageList.findElement(By.xpath("li[@class='active']/span")).getText();
	}

	public void clickPage(String text)
	{
		if( hasPage(text, true) )
		{
			pageList.findElement(By.xpath("li/a[text()=" + quoteXPath(text) + "]")).click();
		}
		else
		{
			throw new RuntimeException("Page '" + text + "' is not present or is not clickable");
		}
	}

	private class PageCondition implements ExpectedCondition<Boolean>
	{
		//private final WebElement realElement;
		private final int expectedPageIndex;

		public PageCondition(WebElement element, int pageIndex)
		{
			//			if( element instanceof RefreshableElement )
			//			{
			//				realElement = ((RefreshableElement) element).findNonWrapped();
			//			}
			//			else
			//			{
			//				realElement = element;
			//			}
			this.expectedPageIndex = pageIndex;
		}

		@Override
		public Boolean apply(WebDriver driver)
		{
			try
			{
				return getCurrentPageIndex() == expectedPageIndex;
			}
			catch( StaleElementReferenceException ser )
			{
				return false;
			}
			catch( NoSuchElementException e )
			{
				return false;
			}
		}

		@Override
		public String toString()
		{
			return "pageCondition " + expectedPageIndex;
		}
	}
}
