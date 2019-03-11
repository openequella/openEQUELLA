package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSearchBlockResultsPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;
import org.testng.annotations.Test;

public class SearchBlockTest extends AbstractParallelMoodleTest {

  private static final String COURSE_NAME = "Test Course 1";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void searchBlockTest() {
    String fullName = "BasicUTF8Selection ' хцч with other characters";
    String description = "<script>alert(\"hello\")</script>";

    new MoodleLoginPage(context).load().logon("admin", "admin");
    MoodleCoursePage course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);
    MoodleSearchBlockResultsPage search =
        course.addEquellaSearchBlock().search("BasicUTF8Selection");
    assertTrue(search.hasResults());
    assertEquals(search.titleForResult(1), fullName);
    assertEquals(search.descriptionForResult(1), description);
    assertTrue(search.urlForResult(1).contains("token=admin"));

    search.search("LargeImageViewer");
    assertTrue(search.hasResults());
    assertEquals(search.titleForResult(1), "LargeImageViewer");
    assertEquals(search.descriptionForResult(1), "");
    assertEquals(search.filenameForResult(1), "google.png");
    assertTrue(search.urlForResult(1).contains("token=admin"));

    course = new MoodleIndexPage(context).load().clickCourse(COURSE_NAME);
    course.setEditing(true);
    course.deleteSearchBlock();
  }
}
