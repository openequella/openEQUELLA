package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleTasksBlockPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;

public class TasksBlockTest extends AbstractParallelMoodleTest
{

	private static final String COURSE_NAME = "Test Course 1";

	@Override
	protected void prepareBrowserSession()
	{
		logon();
	}

	@Test
	public void tasksBlockTest()
	{
		new MoodleLoginPage(context).load().logon("admin", "admin");
		MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		if( course.hasTasksBlock() )
		{
			course.deleteTasksBlock();
		}

		logon("AutoTest", "automated");
		String fullName = context.getFullName("Moderate me");

		WizardPageTab wizard = new ContributePage(context).load().openWizard("Workflow Items");
		wizard.editbox(1, fullName);
		wizard.editbox(2, "A description");
		wizard.save().submit();

		new MoodleLoginPage(context).load().logon("admin", "admin");
		course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
		course.setEditing(true);
		course.addEquellaTasksBlock();
		MoodleTasksBlockPage tasksBlock = course.tasksBlock();
		assertTrue(tasksBlock.hasTasks());
		String taskUrl = tasksBlock.getTaskUrl("All Tasks");
		assertTrue(taskUrl.contains("asgn=ANY"));
		assertTrue(taskUrl.contains("token=admin"));

		course.deleteTasksBlock();
	}

}
