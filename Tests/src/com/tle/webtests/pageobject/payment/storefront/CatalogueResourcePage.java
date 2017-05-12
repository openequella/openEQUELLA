package com.tle.webtests.pageobject.payment.storefront;

import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.Calendar;
import com.tle.webtests.pageobject.viewitem.ItemPage;

public class CatalogueResourcePage extends ItemPage<CatalogueResourcePage>
{
	@FindBy(id = "sipd_nus")
	private WebElement numberOfUsersSubscribe;
	@FindBy(id = "sipd_nup")
	private WebElement numberOfUsersPurchase;
	@FindBy(id = "sipd_addToCartButton")
	private WebElement addToCartButton;
	@FindBy(id = "sipd_removeFromCartButton")
	private WebElement removeFromCartButton;
	@FindBy(id = "scb_viewCartButton")
	private WebElement viewCartButton;
	@FindBy(id = "sipd_pricingTable")
	private WebElement pricingTable;
	@FindBy(id = "sipd_pt_0")
	private WebElement purchaseRadio;
	@FindBy(id = "sipd_pt_1")
	private WebElement subscribeRadio;
	@FindBy(id = "sipd_sd_0")
	private WebElement paymentDateRadio;
	@FindBy(id = "sipd_sd_1")
	private WebElement otherDateRadio;
	@FindBy(id = "sipd_sdtvis")
	private WebElement otherDateField;
	@FindBy(id = "sipd_pbt")
	private WebElement previousPurchasersTable;
	@FindBy(xpath = "//div[@class='pricing']")
	private WebElement pricingDiv;

	@FindBy(id = "updateTotal")
	private WebElement totalDiv;

	private Calendar otherDate;

