package com.tle.webtests.test.cal;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.cal.CALAgreementPage;
import com.tle.webtests.pageobject.cal.CALInactiveViolation;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;

public class CALAgreementTest extends AbstractCALTest
{
	private static final String PORTION_NAME = "Portion 1";
	private static final String NORMAL_BOOK = "view uploaded agreement";
	private static final String XSL_BOOK = "view uploaded xsl agreement";
	private static final String DEFAULT_COURSE = "A Simple Course";

	public CALAgreementTest()
	{
		super("CAL Agreement Test");
	}

	/**
	 * This is being ignored because it doesn't actually test a non-uploaded
	 * agreement.
	 */
	@Test
	public void testNoAgreement()
	{
		logon("cal_COPYRIGHT_ITEM", "``````");
		createBookInCollection("view agreement", "CAL Guide Books Collection - Restrictive");
		String portionName = PORTION_NAME;
		createPortion("1", portionName, "view agreement", 1, 10, 1);
		CALSummaryPage summaryPage = searchAndView("view agreement");
		summaryPage.activateDefault(1, ATTACH1_FILENAME, DEFAULT_COURSE);
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));
		summaryPage.viewSection(1, ATTACH1_FILENAME);
		assertTrue(new VerifyableAttachment(context).get().isVerified());
	}

	@Test
	public void testViewNonActive()
	{
		logon("caladmin", "``````");
		context.getDriver().get(context.getBaseUrl() + "items/2af490d1-bd43-2240-c717-17356cde5e1e/1/Issues_List.doc");

		assertTrue(new CALInactiveViolation(context).get().isInactive());
	}

	@Test
	public void testUploadedAgreementAcceptAndReject()
	{
		logon("cal_COPYRIGHT_ITEM", "``````");
		createBook(NORMAL_BOOK);
		createPortion("1", PORTION_NAME, NORMAL_BOOK, 1, 10, 2);
		CALSummaryPage summaryPage = searchAndView(NORMAL_BOOK);
		summaryPage.activateDefault(1, ATTACH1_FILENAME, DEFAULT_COURSE);
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

		summaryPage.viewSection(1, ATTACH1_FILENAME);
		CALAgreementPage agreementPage = new CALAgreementPage(context).get();
		agreementPage.reject();
		summaryPage.get().viewSection(1, ATTACH1_FILENAME);
		agreementPage.get();
		assertTrue(agreementPage.getAgreementText().contains("This is a verifiable CAL Agreement"));
		VerifyableAttachment attachment = agreementPage.accept(new VerifyableAttachment(context));
		assertTrue(attachment.isVerified());
		summaryPage = searchAndView(NORMAL_BOOK);
		summaryPage.viewSection(1, ATTACH1_FILENAME);
		assertTrue(attachment.get().isVerified());
	}

	@Test
	public void testUploadedXSLAgreementAcceptAndReject()
	{
		logon("cal_COPYRIGHT_ITEM", "``````");
		createBookInCollection(XSL_BOOK, "CAL Guide Books Collection - xsl agreement");
		createPortion("1", PORTION_NAME, XSL_BOOK, 1, 10, 2);
		CALSummaryPage summaryPage = searchAndView(XSL_BOOK);
		summaryPage.activateDefault(1, ATTACH1_FILENAME, DEFAULT_COURSE);
		assertTrue(summaryPage.isActive(1, ATTACH1_FILENAME));

		summaryPage.viewSection(1, ATTACH1_FILENAME);
		CALAgreementPage agreementPage = new CALAgreementPage(context).get();
		agreementPage.reject();
		summaryPage.get().viewSection(1, ATTACH1_FILENAME);
		agreementPage.get();
		assertTrue(agreementPage.getAgreementText().contains("CAL Agreement Title (XSLT)"));
		VerifyableAttachment attachment = agreementPage.accept(new VerifyableAttachment(context));
		assertTrue(attachment.get().isVerified());
		summaryPage = searchAndView(XSL_BOOK);
		summaryPage.viewSection(1, ATTACH1_FILENAME);
		assertTrue(attachment.get().isVerified());
	}
}
