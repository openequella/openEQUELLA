package com.tle.webtests.remotetest.integration.blackboard;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.framework.URLUtils;
import com.tle.webtests.pageobject.DynamicUrlPage;
import com.tle.webtests.pageobject.cal.ActivationsSummaryPage;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALAgreementPage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardContentPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardCoursePage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardEditItemPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardLoginPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardMyInstitutionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;

@Test(groups = "blackboardTest")
public class BlackboardCALTest extends AbstractBlackboardTest
{
	private static final String ATTACHMENT_NAME = "page.html";
	private static final String COURSE_NAME = "EQUELLA TEST COURSE";
	private static final String ITEM_NAME = "Testable CAL Book - Chapter 1";
	private static final String ITEM_UUID = "33641336-ef9e-4d8c-8a33-390086571199";
	private static final String CAL_FOLDER = "Cal";

	@Test
	public void activateThenDelete()
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();
		content = content.addFolder(CAL_FOLDER).enterFolder(CAL_FOLDER);
		SelectionSession selectionSession = content.addEquellaResource();

		ItemListPage items = selectionSession.getSearchPage().exactQuery(ITEM_NAME);
		SummaryPage summary = items.getResultForTitle(ITEM_NAME, 1).viewSummary();
		CALSummaryPage calSummaryPage = summary.cal();
		CALActivatePage<CALSummaryPage> activatePage = calSummaryPage.activate(1, ATTACHMENT_NAME);
		activatePage.setDates(getNowRange());
		activatePage.setCitation("Harvard");
		calSummaryPage = activatePage.activate();
		content = content.finishAddEquellaResource(selectionSession);

		Assert.assertTrue(content.hasAttachment(ATTACHMENT_NAME));
		BlackboardEditItemPage editResource = content.editResource(ITEM_NAME);

		Assert.assertEquals(editResource.getDescription(), ", 'Testable CAL Book - Chapter 1' in");
		// FIXME EQ-1567
		// Map<String, String[]> params =
		// URLUtils.parseParamUrl(editResource.getUrl(), null);
		// Assert.assertTrue(params.containsKey("cf.act"));

		content = editResource.submit();
		content.viewResource(ATTACHMENT_NAME, new CALAgreementPage(context));
		Map<String, String[]> params = URLUtils
			.parseParamUrl(context.getDriver().getCurrentUrl(), context.getBaseUrl());
		Assert.assertEquals(params.get("$PATH$")[0], "integ/gen/" + ITEM_UUID + "/1/");
		content.closePopup();
		logout();

		logon("AutoTest", "automated");
		ActivationsSummaryPage activations = DynamicUrlPage
			.load(context.getBaseUrl() + "integ/gen/" + ITEM_UUID + "/1/", new SummaryPage(context)).cal()
			.activationsTab();
		Assert.assertEquals(activations.getStatus(0), "Active");

		indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		content = indexPage.clickCourse(COURSE_NAME).content();
		content.deleteResource(CAL_FOLDER);

		activations = DynamicUrlPage
			.load(context.getBaseUrl() + "integ/gen/" + ITEM_UUID + "/1/", new SummaryPage(context)).cal()
			.activationsTab();

		Assert.assertEquals(activations.getStatus(0), "Active");
	}

	private java.util.Calendar[] getNowRange()
	{
		Calendar cal = Calendar.getInstance();
		cal.roll(Calendar.YEAR, false);
		Calendar endYear = (Calendar) cal.clone();
		endYear.roll(Calendar.YEAR, 2);
		return new java.util.Calendar[]{cal, endYear};
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardContentPage content = indexPage.clickCourse(COURSE_NAME).content();
		content.deleteResourceIfExists(CAL_FOLDER);
	}
}
