package com.tle.webtests.pageobject.myresources;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.AbstractQuerySection;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.AbstractSearchPage;
import com.tle.webtests.pageobject.searching.FilterByKeywordPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.viewitem.ModerationQueueShowCommentDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

public class MyResourcesPage extends AbstractQueryableSearchPage<MyResourcesPage, ItemListPage, ItemSearchResult>
{
	private static final TimeZone USERS_TIMEZONE = TimeZone.getTimeZone("America/Chicago");

	public static final String ADD_SEARCH_TO_FAV_CONTROL_ID = "mrs_fd_opener";

	@FindBy(id = "cmca_b")
	private WebElement uploadFiles;

	@FindBy(id = "cmca2_b")
	private WebElement authorWebPage;

	// Moderation Queue Table
	@FindBy(id = "mqil_mt")
	private WebElement modqueuetable;

	@FindBy(id = "searchresults-select")
	private WebElement mainElem;

	private String urlTypeArg;

	public MyResourcesPage(PageContext context, String urlTypeArg)
	{
		super(context);
		this.urlTypeArg = urlTypeArg;
	}

	@Override
	public ItemListPage search(PrefixedName query)
	{
		openFilters();
		querySection = createQuerySection();
		return super.search(query);
	}

	@Override
	public ItemListPage search(String query)
	{
		openFilters();
		querySection = createQuerySection();
		return super.search(query);
	}

	@Override
	public ItemListPage exactQuery(String query, int minExpected)
	{
		openFilters();
		return super.exactQuery(query, minExpected);
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return mainElem;
	}

	@Override
	protected AbstractQuerySection<?> createQuerySection()
	{
		return new FilterByKeywordPage(context);
	}

	@Override
	protected void loadUrl()
	{
		String myResourcesURL = "access/myresources.do?";
		if( getUrlTypeArg() != null && getUrlTypeArg().length() > 0 )
		{
			myResourcesURL += ("type=" + getUrlTypeArg());
		}
		driver.get(context.getBaseUrl() + myResourcesURL);
	}

	protected String getUrlTypeArg()
	{
		return urlTypeArg;
	}

	public MyResourcesPage uploadFile(String path, String description, String tags)
	{
		uploadFiles.click();
		new MyResourcesUploadFilesPage(this).get().uploadFile(path, description, tags);
		return get();
	}

	public MyResourcesUploadFilesPage getUploadPage()
	{
		uploadFiles.click();
		return new MyResourcesUploadFilesPage(this);
	}

	public MyResourcesPage uploadFile(File file, String description, String tags)
	{
		return uploadFile(file.getAbsolutePath(), description, tags);
	}

	public MyResourcesPage uploadFile(URL file, String description, String tags)
	{
		return uploadFile(getPathFromUrl(file), description, tags);
	}

	public MyResourcesPage authorWebPage(String description, String pageTitle, String pageBody)
	{
		authorWebPage.click();
		MyResourcesAuthorWebPage page = new MyResourcesAuthorWebPage(this).get();
		page.setDescription(description);
		page.addPage(pageTitle, pageBody);
		return page.save();
	}

	public MyResourcesPage authorWebPages(String description, String[] pageTitles, String[] pageBodies)
	{
		authorWebPage.click();
		MyResourcesAuthorWebPage authorPages = new MyResourcesAuthorWebPage(this).get();
		authorPages.setDescription(description);
		int numTitles = pageTitles.length;
		int numBodies = pageBodies.length;
		// allow for a discrepancy in the array sizes
		for( int i = 0; i < numTitles || i < numBodies; ++i )
		{
			int aTitleIndex = i;
			if( numTitles <= i )
				aTitleIndex = numTitles - 1;
			int aBodyIndex = i;
			if( numBodies <= i )
				aBodyIndex = numBodies - 1;
			authorPages.addPage(pageTitles[aTitleIndex], pageBodies[aBodyIndex]);
		}
		return authorPages.save();
	}

	public MyResourcesAuthorWebPage authorWebPage()
	{
		authorWebPage.click();
		return new MyResourcesAuthorWebPage(this).get();
	}

	/**
	 * Bring up a tab identified by its displayed link.
	 * 
	 * @param linkText
	 * @return
	 */
	public MyResourcesPage clickSelectedTab(String linkText)
	{
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		WebElement tabLinksDiv = context.getDriver().findElement(By.xpath("//div[@id='searchresults-select']"));
		List<WebElement> otherTabLinks = tabLinksDiv.findElements(By.xpath(".//a"));

		for( WebElement otherTabLink : otherTabLinks )
		{
			String tabLinkText = otherTabLink.getText();
			if( tabLinkText.toLowerCase().contains(linkText.toLowerCase()) )
			{
				otherTabLink.click();
				break;
			}
		}
		return waitForResultsReload(waiter);
	}