	public CatalogueResourcePage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return pricingDiv;
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		otherDate = new Calendar(context, "sipd_sdt");
	}

	public boolean isOutrightPurchase()
	{
		return (isPresent(By.id("purchase_div")) && !isPresent(By.id("subscription_div")));
	}

	public boolean isSubscription()
	{
		return (!isPresent(By.id("purchase_div")) && isPresent(By.id("subscription_div")));
	}

	public boolean isSubandOutright()
	{
		return (isPresent(By.id("purchase_div")) && isPresent(By.id("subscription_div")));
	}

	public boolean isFree()
	{
		return (isPresent(By.xpath("//div[@class='pricing']/div[text() = 'Free']")) && !isSubandOutright());
	}

	public boolean isPurchaseSelected()
	{
		if( isSubandOutright() )
		{
			if( purchaseRadio.isSelected() )
			{
				return true;
			}
		}
		return false;
	}

	public CatalogueResourcePage setPurchaseModel()
	{
		if( !purchaseRadio.isSelected() )
		{
			WaitingPageObject<CatalogueResourcePage> ajaxUpdate = ajaxUpdate(totalDiv);
			purchaseRadio.click();
			ajaxUpdate.get();
		}
		return get();
	}

	public CatalogueResourcePage setSubscriptionModel()
	{
		if( !subscribeRadio.isSelected() )
		{
			WaitingPageObject<CatalogueResourcePage> ajaxUpdate = ajaxUpdate(totalDiv);
			subscribeRadio.click();
			ajaxUpdate.get();
		}
		return get();
	}

	public void setNumberOfUsers(boolean subscription, String number)
	{
		WaitingPageObject<CatalogueResourcePage> ajaxUpdate = ajaxUpdate(totalDiv);
		if( subscription )
		{
			numberOfUsersSubscribe.clear();
			numberOfUsersSubscribe.sendKeys(number);
		}
		else
		{
			numberOfUsersPurchase.clear();
			numberOfUsersPurchase.sendKeys(number);
		}
		ajaxUpdate.get();
	}

	public int getNumberOfUsers(boolean subscription)
	{
		if( subscription )
		{
			return Integer.parseInt(numberOfUsersSubscribe.getAttribute("value"));
		}
		else
		{
			return Integer.parseInt(numberOfUsersPurchase.getAttribute("value"));
		}
	}

	public String getTotal()
	{
		return totalDiv.getText();
	}

	public String getPerUnitOutrightPrice()
	{
		String priceText = driver.findElement(By.xpath("//div[@id = 'purchase_div']/p[1]")).getText();

		return priceText;
	}

	public CatalogueResourcePage selectDuration(String name)
	{
		new PeriodTable(context, pricingTable).getRowByDuration(name).get().select();
		return this;
	}

	// not tested
	public String getSelectedDurationPrice()
	{
		List<WebElement> radioColumn = pricingTable.findElements(By.xpath("//tbody/tr/td[1]/input"));
		int index = 0;
		for( WebElement radioButton : radioColumn )
		{
			index++;
			if( radioButton.isSelected() )
			{
				break;
			}
		}

		String durationPrice = pricingTable.findElement(By.xpath("//tbody/tr[" + index + "]/td[3]")).getText();
		return durationPrice;
	}

	public CatalogueResourcePage addToCart()
	{
		addToCartButton.click();
		waitForElement(removeFromCartButton);
		return this;
	}

	public CatalogueResourcePage removeFromCart()
	{
		removeFromCartButton.click();
		waitForElement(addToCartButton);
		return this;
	}

	public CatalogueResourcePage addToCartWithErrors()
	{
		addToCartButton.click();
		waitForElement(By.xpath("//div[contains(@class,'alert-error')]"));
		return this;
	}

	public CartViewPage viewCart()
	{
		viewCartButton.click();
		return new CartViewPage(context).get();
	}

	public void changeDuration(int index)
	{
		WebElement radioButton = driver.findElement(By.xpath("//table[@id='sipd_pricingTable']/tbody/tr[" + index
			+ "]/td[1]/input"));
		if( !radioButton.isSelected() )
		{
			WaitingPageObject<CatalogueResourcePage> ajaxUpdate = ajaxUpdate(totalDiv);
			radioButton.click();
			ajaxUpdate.get();
		}
	}

	public <T extends PageObject> T clickBreadcrumb(PrefixedName text, WaitingPageObject<T> returnTo)
	{
		return clickBreadcrumb(text.toString(), returnTo);
	}

	public <T extends PageObject> T clickBreadcrumb(String text, WaitingPageObject<T> returnTo)
	{
		driver.findElement(By.xpath("//div[@id='breadcrumbs']/span//a[text() = " + quoteXPath(text) + "]")).click();
		return returnTo.get();
	}

	public void selectOtherDate()
	{
		otherDateRadio.click();
		waitForElement(otherDateField);
	}

	public void selectPaymentDate()
	{
		paymentDateRadio.click();
	}

	public void setOtherDate(java.util.Calendar cal)
	{
		Calendar otherDate = new Calendar(context, "sipd_sdt").get();
		otherDate.setDate(cal, this);
	}

	public Date getOtherDate()
	{
		return otherDate.getHiddenDateValue();
	}

	public boolean isPaymentDateSelected()
	{
		return paymentDateRadio.isSelected();
	}

	public boolean isNumberOfUsersInvalid(boolean subscription)
	{
		String divId = subscription ? "subscription_div" : "purchase_div";
		return isPresent(By.xpath("//div[@id = '" + divId + "']/div[contains(@class,'alert-error')]"));
	}

	public boolean isOtherDateInvalid()
	{
		return isPresent(By.xpath("//div[@id='subscription_div']/div/div[contains(@class,'alert-error')]"));
	}

	public boolean isMultiTieredDisabled(boolean perUser)
	{
		// TODO: purchase table disabling check
		if( perUser )
		{
			return !(purchaseRadio.isEnabled() && numberOfUsersPurchase.isEnabled() && subscribeRadio.isEnabled()
				&& paymentDateRadio.isEnabled() && numberOfUsersSubscribe.isEnabled() && otherDateRadio.isEnabled() && otherDateField
					.isEnabled());
		}
		else
		{
			return !(purchaseRadio.isEnabled() && subscribeRadio.isEnabled() && paymentDateRadio.isEnabled()
				&& otherDateRadio.isEnabled() && otherDateField.isEnabled());
		}
	}

	public boolean isOtherDateDisabled()
	{
		return !otherDateField.isEnabled();
	}

	public boolean isPreview(String attachmentTitle)
	{
		String xpath = "//div[@id = 'svi_div']/ul/li/div/a[text() = " + quoteXPath(attachmentTitle + " (preview)")
			+ "]";
		return isPresent(By.xpath(xpath));
	}

	public <T extends PageObject> T previewAttachment(String title, WaitingPageObject<T> page)
	{
		driver.findElement(
			By.xpath("//div[@id = 'svi_div']/ul/li/div/a[text() = " + quoteXPath(title + " (preview)") + "]")).click();
		return page.get();
	}

	public String getPreviewLink(String title)
	{
		WebElement link = driver.findElement(By.xpath("//div[@id = 'svi_div']/ul/li/div/a[text() = "
			+ quoteXPath(title + " (preview)") + "]"));
		return link.getAttribute("href");
	}

	public boolean isResourcePurchased()
	{
		// 1 alert-info div on the page, used for the purchased-already warning
		return isPresent(By.xpath("//div[contains(@class, 'alert-info')]"));
	}

	public String getPurchasedMessage()
	{
		return driver.findElement(By.xpath("//div[contains(@class,'alert-info')]")).getText();
	}

	public boolean isAddToCartButtonPresent()
	{
		return isPresent(addToCartButton);
	}

	public boolean isPreviousPurchasersTablePresent()
	{
		return isPresent(previousPurchasersTable);
	}

	public abstract class Table<THIS extends Table<THIS, R>, R extends TableRow<THIS, R>> extends AbstractPage<THIS>
	{
		private final WebElement table;

		public Table(PageContext context, WebElement table)
		{
			super(context);
			this.table = table;
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return table;
		}

		@Override
		public SearchContext getSearchContext()
		{
			return table;
		}

		protected By getByForRowIndex(int index)
		{
			return By.xpath("tbody/tr[" + index + "]");
		}

		public abstract R getRow(int index);
		// {
		// return new TableRow(this, getByForRowIndex(index));
		// }
	}

	public abstract class TableRow<TABLE extends Table<TABLE, THIS>, THIS extends TableRow<TABLE, THIS>>
		extends
			AbstractPage<THIS>
	{
		private final TABLE table;

		protected TableRow(TABLE table, By by)
		{
			super(table.getContext(), table.getSearchContext(), by);
			this.table = table;
		}

		@Override
		protected WebElement findLoadedElement()
		{
			return context.getDriver().findElement(loadedBy);
		}

		@Override
		public SearchContext getSearchContext()
		{
			WebElement elem = getLoadedElement();
			if( elem == null )
			{
				elem = findLoadedElement();
			}
			return elem;
		}

		public TABLE getTable()
		{
			return table;
		}

		protected WebElement getCell(int index)
		{
			return getSearchContext().findElement(By.xpath("td[" + index + "]"));
		}
	}

	public class PeriodTable extends Table<PeriodTable, PeriodRow>
	{
		public PeriodTable(PageContext context, WebElement table)
		{
			super(context, table);
		}

		@Override
		public PeriodRow getRow(int index)
		{
			return new PeriodRow(this, getByForRowIndex(index));
		}

		public PeriodRow getRowByDuration(String duration)
		{
			return new PeriodRow(this, By.xpath(".//tr/td[2][normalize-space(child::text()) = "
				+ AbstractPage.quoteXPath(duration) + "]/.."));
		}
	}

	public class PeriodRow extends TableRow<PeriodTable, PeriodRow>
	{
		@FindBy(xpath = "td[1]/input")
		private WebElement radio;

		protected PeriodRow(PeriodTable table, By by)
		{
			super(table, by);

		}

		public PeriodRow select()
		{
			radio.click();
			return this;
		}
	}

	public boolean waitForTotal(String secondExpectedPrice)
	{
		waiter.until(ExpectedConditions2.elementTextToBe(totalDiv, secondExpectedPrice));
		return totalDiv.getText().equals(secondExpectedPrice);
	}
}
