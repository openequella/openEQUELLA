package com.tle.webtests.test.myresources;

import static org.testng.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14845 Test
 * Reference: http://time/DTEC/test/editTest.aspx?testId=14960
 * 
 * @author larry
 */
@TestInstitution("myresources")
public class MyResourcesControlsTest extends AbstractCleanupTest
{
	/**
	 * Usually but not always the displayed link text case-insensitively
	 * matching these.
	 */
	private static final String PUBLISHED = "published";
	/**
	 * With drafts, the display text is "Drafts" the item id is "draft", so if
	 * the display text to same case contains the static string, it'll match.
	 */
	private static final String DRAFT = "draft";
	private static final String SCRAPBOOK = "scrapbook";
	/**
	 * modqueue vs "Moderation queue"
	 */
	private static final String MODQUEUE = "modqueue";
	/**
	 * archived vs "Archive"
	 */
	private static final String ARCHIVED = "archived";
	/**
	 * all vs "All resources"
	 */
	private static final String ALL = "all";

	/**
	 * With All resources, the display text is "All resources" the item id is
	 * "all", so if the display text to same case contains the static string,
	 * it'll match.
	 */
	private static final String ALL_RESOURCES = "All resources";

	@Test
	public void testTabsAndControlsPublished()
	{
		logonToHome(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		WizardPageTab wizard = new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);
		wizard.editbox(1).setText(context.getFullName("Oanitem starting with O"));
		wizard.save().publish();

		MyResourcesPage resourcesPage = new MyResourcesPage(context, PUBLISHED).load();
		resourcesPage.examineStandardSortOptions();

		resourcesPage
			.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, PUBLISHED, "O*", 10, 2, 1, true);
		logout();
	}

	@Test
	public void testTabsAndControlsScrapbook()
	{
		MyResourcesPage resourcesPage = logonToMyResources(SCRAPBOOK);

		// We have a few flickr- entitled elements in scrapbook
		resourcesPage.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, SCRAPBOOK, "Flickr*", 4, 1, -1,
			false); // -1 because false: no collection filter

		logout();
	}

	@Test
	public void testTabsAndControlsDraft()
	{
		MyResourcesPage resourcesPage = logonToMyResources(DRAFT);

		// We have at least one "artful" as a draft in the Generic Testing
		// Collection.
		resourcesPage
			.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, DRAFT, "artful*", 1, 1, 1, true);

		logout();
	}

	@Test
	public void testTabsAndControlsModerationQueue()
	{
		MyResourcesPage resourcesPage = logonToMyResources(MODQUEUE);

		// ensure the moderation filter control is present
		resourcesPage.getFilterControl("msmodonly");

		WebElement sortElement = resourcesPage.getSortList("modsort");
		examineModerationSortOptions(sortElement);

		// We have nothing in the moderation queue.
		resourcesPage.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, MODQUEUE, "artful*", 0, 0, 0,
			true);
	}

	@Test
	public void testTabsAndControlsArchive()
	{
		MyResourcesPage resourcesPage = logonToMyResources(ARCHIVED);

		// We don't have anything archived.
		resourcesPage.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, ARCHIVED, "artful*", 0, 0, 0,
			true);

		logout();
	}

	@Test
	public void testTabsAndControlsAllResource()
	{
		MyResourcesPage resourcesPage = logonToMyResources(ALL);

		// We have at least one "a*" in the Generic Testing Collection.
		resourcesPage.examineControlFeatures(GENERIC_TESTING_COLLECTION, ALL_RESOURCES, ALL, "A*", 10, 1, 1, true);

		// Refresh the current page, and look for the distinction between
		// scrapbook items (Edit & Deletable) and contributed items.
		// verify that when the edit and delete buttons are there, that the
		// item can be found in the scrapbook.
		ItemListPage itemsPage = resourcesPage.results();
		int numUnfiltered = itemsPage.getTotalAvailable();
		List<String> presumedScrapbookTitles = resourcesPage.gatherAllScrapbookTitles(itemsPage, true);

		// Load up the scrapbook page
		resourcesPage.clickSelectedTab(SCRAPBOOK);
		itemsPage = resourcesPage.results();
		numUnfiltered = itemsPage.getTotalAvailable();

		// confirm that the items from scrapbook page add up to the presumed
		// scrapbook pages
		// from the all resources results ...

		List<String> confirmedScrapbookTitles = resourcesPage.gatherAllScrapbookTitles(itemsPage, false);

		// ... and that the content of the 2nd list contained within the first
		for( Iterator<String> iter = confirmedScrapbookTitles.iterator(); iter.hasNext(); )
		{
			String stringy = iter.next();
			assertTrue(presumedScrapbookTitles.contains(stringy), "Expected presumed Scrapbook list to contain "
				+ stringy);
			// Having confirmed a match, remove the matching title from both
			// lists, so
			// we can be sure that duplicate titles won't cast any doubt on the
			// result
			iter.remove();
			presumedScrapbookTitles.remove(stringy);
		}

		logout();
	}

	/**
	 * This test requires a filter for a mimetype which we assume we have some
	 * of (such as images) and another filter for a type we assume we have none
	 * of (such as archives).
	 */
	@Test
	public void testEngageCustomResourceFilters()
	{
		MyResourcesPage resourcesPage = logonToMyResources(ALL);

		int minimumExpectedAll = 10;
		ItemListPage itemsPage = resourcesPage.results();
		int numUnfiltered = itemsPage.getTotalAvailable();
		assertTrue(numUnfiltered >= minimumExpectedAll, "We expected this test user to have pre-populated at least "
			+ minimumExpectedAll + " items");

		verifyFilterImpact(resourcesPage, "image", numUnfiltered, true);

		verifyFilterImpact(resourcesPage, "archive", numUnfiltered, false);
	}

	private MyResourcesPage logonToMyResources(String tabName)
	{
		logonToHome(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		return new MyResourcesPage(context, tabName).load();
	}

	/**
	 * Helper method to test the use of resource filters
	 */
	private void verifyFilterImpact(MyResourcesPage resourcesPage, String targetFilterName, int numUnfiltered,
		boolean someResultsExpected)
	{

		resourcesPage.enterStringIntoFilter(targetFilterName);

		ItemListPage itemsPage = resourcesPage.results();
		int numFiltered = itemsPage.getTotalAvailable();

		// Whichever of the pre-configured filters we are using here, we expect
		// either fewer, or none.
		assertTrue(numFiltered < numUnfiltered);

		// If some expected, affirm this; otherwise affirm that there are none
		if( someResultsExpected )
		{
			assertTrue(numFiltered > 0, "Expected filter (" + targetFilterName + ") to return some results.");
		}
		else
			assertTrue(numFiltered == 0, "Did not expect filter (" + targetFilterName
				+ ") to return results, but found " + numFiltered);

		resourcesPage.clearFilters();
	}

	/**
	 * Most tabs bring up a standard set of sort options, the exception being
	 * the status-related variations for the Moderation queue.
	 */
	private void examineModerationSortOptions(WebElement sortElement)
	{
		EquellaSelect sortOptions = new EquellaSelect(context, sortElement);
		List<WebElement> selectableOptions = sortOptions.getSelectableHyperinks();
		// We expect these four. Allow the test to succeed if there are more.
		boolean hasSubmitted = false, hasLastAction = false, hasTitle = false, hasDate = false;
		for( WebElement anOption : selectableOptions )
		{
			String anOptionsText = anOption.getText();
			if( anOptionsText.toLowerCase().contains("Date".toLowerCase()) )
				hasDate = true;
			else if( anOptionsText.toLowerCase().contains("Title".toLowerCase()) )
				hasTitle = true;
			else if( anOptionsText.toLowerCase().contains("Last".toLowerCase()) )
				hasLastAction = true;
			else if( anOptionsText.toLowerCase().contains("Submitted".toLowerCase()) )
				hasSubmitted = true;
		}
		// Close the drop down
		sortOptions.clickOn();
		assertTrue(hasLastAction && hasDate && hasTitle & hasSubmitted, "Expected date(" + (hasDate ? "OK" : "Not OK")
			+ "), Last action(" + (hasLastAction ? "OK" : "Not OK") + "), Submitted("
			+ (hasSubmitted ? "OK" : "Not OK") + ") and title (" + (hasTitle ? "OK" : "Not OK") + ") sort options");
	}

}