	public List<String> gatherAllScrapbookTitles(ItemListPage itemsPage, boolean queryForEditDelete)
	{
		List<String> presumedScrapbookTitles = new ArrayList<String>();
		WebElement pagesDiv = null;
		int pageCounter = 0, pages = 1; // We'll assume by default
		// How many pages in this result set?
		try
		{
			pagesDiv = context.getDriver().findElement(By.id("page"));
			List<WebElement> linksToOtherPages = pagesDiv.findElements(By.xpath(".//li/a"));
			pages = linksToOtherPages.size();
		}
		catch( NoSuchElementException noseeum )
		{
		}

		int numUnfiltered = itemsPage.getResults().size();

		do
		{
			// Ordinal sequence in getByForResult, ie 1-based not 0-based
			for( int sequencer = 1; sequencer <= numUnfiltered; ++sequencer )
			{
				ItemSearchResult itemAdded = itemsPage.getResult(sequencer);
				if( !queryForEditDelete || itemAdded.isEditDeletableItem() )
				{
					presumedScrapbookTitles.add(itemAdded.getTitle());
				}
			}

			// Turn the pages till we get the end of the set
			if( pageCounter < pages - 1 )
			{
				try
				{
					WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
					pagesDiv = context.getDriver().findElement(By.id("page"));
					pagesDiv.findElement(By.xpath(".//li/a[text()='next']")).click();
					itemsPage = waitForResultsReload(waiter).results();
					numUnfiltered = itemsPage.getResults().size();
					pagesDiv = context.getDriver().findElement(By.id("page"));
				}
				catch( NoSuchElementException nosuchthingasnext )
				{
				}
			}
		}
		while( ++pageCounter < pages );

		return presumedScrapbookTitles;
	}

	public MyResourcesPage enterStringIntoFilter(String targetFilterName)
	{
		// Open filter tab
		openFilters();

		// confirm the presence of the resource filter
		WebElement resourceDiv = context.getDriver().findElement(By.xpath("//ul[@id='mt']"));
		List<WebElement> filterCheckboxes = resourceDiv.findElements(By.xpath(".//li/input"));
		// Allow for the possibility that there are others than the two we
		// require,
		// and don't make this test dependent on exact order
		for( Iterator<WebElement> iter = filterCheckboxes.iterator(); iter.hasNext(); )
		{
			WebElement filterCheckbox = iter.next();
			String filterName = filterCheckbox.findElement(By.xpath("../label")).getText();
			if( filterName.toLowerCase().contains(targetFilterName) )
			{
				WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
				filterCheckbox.click();
				waitForResultsReload(waiter);
			}
		}
		return this;
	}

	/**
	 * Most tabs bring up a standard set of sort options, the exception being
	 * the status-related variations for the Moderation queue.
	 */
	public void examineStandardSortOptions()
	{
		openSort();

		WebElement sortElement = getSortList();
		EquellaSelect sortOptions = new EquellaSelect(context, sortElement);
		List<WebElement> selectableOptions = sortOptions.getSelectableHyperinks();
		// We expect these three. Allow the test to succeed if there are more.
		// Noting that "Rating' (found in the sort-options for main search)
		// should not be here
		boolean hasRelevance = false, hasTitle = false, hasDate = false;
		for( WebElement anOption : selectableOptions )
		{
			String anOptionsText = anOption.getText();
			if( anOptionsText.toLowerCase().contains("Date".toLowerCase()) )
				hasDate = true;
			else if( anOptionsText.toLowerCase().contains("Title".toLowerCase()) )
				hasTitle = true;
			else if( anOptionsText.toLowerCase().contains("Relevance".toLowerCase()) )
				hasRelevance = true;
		}
		sortOptions.clickOn();
		assertTrue(hasRelevance && hasDate && hasTitle, "Expected date(" + (hasDate ? "OK" : "Not OK")
			+ "), relevance(" + (hasRelevance ? "OK" : "Not OK") + ") and title (" + (hasTitle ? "OK" : "Not OK")
			+ ") sort options");
	}

