package com.tle.webtests.pageobject.searching;

import java.util.Calendar;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public class ItemAdminPage extends AbstractBulkResultsPage<ItemAdminPage, ItemListPage, ItemSearchResult>
{
	@FindBy(id = "status")
	private WebElement statusDropDown;
	private EquellaSelect statusSelect;
	@FindBy(id = "modonly")
	private WebElement modOnlyBox;
	@FindBy(id = "rf_resetButton")
	private WebElement clearFilters;
	@FindBy(id = "iaq_clearQueryButton")
	private WebElement clearQuery;
	@FindBy(id = "fb")
	private WebElement freeCheckbox;
	@FindBy(id = "mb")
	private WebElement manualInclusions;
	@FindBy(id = "ab")
	private WebElement autoInclusions;
	@FindBy(id = "fbp_filter")
	private WebElement purchasedResourcesFilter;

	@FindBy(id = "searchform-where")
	private WebElement whereDiv;
	@FindBy(id = "itemadmin-page")
	private WebElement mainElem;

	public ItemAdminPage(PageContext context)
	{
		super(context);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/itemadmin.do");
	}

	public WebElement getSelectedContainer()
	{
		return driver.findElement(By.xpath("id('searchform')//div[contains(@class, 'selectedcontainer')]"));
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
	}

	public ItemAdminPage filterByStatus(String status)
	{
		openFilters();
		statusSelect = new EquellaSelect(context, statusDropDown);
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		statusSelect.selectByValue(status);
		return waitForResultsReload(waiter);
	}

	protected void setWithin(String name)
	{
		WebElement selectedContainer = getSelectedContainer();
		selectedContainer.click();
		By richdropdown = By.xpath("//div[contains(@class,'richdropdown active')]");
		waitForElement(richdropdown);
		driver.findElement(richdropdown).findElement(By.xpath("ul/li/a[text()=" + quoteXPath(name) + "]")).click();
	}

	public ItemAdminPage setWithinAll()
	{
		return setWithinCollection("Within all resources");
	}

	public ItemAdminPage setWithinCollection(String collection)
	{
		AutoCompleteOptions autoCompleteOptions = new AutoCompleteOptions(querySection);
		if( autoCompleteOptions.isLoaded() )
		{
			autoCompleteOptions.close();
		}
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		setWithin(collection);
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage searchWithinCatalogue(PrefixedName catalogue)
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		setWithin(catalogue.toString());
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage filterByWorkflow(String workflow)
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		// Throw an exception if it isn't there
		WebElement workFlowDropDown = driver.findElement(By.id("workflow"));
		EquellaSelect box = new EquellaSelect(context, workFlowDropDown);
		box.selectByVisibleText(workflow);
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage filterBySubscriptionTier(String subscriptionTier)
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		WebElement tierDropdown = driver.findElement(By.id("stl"));
		EquellaSelect tierSelect = new EquellaSelect(context, tierDropdown);
		tierSelect.selectByVisibleText(subscriptionTier);
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage filterByFree()
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		freeCheckbox.click();
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage filterByPurchaseTier(String purchaseTier)
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		WebElement tierDropdown = driver.findElement(By.id("ptl"));
		EquellaSelect tierSelect = new EquellaSelect(context, tierDropdown);
		tierSelect.selectByVisibleText(purchaseTier);
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage filterByPurchased()
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		purchasedResourcesFilter.click();
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage clearFilters()
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		clearFilters.click();
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage clearQuery()
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		clearQuery.click();
		return waitForResultsReload(waiter);
	}

	public ItemAdminPage all()
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		openFilters();
		statusSelect = new EquellaSelect(context, statusDropDown);
		statusSelect.selectByIndex(0);
		return waitForResultsReload(waiter);
	}

	public SummaryPage viewItem(String itemName)
	{
		return all().exactQuery(itemName).viewFromTitle(itemName);
	}

	public SummaryPage viewItem(PrefixedName itemName)
	{
		return viewItem(itemName.toString());
	}

	@Override
	public ItemListPage resultsPageObject()
	{
		return new ItemListPage(context);
	}

	public ItemAdminPage setModOnly(boolean checked)
	{
		openFilters();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		boolean isChecked = modOnlyBox.isSelected();
		if( checked != isChecked )
			modOnlyBox.click();
		return waitForResultsReload(waiter);
	}

	// Sets the where under the catalogue within, so the catalogue within needs
	// to be set first. acceptable values are (live, pending, excluded)
	public ItemListPage setCatalogueWhere(String type)
	{
		if( type.equals("live") )
		{
			type = "com.tle.web.payment.backend.search.query.where.live";
		}
		else if( type.equals("pending") )
		{
			type = "com.tle.web.payment.backend.search.query.where.pending";
		}
		else if( type.equals("excluded") )
		{
			type = "com.tle.web.payment.backend.search.query.where.excluded";
		}
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		WebElement where = driver.findElement(By.id("cwl"));
		new EquellaSelect(context, where).selectByValue(type);
		return waiter.get();
	}

	public ItemListPage setManualInclusions(boolean b)
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		openFilters();
		if( manualInclusions.isSelected() != b )
		{
			manualInclusions.click();
			return waiter.get();
		}
		return resultsPageObject;
	}

	public ItemListPage setAutoInclusions(boolean b)
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		openFilters();
		if( autoInclusions.isSelected() != b )
		{
			autoInclusions.click();
			return waiter.get();
		}
		return resultsPageObject;
	}

	public WhereQueryPage<ItemAdminPage> editWhere()
	{
		whereDiv.findElement(By.id("searchform-editquery")).click();
		return new WhereQueryPage<ItemAdminPage>(context, this).get();
	}

	public void saveSearch(String searchName)
	{
		FavouriteSearchDialog dialog = new FavouriteSearchDialog(context, "iafsa").open();
		ReceiptPage.waiter("Successfully added this search to your favourites", dialog.favourite(searchName, this))
			.get();
		ReceiptPage.dismiss(waiter);
	}
}
