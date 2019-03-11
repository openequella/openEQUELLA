package com.tle.webtests.remotetest.integration.moodle.settings;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSelectionPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractSequentialMoodleTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test
public class MoodleOptionsTest extends AbstractSequentialMoodleTest {
  private static final String COURSE_NAME = "Test Course 1";
  private static final int WEEK = 3;
  private static final String SELECT_ITEM = "BasicSelectionImage";
  private static final String SELECT_IMAGE = "google.png";

  private static final String MOODLE_COLLECTION = "9a735fbf-226c-4d99-8f8f-6ec8368d9347";
  private static final String BASIC_COLLECTION = "b28f1ffe-2008-4f5e-d559-83c8acd79316";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void restrictCollection() {
    new MoodleLoginPage(context).load().logon("admin", "admin");
    setOptions("collectionIds=" + BASIC_COLLECTION);
    assertItem(false);

    setOptions("collectionIds=" + BASIC_COLLECTION + "," + MOODLE_COLLECTION);
    assertItem(true);
  }

  @Test
  public void multipleOptions() {
    new MoodleLoginPage(context).load().logon("admin", "admin");
    setOptions("mimeTypes=image/png&collectionIds=" + BASIC_COLLECTION);
    assertItem(false);

    setOptions("collectionIds=" + BASIC_COLLECTION + "&mimeTypes=image/png");
    assertItem(false);
  }

  @Test
  public void mimeTypeTest() {
    new MoodleLoginPage(context).load().logon("admin", "admin");
    setOptions("mimeTypes=image/png");
    assertItem(true);

    setOptions("mimeTypes=image/jpeg");
    assertItem(false);
  }

  private void setOptions(String options) {
    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setOptions(options);
    settings.save();
  }

  private void setRestriction(String option) {
    MoodleEquellaSettingsPage settings = resetSettings();
    settings.setRestriction(option);
    settings.save();
  }

  private void assertItem(boolean exists) {
    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);
    MoodleSelectionPage moodleSelectionPage = course.selectEquellaResource(WEEK);
    Assert.assertEquals(
        moodleSelectionPage.equellaSession().homeExactSearch(SELECT_ITEM).isResultsAvailable(),
        exists);
    moodleSelectionPage.cancel();
  }

  @Test
  public void restrictionTest() {
    // Test restricting to items only
    new MoodleLoginPage(context).load().logon("admin", "admin");
    setRestriction("itemonly");

    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);

    MoodleSelectionPage equellaResource = course.selectEquellaResource(WEEK);
    SummaryPage summary =
        equellaResource
            .equellaSession()
            .homeExactSearch(SELECT_ITEM)
            .getResultForTitle(SELECT_ITEM, 1)
            .viewSummary();
    AttachmentsPage attachments = summary.attachments();

    assertTrue(attachments.attachmentExists(SELECT_IMAGE));
    assertFalse(attachments.attachmentSelectExists(SELECT_IMAGE));
    assertTrue(summary.selectItemPresent());
    course = summary.selectMultipleItem(course);

    course.deleteResource(WEEK, SELECT_ITEM);

    // Test restricting to attachments only
    setRestriction("attachmentonly");

    course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);

    equellaResource = course.selectEquellaResource(WEEK);
    summary =
        equellaResource
            .equellaSession()
            .homeExactSearch(SELECT_ITEM)
            .getResultForTitle(SELECT_ITEM, 1)
            .viewSummary();
    attachments = summary.attachments();

    assertTrue(attachments.attachmentExists(SELECT_IMAGE));
    assertTrue(attachments.attachmentSelectExists(SELECT_IMAGE));

    assertFalse(summary.selectItemPresent());

    course = attachments.selectAttachmentMultiple(SELECT_IMAGE, course);
    course.deleteResource(WEEK, SELECT_IMAGE);

    // Test no restrictions
    setRestriction("none");

    course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);

    equellaResource = course.selectEquellaResource(WEEK);
    summary =
        equellaResource
            .equellaSession()
            .homeExactSearch(SELECT_ITEM)
            .getResultForTitle(SELECT_ITEM, 1)
            .viewSummary();
    attachments = summary.attachments();

    assertTrue(attachments.attachmentExists(SELECT_IMAGE));
    assertTrue(attachments.attachmentSelectExists(SELECT_IMAGE));
    assertTrue(summary.selectItemPresent());

    attachments.selectAttachment(SELECT_IMAGE);
    summary.selectItemNoCheckout();
    course = summary.finishSelecting(course);

    assertTrue(course.hasResource(WEEK, SELECT_IMAGE));
    assertTrue(course.hasResource(WEEK, SELECT_ITEM));
  }

  @AfterClass(alwaysRun = true)
  private void finished() {
    new MoodleLoginPage(context).load().logon("admin", "admin");
    resetSettings().save();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("admin", "admin").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    coursePage.deleteAllForWeek(WEEK);
  }
}
