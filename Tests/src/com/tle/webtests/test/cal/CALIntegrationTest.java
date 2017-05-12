package com.tle.webtests.test.cal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.DynamicUrlPage;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.IntegrationTesterReturnPage;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALAgreementPage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;

public class CALIntegrationTest extends AbstractCALTest
{
	private static final String BOOK_NAME = "Book";

	public CALIntegrationTest()
	{
		super("CAL from Integration");
	}

	protected IntegrationTesterPage integTester()
	{
		return new IntegrationTesterPage(context, "cal", "cal").load();
	}

	@Test
	public void contributeAndSelect()
	{
		context.setSubPrefix("Contribute");
		logon("caladmin", "``````");
		createBook(BOOK_NAME);
		ContributePage contributePage = DynamicUrlPage.load(
			integTester().getSignonUrl("contribute", "caladmin", "1234", "") + "&attachmentonly=true",
			new ContributePage(context));
		SummaryPage summaryTab = createPortion(contributePage, "1", "Portion", BOOK_NAME, 1, 10, 1);
		CALSummaryPage calSummaryPage = summaryTab.cal();
		calSummaryPage.activate(1, "page.html");
		CALActivatePage<SelectionCheckoutPage> activatePage = new CALActivatePage<SelectionCheckoutPage>(context,
			new SelectionCheckoutPage(context));
		SelectionCheckoutPage checkoutPage = activatePage.activate();
		IntegrationTesterReturnPage returnPage = checkoutPage.returnSelection(new IntegrationTesterReturnPage(context));
		CALAgreementPage agreementPage = DynamicUrlPage
			.load(returnPage.getReturnedUrl(), new CALAgreementPage(context));
		VerifyableAttachment accept = agreementPage.accept(new VerifyableAttachment(context));
		assertTrue(accept.isVerified());
	}

