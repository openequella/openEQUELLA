package com.tle.webtests.pageobject.payment.backend;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

/**
 * @author Aaron
 */
public class ShowTiersPage extends AbstractPage<ShowTiersPage>
{
	private static final String TITLE = "Pricing tiers";

	@FindBy(id = "to_c")
	protected WebElement selectCurrency;
	@FindBy(id = "to_f")
	protected WebElement free;
	@FindBy(id = "to_p")
	protected WebElement purchase;
	@FindBy(id = "to_s")
	protected WebElement subscription;

	@FindBy(id = "spt_fr")
	protected WebElement purchaseFlatRate;
	@FindBy(id = "spt_add")
	protected WebElement addPurchaseTierLink;

	@FindBy(id = "sst_fr")
	protected WebElement subFlatRate;
	@FindBy(id = "sst_add")
	protected WebElement addSubTierLink;
	

	public ShowTiersPage(PageContext context)
	{
		super(context, (By) null);
		loadedBy = getLoadedBy();
	}

	protected By getLoadedBy()
	{
		return By.xpath("//div[@class='entities']//h2[normalize-space(text())=" + quoteXPath(TITLE) + "]");
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/tier.do");
	}

	public ShowTiersPage setCurrency(String currencyCode)
	{
		new EquellaSelect(context, selectCurrency).selectByValue(currencyCode);
		acceptConfirmation();
		return this.get();
	}
	
	public String getCurrency()
	{
		return new EquellaSelect(context, selectCurrency).getSelectedText();
	}

	public ShowTiersPage enableFree(boolean enable)
	{
		final boolean checked = free.isSelected();
		if( checked && !enable || !checked && enable )
		{
			free.click();
		}
		return get();
	}

	public ShowTiersPage enablePurchase(boolean enable)
	{
		final boolean checked = purchase.isSelected();
		if( checked && !enable || !checked && enable )
		{
			WaitingPageObject<ShowTiersPage> waiter = showWaiter(addPurchaseTierLink, enable);
			purchase.click();
			return waiter.get();
		}
		return this;
	}

	public ShowTiersPage enableSubscription(boolean enable)
	{
		final boolean checked = subscription.isSelected();
		if( checked && !enable || !checked && enable )
		{
			WaitingPageObject<ShowTiersPage> waiter = showWaiter(addSubTierLink, enable);
			subscription.click();
			return waiter.get();
		}
		return this;
	}

	public boolean isFree()
	{
		return free.isSelected();
	}

	public boolean isSubscribe()
	{
		return subscription.isSelected();
	}

	public boolean isPurchase()
	{
		return purchase.isSelected();
	}

	public boolean tierExists(String name, boolean subscription)
	{
		return isPresent(By.xpath(getXpath(name, subscription, false)));
	}

	public String getPriceForPurchaseTier(String name)
	{
		return driver.findElement(By.xpath(getXpath(name, false, false) + "/td[2]")).getText();
	}

	public String getPriceForSubscriptionTier(String name, String duration)
	{
		return driver.findElement(
			By.xpath(getXpath(name, true, false) + "/td[count(//thead/tr/th[text()=" + quoteXPath(duration)
				+ "]/preceding-sibling::*)+1]")).getText();
	}

	public boolean actionExists(String entityName, String action, boolean subscription)
	{
		return isPresent(By.xpath(getActionXPath(entityName, action, subscription, false)));
	}

	public ShowTiersPage deleteTier(String name, boolean subscription, boolean startsWith)
	{
		if( isTierEnabled(name, subscription, startsWith) )
		{
			disableTier(name, subscription, startsWith);
		}
		WebElement element = driver.findElement(By.xpath(getActionXPath(name, "Delete", subscription, startsWith)));
		WaitingPageObject<ShowTiersPage> waiter = removalWaiter(element);
		element.click();
		acceptConfirmation();
		return waiter.get();
	}

	public ShowTiersPage disableTier(String name, boolean subscription, boolean startsWith)
	{
		WebElement element = driver.findElement(By.xpath(getActionXPath(name, "Disable", subscription, startsWith)));
		WaitingPageObject<ShowTiersPage> waiter = removalWaiter(element);
		element.click();
		return waiter.get();
	}

	public boolean isTierEnabled(String name, boolean subscription, boolean startsWith)
	{
		return isPresent(By.xpath(getActionXPath(name, "Disable", subscription, startsWith)));
	}

	public EditTierPage createTier(boolean subscription)
	{
		getAddLink(subscription).click();
		return getEditPage(true, subscription).get();
	}

	public EditTierPage editTier(String entityName, boolean subscription)
	{
		WebElement element = driver.findElement(By.xpath(getActionXPath(entityName, "Edit", subscription, false)));
		element.click();
		return getEditPage(false, subscription).get();
	}

	public EditTierPage cloneTier(String entityName, boolean subscription)
	{
		WebElement element = driver.findElement(By.xpath(getActionXPath(entityName, "Clone", subscription, false)));
		element.click();
		return getEditPage(true, subscription).get();
	}

	protected String getActionXPath(String entityName, String action, boolean subscription, boolean startsWith)
	{
		return getXpath(entityName, subscription, startsWith) + "/td/a[text()=" + quoteXPath(action) + "]";
	}

	protected String getXpath(String name, boolean subscription, boolean startsWith)
	{
		if( startsWith )
		{
			return "//div[@id='" + (subscription ? "subt" : "purt")
				+ "']//td[@class='name' and starts-with(normalize-space(text()), " + quoteXPath(name) + ")]/..";
		}
		else
		{
			return "//div[@id='" + (subscription ? "subt" : "purt")
				+ "']//td[@class='name' and normalize-space(text())=" + quoteXPath(name) + "]/..";
		}
	}

	protected WebElement getAddLink(boolean subscription)
	{
		return (subscription ? addSubTierLink : addPurchaseTierLink);
	}

	protected EditTierPage getEditPage(boolean creating, boolean subscription)
	{
		return new EditTierPage(context, creating, subscription).get();
	}

	protected String getEmptyText(boolean subscription)
	{
		return (subscription ? "There are no subscription pricing tiers configured"
			: "There are no purchase pricing tiers configured");
	}

	public void clearTierWithPrefix(String prefix, boolean subscription)
	{
		String div = subscription ? "subt" : "purt";
		List<WebElement> rows = getTierRows(div);
		int numRowsToBeginWith = rows.size();
		for( int i = 0; i < numRowsToBeginWith; ++i )
		{
			if( isTierEnabled(prefix, subscription, true) )
			{
				disableTier(prefix, subscription, true);
			}
			deleteTier(prefix, subscription, true);
		}
		get();
	}

	private List<WebElement> getTierRows(String div)
	{
		List<WebElement> rows = driver.findElements(By.xpath("//div[@id='" + div + "']//td[@class='name']"));
		return rows;
	}
}
