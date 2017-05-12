package com.tle.webtests.remotetest.integration.moodle.tests;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.tle.webtests.framework.URLUtils;
import com.tle.webtests.pageobject.DynamicUrlPage;
import com.tle.webtests.pageobject.cal.ActivationsSummaryPage;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSelectionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractMoodleSectionTest;

public class MoodleCALTest extends AbstractMoodleSectionTest
{
	private static final String ATTACHMENT_NAME = "page.html";
	private static final String COURSE_NAME = "Test Course 1";
	private static final int WEEK = 3;
	private static final String CAL_ITEM = "Testable CAL Book - Chapter 1";
	private final Map<Integer, String> UUID_MAP = ImmutableMap.<Integer, String> builder()
		.put(25, "6b303fe3-9885-47b0-9e0c-0b679a200b20").put(26, "72643609-78e0-4a61-8d78-d5251ed82ff1")
		.put(27, "82d9a918-200a-4b67-91f0-404c41d23161").build();

	@Test
	public void activateThenDelete()
	{
		MoodleCoursePage coursePage = new MoodleLoginPage(context).load().logon("teacher", "``````")
			.clickCourse(COURSE_NAME);
		coursePage.setEditing(true);

		String itemUuid = UUID_MAP.get(getMoodleVersion());
		String itemName = CAL_ITEM + " moodle" + getMoodleVersion();
		MoodleSelectionPage moodleSelection = coursePage.selectEquellaResource(WEEK);
		SelectionSession selectionSession = moodleSelection.equellaSession();
		ItemListPage items = selectionSession.homeExactSearch(itemName);
		SummaryPage summary = items.getResultForTitle(itemName, 1).viewSummary();
		CALSummaryPage calSummaryPage = summary.cal();
		CALActivatePage<CALSummaryPage> activatePage = calSummaryPage.activate(1, ATTACHMENT_NAME);
		activatePage.setDates(getNowRange());
		activatePage.setCitation("Harvard");
		calSummaryPage = activatePage.activate();
		coursePage = summary.finishSelecting(coursePage);

		Assert.assertTrue(coursePage.hasResource(WEEK, ATTACHMENT_NAME));
		MoodleResourcePage resource = coursePage.clickResource(WEEK, ATTACHMENT_NAME);
		Assert.assertEquals(resource.getDescription(), ", 'Testable CAL Book - Chapter 1 moodle" + getMoodleVersion()
			+ "' in");
		Map<String, String[]> params = URLUtils.parseParamUrl(resource.getContentUrl(), context.getBaseUrl());
		Assert.assertEquals(params.get("$PATH$")[0], "integ/gen/" + itemUuid + "/1/");
		Assert.assertTrue(params.containsKey("cf.act"));
		logout();

		logon("teacher", "``````");
		ActivationsSummaryPage activations = DynamicUrlPage
			.load(context.getBaseUrl() + "integ/gen/" + itemUuid + "/1/", new SummaryPage(context)).cal()
			.activationsTab();
		Assert.assertEquals(activations.getStatus(0), "Active");

		coursePage = new MoodleLoginPage(context).load().logon("teacher", "``````").clickCourse(COURSE_NAME);
		coursePage.setEditing(true);
		coursePage.deleteAllForWeek(WEEK);

		activations = DynamicUrlPage
			.load(context.getBaseUrl() + "integ/gen/" + itemUuid + "/1/", new SummaryPage(context)).cal()
			.activationsTab();
		Assert.assertEquals(activations.getStatus(0), "Inactive");
	}

	@Override
	public int getWeek()
	{
		return WEEK;
	}

	@Override
	public String getCourseName()
	{
		return COURSE_NAME;
	}

	protected Date[] getNowRange()
	{
		return getNowRange(TimeZone.getTimeZone("America/Chicago"));
	}

	protected Date[] getNowRange(TimeZone zone)
	{
		return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(zone, false, false);
	}
}
