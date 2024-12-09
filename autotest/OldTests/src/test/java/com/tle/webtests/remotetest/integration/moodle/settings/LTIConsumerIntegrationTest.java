package com.tle.webtests.remotetest.integration.moodle.settings;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.DynamicUrlPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractSequentialMoodleTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class LTIConsumerIntegrationTest extends AbstractSequentialMoodleTest {
  /** TODO Doesn't yet cover: LTI Other roles, Adding new users to groups */
  private static final String COURSE_NAME = "Test Course 1";

  private static final int WEEK = 5;
  private static final String SELECT_GUEST_ITEM = "GuestViewableItem";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void addItem() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");
    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("token", "token");
    settings.save();
    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);

    course.setEditing(true);
    course.addItem(WEEK, SELECT_GUEST_ITEM, null);
    assertTrue(course.hasResource(WEEK, SELECT_GUEST_ITEM));
    course.clickResource(WEEK, SELECT_GUEST_ITEM);
    Assert.assertEquals(loggedInUser(), "admin");
  }

  @Test(dependsOnMethods = "addItem")
  public void prefixTest() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");
    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("prefix", "prefix");
    settings.save();

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.clickResource(WEEK, SELECT_GUEST_ITEM);
    Assert.assertEquals(loggedInUser(), "prefixedadmin");
  }

  @Test(dependsOnMethods = "addItem")
  public void postfixTest() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");

    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("postfix", "postfix");
    settings.save();

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.clickResource(WEEK, SELECT_GUEST_ITEM);
    Assert.assertEquals(loggedInUser(), "adminpostfixed");
  }

  @Test(dependsOnMethods = "addItem")
  public void guestTest() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");

    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("guest", "guest");
    settings.save();

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.clickResource(WEEK, SELECT_GUEST_ITEM);
    Assert.assertEquals(loggedInUser(), "guest");
  }

  @Test(dependsOnMethods = "addItem")
  public void testExpression() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");

    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("expression", "expression");
    settings.save();

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    assertTrue(course.clickResource(WEEK, SELECT_GUEST_ITEM).isLtiAuthError());
  }

  @Test(dependsOnMethods = "addItem")
  public void testInstructorRole() {
    logout();
    new MoodleLoginPage(context).load().logon("admin", "admin");

    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setLtiDetails("inst_role", "inst_role");
    settings.save();

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    assertTrue(course.clickResource(WEEK, SELECT_GUEST_ITEM).isLtiAuthError());
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("admin", "admin").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    coursePage.deleteAllForWeek(WEEK);
  }

  private String loggedInUser() {
    // FIXME sometimes it LTI auth from the moodle item viewing is slower
    // than this page load, so guest is returned
    SummaryPage summary =
        DynamicUrlPage.load(
                context.getBaseUrl() + "items/a546e490-d9c1-4e6c-b037-3a9829c3e7e6/1/",
                new SummaryPage(context))
            .get();
    return summary.loggedInUser();
  }
}
