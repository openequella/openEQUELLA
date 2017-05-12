package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tle.common.Check;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEditResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.viewitem.DRMAgreementPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;

public class MoodleTest extends AbstractParallelMoodleTest
{
	private static final String COURSE_NAME = "Test Course 1";
	private static final int WEEK = 2;

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void itemWithApostrophe()
	{
		String fullName = "BasicUTF8Selection ' хцч with other characters";
		new MoodleLoginPage(context).load().logon("teacher", "``````");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItem(WEEK, fullName);
		assertTrue(course.hasResource(WEEK, fullName));
		course.deleteResource(WEEK, fullName);
	}

	@Test
	public void tokenOnAttachments()
	{

		String fullName = "BasicSelectionImage";
		String attachment = "google.png";

		new MoodleLoginPage(context).load().logon("teacher", "``````");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItem(WEEK, fullName, attachment);

		assertTrue(course.hasResource(WEEK, attachment));
		String contentUrl = course.clickResource(WEEK, attachment).getContentUrl();
		assertTrue(contentUrl.contains("token=teacher"));
		course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.deleteResource(WEEK, attachment);
	}

	@Test
	public void externalLinkViewer()
	{
		String fullName = "ExternalLink";
		String attachment = "http://dev.equella.com/";

		new MoodleLoginPage(context).load().logon("teacher", "``````");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItem(WEEK, fullName, attachment);
		assertTrue(course.hasResource(WEEK, attachment));

		MoodleResourcePage resourcePage = course.clickResource(WEEK, attachment);
		assertFalse(resourcePage.isUsingExternalUrlViewer());
		course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);

		MoodleEditResourcePage resource = course.editResource(WEEK, attachment);
		resource.setUrl(resource.getUrl() + "&.vi=externalLinkViewer");
		resource.setName(context.getFullName(fullName));
		course = resource.submit();

		course.hasResource(WEEK, context.getFullName(fullName));
		resourcePage = course.clickResource(WEEK, context.getFullName(fullName));

		assertTrue(resourcePage.isUsingExternalUrlViewer());

		course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.deleteResource(WEEK, context.getFullName(fullName));
	}

	@Test
	public void externalLinkPointsToItem()
	{
		String fullName = "ExternalLink";
		String attachment = "http://dev.equella.com/";

		new MoodleLoginPage(context).load().logon("teacher", "``````");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItem(WEEK, fullName, attachment);
		assertTrue(course.hasResource(WEEK, attachment));
		MoodleEditResourcePage resource = course.editResource(WEEK, attachment);
		resource.setName(context.getFullName(fullName));

		assertFalse(resource.getUrl().contains("google"));
		resource.submit();

	}

	@Test
	public void itemWithDRM()
	{
		// DTEC 14601
		String terms = "Agree to these terms if you ever want to see your item again";
		String fullName = "Basic DRM Item";
		new MoodleLoginPage(context).load().logon("teacher", "``````");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addItemFromSearchResult(WEEK, fullName);
		assertTrue(course.hasResource(WEEK, fullName));

		MoodleResourcePage resource = course.clickResource(WEEK, fullName);
		DRMAgreementPage drmPage = resource.switchToItem(new DRMAgreementPage(context));
		assertTrue(drmPage.hasTerms(terms));
		course = drmPage.reject(course);

		assertTrue(course.hasResource(WEEK, fullName));
	}

	@BeforeMethod
	public void checkMoodleUrl() throws Exception
	{
		if( Check.isEmpty(context.getIntegUrl()) )
		{
			throw new SkipException("Properties for moodle are not set up");
		}
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		MoodleCoursePage coursePage = new MoodleLoginPage(context).load().logon("admin", "admin")
			.clickCourse(COURSE_NAME);
		coursePage.setEditing(true);
		coursePage.deleteAllForWeek(WEEK);
	}

}
