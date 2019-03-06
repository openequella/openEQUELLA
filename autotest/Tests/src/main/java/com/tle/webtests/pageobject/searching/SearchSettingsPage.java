package com.tle.webtests.pageobject.searching;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;

public class SearchSettingsPage extends AbstractPage<SearchSettingsPage>
{
	public static final String SEARCH_SETTINGS_SECTION_TITLE = "Searching";

	public enum Order
	{
		RANK, DATEMODIFIED, NAME, RATING
	}

	@FindBy(id = "_showNonLiveCheckbox")
	private WebElement includeNonLive;

	@FindBy(id = "_authenticateByDefault")
	private WebElement genAuthFeeds;

	@FindBy(id = "_saveButton")
	private WebElement save;

	@FindBy(id = "_newFilterLink")
	private WebElement newFilterLink;

	@FindBy(id = "_harvestOptions_0")
	private WebElement noIndexingRadio;
	@FindBy(id = "_harvestOptions_1")
	private WebElement indexPageOnlyRadio;
	@FindBy(id = "_harvestOptions_2")
	private WebElement index2ndryPagesRadio;

	@FindBy(id = "cs_dc")
	private WebElement disableCloud;

	private EquellaSelect resultOrder;

	public SearchSettingsPage(PageContext context)
	{
		super(context, By.xpath("//h2[text()='" + SEARCH_SETTINGS_SECTION_TITLE + "']"));
	}

	@Override
	protected void loadUrl()
	{
		driver.get(context.getBaseUrl() + "access/searchsettings.do");
	}

	@Override
	public void checkLoaded() throws Error
	{
		super.checkLoaded();
		resultOrder = new EquellaSelect(context, driver.findElement(By.id("_defaultSortType")));
	}

	public SearchSettingsPage setOrder(Order order)
	{
		resultOrder.selectByValue(order.name());
		return get();
	}

	public SearchSettingsPage includeNonLive(boolean showNonLive)
	{
		if( includeNonLive.isSelected() != showNonLive )
		{
			includeNonLive.click();
		}
		return get();
	}

	public SearchSettingsPage setGenerateAuthFeeds(boolean authFeeds)
	{
		if( genAuthFeeds.isSelected() != authFeeds )
		{
			genAuthFeeds.click();
		}
		return get();
	}

	public SearchSettingsPage setNoIndexingOfPages()
	{
		if( !noIndexingRadio.isSelected() )
		{
			noIndexingRadio.click();
		}
		return get();
	}

	public SearchSettingsPage setDisableCloud(boolean disable)
	{
		if( disableCloud.isSelected() != disable )
		{
			disableCloud.click();
		}
		return get();
	}

	public boolean isNoIndexingOfPages()
	{
		return noIndexingRadio.isSelected();
	}

	public SearchSettingsPage setIndexPageOnly()
	{
		if( !indexPageOnlyRadio.isSelected() )
		{
			indexPageOnlyRadio.click();
		}
		return get();
	}

	public boolean isIndexPageOnly()
	{
		return indexPageOnlyRadio.isSelected();
	}

	public SearchSettingsPage setIndex2ndryPages()
	{
		if( !index2ndryPagesRadio.isSelected() )
		{
			index2ndryPagesRadio.click();
		}
		return get();
	}

	public boolean isIndex2ndryPages()
	{
		return index2ndryPagesRadio.isSelected();
	}

	public boolean isDisableCloud()
	{
		return disableCloud.isSelected();
	}

	public void save()
	{
		save.click();
	}

	public CreateSearchFilterPage addFilter()
	{
		newFilterLink.click();
		return new CreateSearchFilterPage(getContext()).get();
	}

	public CreateSearchFilterPage editFilter(String filterName)
	{
		driver.findElement(
			By.xpath("//a[@title=" + quoteXPath(filterName)
				+ "]/../../td[@class='actions']//a[normalize-space(text())='Edit']")).click();
		return new CreateSearchFilterPage(getContext(), true).get();
	}

	public SearchSettingsPage removeFilter(String filterName)
	{
		WebElement findElement = driver.findElement(By.xpath("//a[@title=" + quoteXPath(filterName)
			+ "]/../../td[@class='actions']//a[text()='Remove']"));
		WaitingPageObject<SearchSettingsPage> removalWaiter = removalWaiter(findElement);
		findElement.click();
		acceptConfirmation();
		return ReceiptPage.waiter("Successfully saved settings", removalWaiter.get()).get();
	}

	public boolean hasFilter(String filterName)
	{
		return isPresent(By.xpath("//a[@title=" + quoteXPath(filterName) + "]"));
	}
}
