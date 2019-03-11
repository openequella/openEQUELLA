package com.tle.webtests.remotetest.integration.moodle.tests;

import com.tle.webtests.pageobject.integration.moodle.MoodleBreadcrumbs;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleCourseSettingsBlock;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSelectionPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MoodleBackupTest extends AbstractParallelMoodleTest {
  private static final String ITEM_NAME = "BasicSelectionItem";
  private static final String BACKUPCOURSE_NAME = "Backup Course";
  private static final int WEEK = 1;

  @Test
  public void backupAndRestore() {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("admin", "admin").clickCourse(BACKUPCOURSE_NAME);
    coursePage.setEditing(true);
    coursePage.deleteAllForWeek(WEEK);

    MoodleSelectionPage selectionPage = coursePage.selectEquellaResource(WEEK);
    SelectionSession session = selectionPage.equellaSession();
    SummaryPage summaryPage = session.homeExactSearch(ITEM_NAME).viewFromTitle(ITEM_NAME);
    summaryPage.attachments().selectAttachment("page.html");
    coursePage = summaryPage.finishSelecting(coursePage);

    MoodleCourseSettingsBlock settingsBlock = coursePage.settingsBlock();
    String filename = settingsBlock.backupCourse();
    coursePage = new MoodleBreadcrumbs(context).get().clickCourse(coursePage);
    coursePage.deleteAllForWeek(WEEK);
    coursePage = coursePage.settingsBlock().restoreCourse(filename, coursePage);
    Assert.assertTrue(coursePage.hasResource(WEEK, "page.html"));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    // nothing
  }
}
