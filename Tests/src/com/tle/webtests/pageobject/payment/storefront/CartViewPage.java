package com.tle.webtests.pageobject.payment.storefront;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.collect.Sets;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.WaitingPageObject;

public class CartViewPage extends AbstractPage<CartViewPage>
{
	private static final String PAGE_TITLE = "Shopping cart";

	private final Set<CartStorePart> parts = Sets.newHashSet();

	@FindBy(id = "svc_c")
	private WebElement commentBox;
	@FindBy(id = "svc_submitButton")
	private WebElement submitForApprovalButton;
	@FindBy(xpath = "//div[contains(@class,'bottomTotal')]/div")
	private WebElement bottomTotal;
	@FindBy(xpath = "//div[contains(@class,'topTotal')]/div")
	private WebElement topTotal;

	@FindBy(css = ".area.order")
	private WebElement root;

	public CartViewPage(PageContext context)
	{
		super(context, By.xpath("//div[@class='banner' and text() = " + quoteXPath(PAGE_TITLE) + "]"));
	}

	public String getBottomTotal()
	{
		return bottomTotal.getText();
	}

	public String getTopTotal()
	{
		return topTotal.getText();
	}

	public CartViewPage setComment(String comment)
	{
		commentBox.clear();
		commentBox.sendKeys(comment);
		return this;
	}

	public OrderPage submitForApproval()
	{
		WaitingPageObject<OrderPage> waiter = ExpectWaiter.waiter(removalCondition(submitForApprovalButton),
			new OrderPage(context));
		submitForApprovalButton.click();
		return waiter.get();
	}

	public boolean isMultipleCurrencies()
	{
		return getBottomTotal().equals("Multiple currencies");
	}

	public CartStorePart getStoreSection(String storeName)
	{
		By by = By.xpath(".//div[@class = 'storeSection']/div[@class = 'storeTitle']/h3[text() = "
			+ quoteXPath(storeName) + "]/../..");
		CartStorePart part = new CartStorePart(context, root, by).get();
		parts.add(part);
		return part;
	}

	public boolean doesStoreCartExist(String storeName)
	{
		return isPresent(By.xpath("//div[@class = 'storeTitle']/h3[text() = " + quoteXPath(storeName) + "]"));
	}

	public CartViewPage removeAll()
	{
		while( isPresent(By.xpath("//div[@class = 'storeSection']")) )
		{
			By by = By.xpath(".//div[@class = 'storeSection']");
			new CartStorePart(context, root, by).removeAll();
		}

		return this;
	}

	protected void updateAjax()
	{
		Iterator<CartStorePart> it = parts.iterator();
		while( it.hasNext() )
		{
			CartStorePart part = it.next();
			try
			{
				part.checkLoaded();
			}
			catch( NoSuchElementException nse )
			{
				it.remove();
			}
		}
	}

	public class CartStorePart extends AbstractPage<CartStorePart>
	{
		@FindBy(className = "removeAll")
		private WebElement removeAllButton;

		// Not ideal, change these XPaths if you know of a better way
		@FindBy(xpath = "//div[contains(@class,'shopGateways')]/div/button[text() = ' Download resources']")
		private WebElement freeGatewayButton;
		@FindBy(xpath = "//img[contains (@src, \"google\")]")
		private WebElement googleGatewayButton;
		@FindBy(xpath = "//img[contains (@src, \"paypal\")]")
		private WebElement paypalGatewayButton;
		@FindBy(xpath = "//img[contains (@src, \"fake\")]")
		private WebElement fakeGatewayButton;

		public CartStorePart(PageContext context, SearchContext div, By divBy)
		{
			super(context, div, divBy);
		}

		@Override
		public SearchContext getSearchContext()
		{
			return findLoadedElement();
		}

		public boolean isGone()
		{
			try
			{
				findLoadedElement();
				return false;
			}
			catch( NoSuchElementException nse )
			{
				return true;
			}
		}

		public OrderPage payWithDemoGateway()
		{
			fakeGatewayButton.click();
			return new OrderPage(context).get();
		}

		public boolean isFreeButtonPresent()
		{
			return isPresent(freeGatewayButton);
		}

		public boolean isPaypalButtonPresent()
		{
			return isPresent(paypalGatewayButton);
		}

		public boolean isDemoGatewayButtonPresent()
		{
			return isPresent(fakeGatewayButton);
		}

		public String getTotal()
		{
			return getSearchContext().findElement(By.xpath(".//div[@class = 'shopTotal']/div")).getText();
		}

		public OrderPage downloadResources()
		{
			getSearchContext().findElement(
				By.xpath(".//div[@class = 'shopGateways']//button[normalize-space(text()) = 'Download resources']"))
				.click();
			return new OrderPage(context).get();
		}

		public boolean isRemoveAllButtonPresent()
		{
			return isPresent(removeAllButton);
		}

		public void removeAll()
		{
			WaitingPageObject<CartViewPage> waiter = CartViewPage.this.removalWaiter(removeAllButton);
			removeAllButton.click();
			acceptConfirmation();
			waiter.get();
			CartViewPage.this.updateAjax();
		}

		public CartStorePart removeAllCancel()
		{
			removeAllButton.click();
			driver.switchTo().alert().dismiss();
			return this;
		}

		private WebElement getTable()
		{
			return getSearchContext().findElement(By.xpath(".//table"));
		}

		public boolean isResourceInCart(String resourceName)
		{
			List<WebElement> itemnames = getTable().findElements(By.xpath(".//tbody/tr/td[1]"));
			for( WebElement itemname : itemnames )
			{
				if( itemname.getText().equals(resourceName) )
				{
					return true;
				}
			}
			return false;
		}

		public CartStorePart removeResourceFromCart(String resourceName)
		{
			WebElement unselectButton = getTable().findElement(
				By.xpath("./tbody/tr/td[1]/span[text() = " + quoteXPath(resourceName)
					+ "]/../../td/a[contains(@class,'unselect')]"));
			WaitingPageObject<CartViewPage> waiter = CartViewPage.this.removalWaiter(unselectButton);
			unselectButton.click();
			acceptConfirmation();
			waiter.get();
			CartViewPage.this.updateAjax();
			return this;
		}

		public boolean startDateColumnPresent()
		{
			return isPresent(getTable(), By.xpath(".//thead/tr/th[text() = 'Start date']"));
		}

		public boolean usersColumnPresent()
		{
			return isPresent(getTable(), By.xpath(".//thead/tr/th[text() = '# Users']"));
		}

		public boolean priceColumnHasDuration()
		{
			return getTable().findElement(By.xpath(".//thead/tr/th[2]")).getText().equalsIgnoreCase("Duration / Price");
		}
	}
}
