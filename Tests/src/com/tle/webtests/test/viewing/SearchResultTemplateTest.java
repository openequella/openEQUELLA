package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;

@TestInstitution("fiveo")
public class SearchResultTemplateTest extends AbstractCleanupTest
{
	private static final String ITEM_NAME = "Result Templates";
	private static final String COLLECTION_NAME = "Search Results Template Collection";

	// DTEC 14976
	@Test
	public void testDisplayNodeTypes()
	{
		logon("AutoTest", "automated");
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION_NAME);
		wizard.editbox(1, context.getFullName(ITEM_NAME));
		wizard.editbox(2, "<b>BOLD</b>");
		ShuffleListControl list = wizard.shuffleList(3);
		list.add("value1");
		list.add("value2");
		wizard.calendar(5).setDate(new Date(0l));
		wizard.editbox(6, "http://www.google.com.au/");
		wizard.save().publish();

		SearchPage searchPage = new SearchPage(context).load();
		searchPage.exactQuery(context.getFullName(ITEM_NAME));
		ItemSearchResult result = searchPage.results().getResult(1);

		assertEquals(result.getDetailText("HTML"), "BOLD");
		assertEquals(result.getDetailText("Text"), "<b>BOLD</b>");
		assertEquals(result.getDetailLinkText("URL"), "http://www.google.com.au/");
		assertEquals(result.getDetailText("Split"), "value1\"value2");
		assertEquals(result.getDetailText("Date"), "January 1, 1970");
		String truncDetail = result.getDetailText("Truncated");
		assertTrue(truncDetail.equals("SearchResu..."));
	}

	@Test
	public void testDefaultOpen()
	{
		final String ITEM_OPEN = context.getFullName(ITEM_NAME) + " open";
		final String ITEM_CLOSED = context.getFullName(ITEM_NAME) + " closed";
		logon("AutoTest", "automated");
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION_NAME);
		wizard.editbox(1, ITEM_OPEN);
		wizard.addSingleFile(7, Attachments.get("avatar.png"));
		wizard.save().publish();

		wizard = new ContributePage(context).load().openWizard("No mandatory");
		wizard.editbox(1, ITEM_CLOSED);
		wizard.addSingleFile(3, Attachments.get("avatar.png"));
		wizard.save().publish();

		ItemListPage searchResults = new SearchPage(context).load().search(ITEM_NAME);
		assertTrue(searchResults.getResultForTitle(ITEM_OPEN).attachmentListOpen());
		assertFalse(searchResults.getResultForTitle(ITEM_CLOSED).attachmentListOpen());
	}
}
