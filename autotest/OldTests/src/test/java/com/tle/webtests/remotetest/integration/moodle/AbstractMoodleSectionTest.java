package com.tle.webtests.remotetest.integration.moodle;

import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;

public abstract class AbstractMoodleSectionTest extends AbstractParallelMoodleTest
{
	public abstract int getWeek();

	public abstract String getCourseName();

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		MoodleCoursePage coursePage = new MoodleLoginPage(context).load().logon("tokenuser", "``````")
			.clickCourse(getCourseName());
		coursePage.setEditing(true);
		coursePage.deleteAllForWeek(getWeek());
	}
}
