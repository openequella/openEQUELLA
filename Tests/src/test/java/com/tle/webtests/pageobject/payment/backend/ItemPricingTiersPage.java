package com.tle.webtests.pageobject.payment.backend;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

/**
 * @author Dinuk
 */

public class ItemPricingTiersPage extends AbstractPage<ItemPricingTiersPage>
{
	public static final String SELECT_PRICING_TIERS_TITLE = "Set pricing tiers";
	public static final String RECEIPT_SAVE = "The pricing tier was successfully saved";

	private boolean purchaseTierPresentStatus;

	// Purchase Tier List Box
	@FindBy(id = "{sectionId}_ptl")
	private WebElement purchaseTierListBox;

	// Subscription Tier Table
	@FindBy(id = "{sectionId}_stt")
	private WebElement subscripitonTierTable;

	// Save Pricing Button
	@FindBy(id = "{sectionId}_saveButton")
	private WebElement savePricingButton;

	// Save Confirmation Message
	@FindBy(id = "receipt-message")
	private WebElement saveConfirmation;

	@FindBy(id = "{sectionId}_fb")
	private WebElement freeCheckBox;

	@FindBy(id = "{sectionId}_stt")
	private WebElement subscriptionTable;

	public ItemPricingTiersPage(PageContext context)
	{
		super(context, By.xpath("//h2[text()=" + quoteXPath(SELECT_PRICING_TIERS_TITLE) + ']'));
	}

	public String getSectionId()
	{
		return "cpt";
	}

	public boolean isPurchaseTierListPresent()
	{
		return isPresent(purchaseTierListBox);
	}

	public boolean isSubscriptionTierTablePresent()
	{
		return isPresent(subscripitonTierTable);
	}

	// Verify Purchase Tier Present in List Box - Dinuk
	public boolean isPurchaseTierPresent(String purchaseTierName)
	{
		EquellaSelect purchaseTierList = new EquellaSelect(context, purchaseTierListBox);
		List<WebElement> options = purchaseTierList.getSelectableHyperinks();
		purchaseTierPresentStatus = false;
		for( WebElement opt : options )
		{
			if( opt.getText().equals(purchaseTierName) )
			{
				opt.click();
				purchaseTierPresentStatus = true;
				break;
			}
		}
		return purchaseTierPresentStatus;
	}

	// Confirm Save Pricing
	public String confirmSaveMessage()
	{
		return saveConfirmation.getText();
	}

	public ItemPricingTiersPage save()
	{
		return save(ReceiptPage.waiter(RECEIPT_SAVE, this));
	}

	// Save Pricing
	private <T extends PageObject> T save(WaitingPageObject<T> returnTo)
	{
		savePricingButton.click();
		return returnTo.get();
	}

	public SubscriptionTierRow getSubscriptionTier(String name)
	{
		return new SubscriptionTierTable(context, subscripitonTierTable).getTier(name);
	}

	public Set<String> getSubscriptions()
	{
		Set<String> subs = Sets.newHashSet();
		List<WebElement> elems = subscripitonTierTable.findElements(By.xpath("./tbody/tr/td[2]"));
		for( WebElement elem : elems )
		{
			subs.add(elem.getText());
		}
		return subs;
	}

	public ItemPricingTiersPage selectOutrightPricingTier(String name)
	{
		EquellaSelect purchaseTierSelect = new EquellaSelect(context, purchaseTierListBox);
		purchaseTierSelect.selectByVisibleText(name);
		return this;
	}

	public ItemPricingTiersPage setFree(boolean free)
	{
		if( freeCheckBox.isSelected() != free )
		{
			freeCheckBox.click();
		}
		return this;
	}

	public class SubscriptionTierTable extends AbstractPage<SubscriptionTierRow>
	{
		private final WebElement root;
		private Map<String, Integer> periodMap;

		public SubscriptionTierTable(PageContext context, WebElement root)
		{
			super(context);
			this.root = root;
		}

		@Override
		public WebElement getLoadedElement()
		{
			return root;
		}

		public SubscriptionTierRow getTier(String name)
		{
			WebElement row = root.findElement(By.xpath("./tbody/tr/td[text()=" + quoteXPath(name) + "]/.."));
			return new SubscriptionTierRow(context, this, row);
		}

		public synchronized int getPeriodIndex(String name)
		{
			if( periodMap == null )
			{
				periodMap = Maps.newHashMap();

				List<WebElement> headers = root.findElements(By.xpath("./thead/tr/th"));
				int column = 0;
				for( WebElement th : headers )
				{
					column++;
					periodMap.put(th.getText(), column);
				}
			}
			return periodMap.get(name);
		}
	}

	public class SubscriptionTierRow extends AbstractPage<SubscriptionTierRow>
	{
		private final SubscriptionTierTable table;
		private final WebElement row;

		@FindBy(xpath = "./td[1]/input[@type='radio']")
		private WebElement radio;
		@FindBy(xpath = "./td[2]")
		private WebElement name;

		public SubscriptionTierRow(PageContext context, SubscriptionTierTable table, WebElement row)
		{
			super(context);
			this.row = row;
			this.table = table;
		}

		@Override
		public WebElement getLoadedElement()
		{
			return row;
		}

		@Override
		public SearchContext getSearchContext()
		{
			return row;
		}

		public String getName()
		{
			return name.getText();
		}

		public String getValue(String period)
		{
			int idx = table.getPeriodIndex(period);
			WebElement td = getSearchContext().findElement(By.xpath("./td[" + idx + "]"));
			return td.getText();
		}

		public boolean isSelected()
		{
			return radio.isSelected();
		}

		public void select()
		{
			if( !isSelected() )
			{
				radio.click();
			}
		}
	}
}
