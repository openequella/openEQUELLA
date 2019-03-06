package com.tle.webtests.remotetest.integration.moodle.settings;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.webtests.pageobject.DynamicUrlPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractSequentialMoodleTest;

@Test
public class MoodleTokenTest extends AbstractSequentialMoodleTest
{
	private static final String COURSE_NAME = "Test Course 1";
	private static final int WEEK = 6;
	private static final String SELECT_GUEST_ITEM = "GuestViewableItem";
	private static final String SELECT_ITEM = "BasicSelectionItem";

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void addItem()
	{
		new MoodleLoginPage(context).load().logon("admin", "admin");
		MoodleEquellaSettingsPage settings = resetSettings();
		settings.save();
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);

		course.setEditing(true);
		course.addItem(WEEK, SELECT_GUEST_ITEM, null);
		course.addItem(WEEK, SELECT_ITEM, null);
		assertTrue(course.hasResource(WEEK, SELECT_GUEST_ITEM));
		assertTrue(course.hasResource(WEEK, SELECT_ITEM));
		course.clickResource(WEEK, SELECT_ITEM);
		Assert.assertEquals(loggedInUser(), "admin");
	}

	@Test(dependsOnMethods = "addItem")
	public void prefixTest()
	{
		new MoodleLoginPage(context).load().logon("admin", "admin");
		MoodleEquellaSettingsPage settings = resetSettings();
		settings.setAllTokenFields("prefix", "prefix");
		settings.save();

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.clickResource(WEEK, SELECT_GUEST_ITEM);
		Assert.assertEquals(loggedInUser(), "prefixedadmin");
	}

	@Test(dependsOnMethods = "addItem")
	public void postfixTest()
	{
		new MoodleLoginPage(context).load().logon("admin", "admin");

		MoodleEquellaSettingsPage settings = resetSettings();
		settings.setAllTokenFields("postfix", "postfix");
		settings.save();

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.clickResource(WEEK, SELECT_GUEST_ITEM);
		Assert.assertEquals(loggedInUser(), "adminpostfixed");
	}

	@Test(dependsOnMethods = "addItem")
	public void guestTest()
	{
		logout();
		new MoodleLoginPage(context).load().logon("admin", "admin");

		MoodleEquellaSettingsPage settings = resetSettings();
		settings.setAllTokenFields("guest", "guest");
		settings.save();

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.clickResource(WEEK, SELECT_GUEST_ITEM);
		Assert.assertEquals(loggedInUser(), "guest");
	}

	@Test(dependsOnMethods = "addItem")
	public void userTest()
	{
		logout();
		new MoodleLoginPage(context).load().logon("admin", "admin");
		MoodleEquellaSettingsPage settings = resetSettings();
		settings.setAllTokenFields("tokenuser", "tokenuser");
		settings.save();

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		MoodleResourcePage resource = course.clickResource(WEEK, SELECT_GUEST_ITEM);
		assertFalse(resource.isTokenError());

		course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		resource = course.clickResource(WEEK, SELECT_ITEM);
		assertTrue(resource.isTokenError(), "User should be denied access");
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		MoodleCoursePage coursePage = new MoodleLoginPage(context).load().logon("admin", "admin")
			.clickCourse(COURSE_NAME);
		coursePage.setEditing(true);
		coursePage.deleteAllForWeek(WEEK);
	}

	private String loggedInUser()
	{
		SummaryPage summary = DynamicUrlPage.load(context.getBaseUrl()
			+ "items/a546e490-d9c1-4e6c-b037-3a9829c3e7e6/1/", new SummaryPage(context));
		return summary.loggedInUser();
	}
}