	/**
	 * Helper method to do all the hack work of rummaging through filters etc
	 */
	public void examineControlFeatures(String resourceCollection, String resourceFilter, String tabPageId,
		String filterWildcard, int minimumExpectedAll, int minimumExpectedFiltered, int minimumExpectedCollection,
		boolean includesCollectionFilter)
	{
		// Prove that results are retrievable.
		ItemListPage itemsPage = results();
		int numUnfiltered = itemsPage.getTotalAvailable();
		assertTrue(numUnfiltered >= minimumExpectedAll, "We expected this test user to have pre-populated at least "
			+ minimumExpectedAll + ' ' + tabPageId + " items");

		// default sub-element is the search string input text box
		WebElement filterSearchControl = this.getFilterControl();
		WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
		filterSearchControl.sendKeys(filterWildcard);
		WebElement filterSearchRunControl = this.getFilterControl(AbstractSearchPage.FILTER_RUN_SEARCH_CONTROL_ID);
		filterSearchRunControl.click();
		itemsPage = waitForResultsReload(waiter).results();
		assertTrue(itemsPage.getTotalAvailable() >= minimumExpectedFiltered,
			"We expected this test user to have pre-populated at least " + minimumExpectedFiltered + ' ' + tabPageId
				+ " item matching '" + filterWildcard + "'");

		// clear the text box and do a search on resource collection. We assume
		// presence of 'Generic Testing Collection'
		// with at least one item where the search expects one.
		filterSearchControl.clear();
		filterSearchControl.sendKeys(Keys.ESCAPE);
		if( includesCollectionFilter )
		{
			itemsPage = selectViaFilterFromResourceCollection(resourceCollection).results();
			assertTrue(itemsPage.getTotalAvailable() >= minimumExpectedCollection,
				"We expected this test user to have pre-populated at least " + minimumExpectedCollection + ' '
					+ tabPageId + " item in the " + resourceCollection);
			selectViaFilterFromResourceCollection(resourceFilter);
		}

		// clear filters (if there are any)
		clearFilters();
	}

	public MyResourcesPage clearFilters()
	{
		resetFilters();
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		dfm.setTimeZone(USERS_TIMEZONE);
		try
		{
			dfm.parse("2012-01-01");
		}
		catch( ParseException e )
		{
			throw new Error(e);
		}
		Calendar d1 = dfm.getCalendar();
		setDateFilter("BEFORE", new Calendar[]{d1, null});
		return this;
	}

	public MyResourcesPage resetFilters()
	{
		try
		{
			WaitingPageObject<ItemListPage> waiter = resultsPageObject.getUpdateWaiter();
			WebElement resetButton = context.getDriver().findElement(By.id("rf_resetButton"));
			resetButton.click();
			return waitForResultsReload(waiter);
		}
		catch( NoSuchElementException noseeum )
		{
		}
		return this;
	}

	public boolean filterByMimeTypeIsPresent()
	{
		return this.isPresent(By.xpath(".//div[@class='input checkbox filterbymimetype']"));
	}

	// Define XPath for a Row
	private String xpathForRow(int i)
	{
		return ".//tbody/tr[contains(@class, 'odd') or contains(@class, 'even')][" + i + "]";
	}

	// Verify Show Comment link of an Item
	public boolean isShowCommentLinkPresentAtIndex(int i)
	{
		return modqueuetable.findElement(By.xpath(xpathForRow(i) + "/td[2]")).findElement(By.linkText("Show comment"))
			.isDisplayed();
	}

	// Access Moderation Queue Show Comment Dialog
	public String accessShowCommentViewAtIndex(int i)
	{
		modqueuetable.findElement(By.xpath(xpathForRow(i) + "/td[2]")).findElement(By.linkText("Show comment")).click();
		ModerationQueueShowCommentDialog commentDialog = new ModerationQueueShowCommentDialog(context).get();
		String comment = commentDialog.getComment();
		WaitingPageObject<MyResourcesPage> removalWaiter = removalWaiter(commentDialog.getLoadedElement());
		commentDialog.closeComment();
		removalWaiter.get();
		return comment;
	}

	// View Item Summary in Moderation Queue
	public SummaryPage accessItemSummeryInModQueueItemAtIndex(int i)
	{
		modqueuetable.findElement(By.xpath(xpathForRow(i) + "/td[1]")).findElement(By.tagName("a")).click();
		return new SummaryPage(context).get();
	}

	@Override
	public ItemListPage resultsPageObject()
	{
		return new ItemListPage(context);
	}

	public MyResourcesAuthorWebPage editWebPage(ItemSearchResult result)
	{
		return edit(result, new MyResourcesAuthorWebPage(this));
	}

	public MyResourcesUploadFilesPage editFile(ItemSearchResult result)
	{
		return edit(result, new MyResourcesUploadFilesPage(this));
	}

}
