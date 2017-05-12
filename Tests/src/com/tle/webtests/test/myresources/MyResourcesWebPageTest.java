package com.tle.webtests.test.myresources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URL;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.myresources.MyResourcesAuthorWebPage;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14446 Test
 * Reference: http://time/DTEC/test/editTest.aspx?testId=14449 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14450 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14959 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14966
 * 
 * @author larry
 */
@TestInstitution("myresources")
public class MyResourcesWebPageTest extends AbstractCleanupTest
{

	@Test
	public void addNewWebPage()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

		String scrapbookItem = context.getFullName("An authored page");
		int presumedPageNo = 1;

		ItemListPage results = authorWebPageAndVerify(scrapbookItem, presumedPageNo);

		results.getResultForTitle(scrapbookItem, presumedPageNo).clickLink("A Page");
		assertTrue(new VerifyableAttachment(context).get().isVerified());
	}

	/**
	 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14449
	 */
	@Test
	public void deleteOneFromMultipleWebPage()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

		int deleteIndex = 1;
		String itemName = context.getFullName("Hesitation is the trademark of deliberation");
		String[] pageTitles = {"This page title stays", "This page title gets deleted", "This page title also stays"};
		MyResourcesAuthorWebPage editWebPageWizard = openEditorToMultiWebPageItem(itemName, pageTitles);

		editWebPageWizard.deletePage(pageTitles[deleteIndex]);
		ItemSearchResult result = editWebPageWizard.save().results().getResultForTitle(itemName, 1);

		for( int i = 0; i < pageTitles.length; ++i )
		{
			assertEquals(i != deleteIndex, isPageInResult(result, pageTitles[i]));
		}
	}

	@Test
	public void addAndEditWebPageItem()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		String scrapbookItem = context.getFullName("Another web-page");
		String[] pageTitles = {"Chapter 1", "Chapter 2", "Chapter 3", "Chapter 4", "Chapter 5", "Chapter 6",
				"Chapter 7", "Chapter 8"};

		MyResourcesAuthorWebPage editor = openEditorToMultiWebPageItem(scrapbookItem, pageTitles);

		String abandonedDescStr = "And did those feet in ancient time walk upon England's mountains green?";
		editor.setDescription(abandonedDescStr);

		// Page referenced by index (offset) not ordinal, ie 0 is first
		editor.editPage("Chapter 4");
		editor.setTitle("An alternate Name");

		// abandon changes
		// Refresh the results list and the particular item reference, with the
		// old name
		MyResourcesPage myResourcesPage = editor.cancel();
		ItemListPage results = myResourcesPage.results();
		ItemSearchResult result = results.getResultForTitle(scrapbookItem, 1);
		// Verify the old page name hasn't changed
		assertTrue(isPageInResult(result, "Chapter 4"));

		// Now make an edit in earnest
		editor = myResourcesPage.editWebPage(result);

		String descStr = context.getFullName("Tyger, tyger burning bright");
		editor.setDescription(descStr);

		editor.editPage("Chapter 3");
		editor.setTitle("What the anvil, what the flame?");

		// confirm changes

		// Refresh the results list and the particular item reference, with the
		// new name
		result = editor.save().exactQuery(descStr).getResultForTitle(descStr, 1);

		// Verify the old page name has changed
		assertTrue(isPageInResult(result, "What the anvil, what the flame?"));
	}

	@Test
	public void addAndEditWebPageItemContent()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		String scrapbookItem = context.getFullName("Blake's Zoology");

		String[] pageTitles = {"Chapter 1", "Chapter 2", "Chapter 3"};
		String[] pageBodies = {"And did those feet in ancient times", "Walk upon England's mountains green?",
				"And was the holy lamb of god"};

		MyResourcesPage myResourcesPage = new MyResourcesPage(context, "scrapbook").load();
		ItemListPage results = myResourcesPage.authorWebPages(scrapbookItem, pageTitles, pageBodies).results();

		assertTrue(results.doesResultExist(scrapbookItem, 1));

		// Load for edit and change the HTML content of the page
		ItemSearchResult itemAdded = results.getResultForTitle(scrapbookItem);

		MyResourcesAuthorWebPage editor = myResourcesPage.editWebPage(itemAdded);

		String[] newHtmlContent = {"Tyger, tyger, burning bright In the forests of the night,",
				"What immortal hand or eye Could frame thy fearful symmetry?",
				"In what distant deeps or skies Burnt the fire of thine eyes?",
				"On what wings dare he aspire? What the hand dare seize the fire?",
				"And what shoulder and what art Could twist the sinews of thy heart?",
				"And when thy heart began to beat, What dread hand and what dread feet?",
				"What the hammer? what the chain? In what furnace was thy brain?",
				"What the anvil? What dread grasp Dare its deadly terrors clasp?",
				"When the stars threw down their spears, And water'd heaven with their tears,",
				"Did He smile His work to see? Did He who made the lamb make thee?",
				"Tyger, tyger, burning bright In the forests of the night,",
				"What immortal hand or eye Dare frame thy fearful symmetry?", "William Blake, 1794."};

		editor.editPage("Chapter 2");

		editor.setBodyHtml(String.join("<br>", newHtmlContent));

		// confirm changes

		// Refresh the results list and the particular item reference, with the
		// unchanged name
		itemAdded = editor.save().exactQuery(scrapbookItem).getResultForTitle(scrapbookItem);

		// use the editor to view saved html area content
		editor = myResourcesPage.editWebPage(itemAdded);
		editor.editPage("Chapter 2");
		String htmlAreaContent = editor.getBodyText();

		// abandon editor
		editor.cancel();

		// check that it's all there - not caring about newlines here.
		for( int i = 0; i < newHtmlContent.length; ++i )
		{
			assertTrue(htmlAreaContent.contains(newHtmlContent[i]), "Failed to find '" + newHtmlContent[i]
				+ "' in edit area content");
		}
	}

	@Test
	public void addAndDeleteAuthoredWebPageItem()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

		String scrapbookItem = context.getFullName("An authored page");
		int presumedIndex = 1;

		ItemListPage results = authorWebPageAndVerify(scrapbookItem, presumedIndex);
		results = results.getResultForTitle(scrapbookItem, presumedIndex).clickActionConfirmAndRemove("Delete", true,
			results);
		results = ReceiptPage.waiter("Successfully deleted from scrapbook", results).get();

		assertFalse(results.doesResultExist(scrapbookItem, presumedIndex));
	}

	@Test
	public void addAndAlternateOverWebPagesInItem()
	{
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		String scrapbookItem = context.getFullName("Blake's Zoology");

		String[] pageTitles = {"Chapter 1", "Chapter 2"};
		String[] pageBodies = {"And Gilgamesh said to Enkidu: 'Let us journey to the land of the scorpion men.'",
				"And Enkidu said unto Gilgamesh 'One of us shall lead and the other follow: let our strength of arm decide!'"};

		MyResourcesPage myResourcesPage = new MyResourcesPage(context, "scrapbook").load();
		ItemListPage results = myResourcesPage.authorWebPages(scrapbookItem, pageTitles, pageBodies).results();

		assertTrue(results.doesResultExist(scrapbookItem));

		String[] miscAddsForFirstPage = {"One man went to mow he", "went to mow a meadow", "two men went to mow they",
				"both went to mow the meadow"};
		String[] miscAddsForSecondPage = {"We three kings of orient are", "travelling round in an old bomb car",
				"CRASH! BANG!", "We two kings of orient are"};

		// Load for edit and change the HTML content of the page
		ItemSearchResult itemAdded = results.getResultForTitle(scrapbookItem);

		MyResourcesAuthorWebPage editor = myResourcesPage.editWebPage(itemAdded);
		assertTrue(miscAddsForFirstPage.length == miscAddsForSecondPage.length);

		// swap between the two pages, adding a bit more distinct content each
		// time
		for( int i = 0; i < miscAddsForFirstPage.length; ++i )
		{
			editor.editPage("Chapter 1");
			editor.appendBodyHtml(miscAddsForFirstPage[i]);
			editor.editPage("Chapter 2");
			editor.appendBodyHtml(miscAddsForSecondPage[i]);
		}

		// reload each page and verify they have the correct content
		results = editor.save().results();
		itemAdded = results.getResultForTitle(scrapbookItem);

		editor = myResourcesPage.editWebPage(itemAdded);

		editor.editPage("Chapter 1");
		String htmlAreaContentOfFirst = editor.getBodyText();
		editor.editPage("Chapter 2");
		String htmlAreaContentOfSecond = editor.getBodyText();

		editor.cancel();

		for( int i = 0; i < miscAddsForFirstPage.length; ++i )
		{
			if( i % 2 == 0 )
			{
				assertTrue(htmlAreaContentOfFirst.contains(miscAddsForFirstPage[i]));
				assertFalse(htmlAreaContentOfFirst.contains(miscAddsForSecondPage[i]));
			}
			else
			{
				assertTrue(htmlAreaContentOfSecond.contains(miscAddsForSecondPage[i]));
				assertFalse(htmlAreaContentOfSecond.contains(miscAddsForFirstPage[i]));
			}
		}
	}

	@Test
	public void uploadAttachmentToWebPageTest()
	{
		final String WEBPAGE_NAME = namePrefix + " Attachment Web Page";
		logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
		MyResourcesPage myResources = new MyResourcesPage(context, "scrapbook").load();
		MyResourcesAuthorWebPage authorPage = myResources.authorWebPage();
		authorPage.setDescription(WEBPAGE_NAME);
		authorPage.addPage("attachment", "");
		URL file = Attachments.get("courses.csv");
		authorPage.equellaFileUploader(file, "erryday i'm uploadin'");
		ItemListPage results = authorPage.save().results();
		assertTrue(results.doesResultExist(WEBPAGE_NAME));
	}

	/**
	 * convenience method to create and open editor to a multi-page item
	 */
	public MyResourcesAuthorWebPage openEditorToMultiWebPageItem(String itemName, String[] pageTitles)
	{
		String[] pageBodies = {"And did those feet in ancient times", "Walk upon England's mountains green?",
				"And was the holy lamb of god", "On England's pastures seen?",
				"And did the countenance divine shine forth upon these clouded hills?"};

		MyResourcesPage myResourcesPage = new MyResourcesPage(context, "scrapbook").load().authorWebPages(itemName,
			pageTitles, pageBodies);

		ItemListPage results = myResourcesPage.exactQuery(itemName);
		return myResourcesPage.editWebPage(results.getResultForTitle(itemName));
	}

	/**
	 * convenience local method to concoct a webpage into scrapbook
	 * 
	 * @param scrapbookItem
	 * @param presumedIndex the ordinal index of the item we add
	 * @return results page
	 */
	private ItemListPage authorWebPageAndVerify(String scrapbookItem, int presumedIndex)
	{
		ItemListPage results = new MyResourcesPage(context, "scrapbook").load()
			.authorWebPage(scrapbookItem, "A Page", "This is a verifiable attachment").results();

		assertTrue(results.doesResultExist(scrapbookItem, presumedIndex));

		return results;
	}

	private boolean isPageInResult(ItemSearchResult searchResult, String pageName)
	{
		return searchResult.isDetailLinkPresent("Web Pages", pageName);
	}
}
