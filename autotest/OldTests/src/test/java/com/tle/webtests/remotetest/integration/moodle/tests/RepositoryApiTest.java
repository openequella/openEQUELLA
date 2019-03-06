package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEditResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleManageRepoPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;
import com.tle.webtests.remotetest.integration.moodle.annotation.Moodles;

@Moodles({"moodle20", "moodle21", "moodle22"})
public class RepositoryApiTest extends AbstractParallelMoodleTest
{
	private static final String COURSE_NAME = "Test Course 1";
	private static final int WEEK = 4;
	private static final String SELECT_ITEM = "BasicSelectionItem";
	private static final String SELECT_IMAGE_ITEM = "BasicSelectionImage";
	private static final String SELECT_IMAGE = "google.png";

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void imageTest()
	{
		new MoodleLoginPage(context).load().logon("admin", "admin");

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItem(WEEK, SELECT_ITEM);
		MoodleEditResourcePage resource = course.editResource(WEEK, SELECT_ITEM);
		resource.setDescription("An image");
		resource.insertImage(SELECT_IMAGE_ITEM, SELECT_IMAGE, "google");
		course = resource.submit();
		MoodleResourcePage view = course.clickResource(WEEK, SELECT_ITEM);
		assertEquals(view.getDescription(), "An image");
		assertTrue(view.getImageUrlFromDescription().contains(".php"));
		assertTrue(view.getImageUrlFromDescription().contains("google.png"));
	}

	@Test
	public void escapeTest()
	{
		final String fullName = "MoodleSelectionTest - selectHtmlTitle <script>alert('escape fail')</script>";
		final String attachment = "<script>alert('escape fail')</script>";
		new MoodleLoginPage(context).load().logon("admin", "admin");
		new MoodleManageRepoPage(context).load().enableEquella();

		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		String selectItem = "BasicSelectionPackage";
		course.addItem(WEEK, selectItem);

		MoodleEditResourcePage resource = course.editResource(WEEK, selectItem);
		resource.setDescription("An image");
		resource.insertImage(fullName, attachment, "escape test");
		course = resource.submit();
		MoodleResourcePage view = course.clickResource(WEEK, selectItem);
		assertEquals(view.getDescription(), "An image");
		assertTrue(view.getImageUrlFromDescription().contains("equella/redirect.php"));
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		MoodleCoursePage coursePage = new MoodleLoginPage(context).load().logon("tokenuser", "``````")
			.clickCourse("Test Course 1");
		coursePage.setEditing(true);
		coursePage.deleteAllForWeek(WEEK);
		super.cleanupAfterClass();
	}
}
