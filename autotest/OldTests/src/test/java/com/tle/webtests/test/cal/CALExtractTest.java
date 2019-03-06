package com.tle.webtests.test.cal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.test.files.Attachments;

public class CALExtractTest extends AbstractCALTest
{
	private static final String BOOK_NAME = "Book";
	private static final String BOOK_COLLECTION = "CAL Guide Books Collection - Restrictive";

	public CALExtractTest()
	{
		super("Deakin Extracts");
	}

	@Override
	protected void prepareBrowserSession()
	{
		logon("caladmin", "``````");
	}

	@Test
	public void testChapterNone()
	{
		/* Book with 2 portions named "none".
		 * Portion 1 - 11%
		 * Portion 2 - 4%
		 * They should be treated as seperate chapters, which means more than 10% but not both at same time.
		 */
		createBookInCollection(BOOK_NAME, BOOK_COLLECTION);
		createPortion("none", "None 1", BOOK_NAME, 1, 11, 2);
		createPortion("none", "None 2", BOOK_NAME, 12, 15, 1);
		CALSummaryPage summaryPage = searchAndView(BOOK_NAME);
		String none1 = context.getFullName("None 1");
		String none2 = context.getFullName("None 2");

		CALActivatePage<CALSummaryPage> activatePage = summaryPage.activate(none1, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summaryPage.isActive(none1, ATTACH1_FILENAME));

		activatePage = summaryPage.activate(none2, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summaryPage.isInactive(none2, ATTACH1_FILENAME));

		activatePage = summaryPage.activate(none1, ATTACH2_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summaryPage.isActive(none1, ATTACH2_FILENAME));
	}

	@SuppressWarnings("nls")
	@Test
	public void testActivateForRestrictiveApplied() throws Exception
	{
		createBookInCollection(BOOK_NAME, BOOK_COLLECTION);
		createPortion("1", "Portion 1", BOOK_NAME, 1, 5, 1);
		createPortion("1", "Portion 1-2", BOOK_NAME, 6, 7, 1, Attachments.get("page2.html"));
		createPortion("2", "Portion 2", BOOK_NAME, 8, 10, 1);
		CALSummaryPage summaryPage = searchAndView(BOOK_NAME);

		//apply restrictive validations to single portion

		CALActivatePage<CALSummaryPage> activatePage = summaryPage.activate(1, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

		activatePage = summaryPage.activate(1, "page2.html");
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summaryPage.isActive(1, "page2.html"));

		activatePage = summaryPage.activate(2, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summaryPage.isInactive(2, ATTACH1_FILENAME));

		summaryPage.viewSection(1, ATTACH1_FILENAME);
		assertTrue(new VerifyableAttachment(context).get().isVerified());
	}

	@Test
	public void testExtracts()
	{
		/* Book with 2 portions unnamed chapters (extracts)
		 * No more than 10% can be activated in only 1 of them.
		 */
		createBookInCollection(BOOK_NAME, BOOK_COLLECTION);
		createPortion("", "Extract 1", BOOK_NAME, 1, 11, 2);
		createPortion("", "Extract 2", BOOK_NAME, 12, 15, 1);
		CALSummaryPage summaryPage = searchAndView(BOOK_NAME);

		String extract1 = context.getFullName("Extract 1");
		String extract2 = context.getFullName("Extract 2");

		CALActivatePage<CALSummaryPage> activatePage = summaryPage.activate(extract1, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activate();
		assertTrue(summaryPage.isActive(extract1, ATTACH1_FILENAME));

		activatePage = summaryPage.activate(extract1, ATTACH2_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summaryPage.isInactive(extract1, ATTACH2_FILENAME));

		activatePage = summaryPage.activate(extract2, ATTACH1_FILENAME);
		activatePage.setCourse("A Simple Course");
		activatePage.setDates(getNowRange());
		activatePage.activateViolation();
		activatePage.okViolation();
		assertTrue(summaryPage.isInactive(extract2, ATTACH1_FILENAME));

	}
}