	@Test(dependsOnMethods = "contributeAndSelect")
	public void selectAttachment()
	{
		context.setSubPrefix("Contribute");
		logon("caladmin", "``````");
		String bookFullname = context.getFullName(BOOK_NAME);
		integTester().select("selectOrAdd", "caladmin", "1234", "");
		SelectionSession selectionSession = new SelectionSession(context).get();
		CALSummaryPage summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname)
			.cal();
		CALActivatePage<SelectionCheckoutPage> activatePage = summaryPage.activate(1, ATTACH1_FILENAME,
			new SelectionCheckoutPage(context));
		assertFalse(activatePage.containsCourseSelection());
		activatePage.setDates(getNowRange());
		verifyReturned(activatePage);
	}

	private void verifyReturned(CALActivatePage<SelectionCheckoutPage> activatePage)
	{
		SelectionCheckoutPage checkout = activatePage.activate();
		IntegrationTesterReturnPage returnPage = checkout.returnSelection(new IntegrationTesterReturnPage(context));
		CALAgreementPage agreementPage = DynamicUrlPage
			.load(returnPage.getReturnedUrl(), new CALAgreementPage(context));
		VerifyableAttachment attachment = agreementPage.accept(new VerifyableAttachment(context));
		assertTrue(attachment.isVerified());
	}

	@Test(dependsOnMethods = "contributeAndSelect")
	public void invalidCourse()
	{
		context.setSubPrefix("Contribute");
		logon("caladmin", "``````");
		integTester().select("selectOrAdd", "caladmin", "invalid1234", "");
		String bookFullname = context.getFullName(BOOK_NAME);
		SelectionSession selectionSession = new SelectionSession(context).get();
		CALSummaryPage summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname)
			.cal();
		assertTrue(summaryPage.activateToViolation(1, ATTACH1_FILENAME).isLoaded());
	}

	@Test(dependsOnMethods = "contributeAndSelect")
	public void courseAutoCreate()
	{
		context.setSubPrefix("Contribute");
		logonWithNotice("cal_AUTO_CREATE_COURSE", "``````");
		integTester().select("selectOrAdd", "cal_AUTO_CREATE_COURSE", "invalid1234", "");
		String bookFullname = context.getFullName(BOOK_NAME);
		SelectionSession selectionSession = new SelectionSession(context).get();
		CALSummaryPage summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname)
			.cal();
		assertTrue(summaryPage.activate(1, ATTACH1_FILENAME).isLoaded());
	}

	@Test(dependsOnMethods = "contributeAndSelect")
	public void useCourseDefaultDates()
	{
		context.setSubPrefix("Contribute");
		logon("caladmin", "``````");
		integTester().select("selectOrAdd", "caladmin", "4321", "");
		String bookFullname = context.getFullName(BOOK_NAME);
		SelectionSession selectionSession = new SelectionSession(context).get();
		CALSummaryPage summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname)
			.cal();
		CALActivatePage<SelectionCheckoutPage> activatePage = summaryPage.activate(1, ATTACH1_FILENAME,
			new SelectionCheckoutPage(context));
		assertEquals(activatePage.getFromDate().getTextValue(), "");
		assertEquals(activatePage.getUntilDate().getTextValue(), "");

		// WTF? Course 4321 has *no* default dates, if this ever succeeded it
		// was WRONG.

		// This test is only a part of 010983 where no course date and
		// no default course dates are set
		// so asserting to current as from and tomorrow as until
		// Calendar today =
		// Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		// SimpleDateFormat sdf =
		// com.tle.webtests.pageobject.generic.component.Calendar.createFormatter();
		// assertEquals(activatePage.getFromText(),
		// sdf.format(today.getTime()));
		// today.add(Calendar.DATE, 1);
		// assertEquals(activatePage.getUntilText(),
		// sdf.format(today.getTime()));
		// verifyReturned(activatePage);

		// Try something likely, like course "1234" which should go from 'today'
		// to April 8, 2040
		integTester().select("selectOrAdd", "caladmin", "1234", "");
		selectionSession = new SelectionSession(context).get();
		summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname).cal();
		activatePage = summaryPage.activate(1, ATTACH1_FILENAME, new SelectionCheckoutPage(context));
		// Caladmin's today is America/Chicago time
		Calendar today = Calendar.getInstance(TimeZone.getTimeZone("America/Chicago"));
		com.tle.webtests.pageobject.generic.component.Calendar fromDate = activatePage.getFromDate();
		SimpleDateFormat sdf = fromDate.createFormatter();
		sdf.setTimeZone(TimeZone.getTimeZone("America/Chicago"));
		assertEquals(fromDate.getTextValue(), sdf.format(today.getTime()));
		//Hmm, second param depends on your format...
		assertEquals(activatePage.getUntilDate().getTextValue(), "04/08/2040");
	}

	@Test(dependsOnMethods = "contributeAndSelect")
	public void testAddActivatedPortion()
	{
		context.setSubPrefix("Contribute");
		logon("caladmin", "``````");
		integTester().select("selectOrAdd", "caladmin", "integ101", "");

		SelectionSession selectionSession = new SelectionSession(context).get();
		String bookFullname = context.getFullName(BOOK_NAME);
		CALSummaryPage summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname)
			.cal();
		// check can't add
		assertFalse(summaryPage.canAdd(1, ATTACH1_FILENAME), "Course has no activations but can add");
		// create activation for course
		logon("caladmin", "``````");
		summaryPage = searchAndView(BOOK_NAME);
		summaryPage.activateDefault(1, ATTACH1_FILENAME, "Integration Course");

		integTester().select("selectOrAdd", "caladmin", "integ101", "");
		summaryPage = selectionSession.homeSearch('"' + bookFullname + '"').viewFromTitle(bookFullname).cal();
		assertTrue(summaryPage.canAdd(1, ATTACH1_FILENAME), "Course should have activation but cannot add");
		summaryPage.add(1, ATTACH1_FILENAME);
		IntegrationTesterReturnPage integReturn = selectionSession.finish().returnSelection(
			new IntegrationTesterReturnPage(context));
		assertTrue(integReturn.isSuccess());
	}
}
