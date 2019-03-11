package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.common.Check;
import com.tle.webtests.framework.URLUtils;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleEditResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleSelectionPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.moodle.AbstractMoodleSectionTest;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MoodleSelectionTest extends AbstractMoodleSectionTest {
  private static final String MANAGER_ITEM = "Manager only item";
  private static final String TEACHER_ITEM = "Teacher only item";
  private static final String COURSE_NAME = "Test Course 1";
  private static final int WEEK = 1;
  private static final String SELECT_ITEM = "BasicSelectionItem";
  private static final String SELECT_PACKAGE = "BasicSelectionPackage";
  private static final String PACKAGE_NAME = "Zou ba! Visiting China: Is this your first visit?";

  @Test
  public void selectPackageTest() {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    coursePage.addPackage(WEEK, SELECT_PACKAGE);
    assertFalse(coursePage.hasResource(WEEK, SELECT_PACKAGE));
    assertTrue(coursePage.hasResource(WEEK, PACKAGE_NAME));
  }

  @Test
  public void selectItemTest() {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    MoodleSelectionPage moodleSelection = coursePage.selectEquellaResource(WEEK);
    SelectionSession selectionSession = moodleSelection.equellaSession();
    ItemListPage items = selectionSession.homeExactSearch(SELECT_ITEM);
    SummaryPage summary = items.getResultForTitle(SELECT_ITEM, 1).viewSummary();
    AttachmentsPage attachments = summary.attachments();
    attachments.selectAttachment("http://dev.equella.com/");
    attachments.selectAttachment("page.html");
    coursePage = summary.finishSelecting(coursePage);

    Assert.assertTrue(coursePage.hasResource(WEEK, "http://dev.equella.com/"));
    Assert.assertTrue(coursePage.hasResource(WEEK, "page.html"));

    MoodleResourcePage resource = coursePage.clickResource(WEEK, "page.html");
    Map<String, String[]> params =
        URLUtils.parseParamUrl(resource.getContentUrl(), context.getBaseUrl());
    Assert.assertEquals(
        params.get("$PATH$")[0], "integ/gen/b9c151cb-9c70-4c0c-b020-da8d8333cf59/1/");
    Assert.assertTrue(params.containsKey("token"));

    coursePage = resource.backToCoursePage();
    resource = coursePage.clickResource(WEEK, "http://dev.equella.com/");
    params = URLUtils.parseParamUrl(resource.getContentUrl(), context.getBaseUrl());
    Assert.assertEquals(
        params.get("$PATH$")[0], "integ/gen/b9c151cb-9c70-4c0c-b020-da8d8333cf59/1/");
    Assert.assertTrue(params.containsKey("token"));
    Assert.assertEquals(params.get("attachment.uuid")[0], "be740469-3475-40c9-86b0-ca798874fd99");
  }

  @Test
  public void searchAsTeacherTest() {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("teacher", "``````").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    SelectionSession session = coursePage.selectEquellaResource(WEEK).equellaSession();
    ItemListPage results = session.homeExactSearch(MANAGER_ITEM);
    Assert.assertFalse(results.doesResultExist(MANAGER_ITEM));
    SearchPage search = session.getSearchPage();
    results = search.exactQuery(TEACHER_ITEM);
    Assert.assertTrue(results.doesResultExist(TEACHER_ITEM));
  }

  @Test
  public void searchAsManagerTest() {
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("manager", "``````").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);
    SelectionSession session = coursePage.selectEquellaResource(WEEK).equellaSession();
    ItemListPage results = session.homeExactSearch(TEACHER_ITEM);
    Assert.assertFalse(results.doesResultExist(TEACHER_ITEM));
    SearchPage search = session.getSearchPage();
    results = search.exactQuery(MANAGER_ITEM);
    Assert.assertTrue(results.doesResultExist(MANAGER_ITEM));
  }

  @Test
  public void selectHtmlTitle() {
    final String alert = "<script>alert('escape fail')</script>";
    final String fullName =
        "MoodleSelectionTest - selectHtmlTitle <script>alert('escape fail')</script>";
    final String description = "MoodleSelectionTest - selectHtmlTitle bold";
    MoodleCoursePage coursePage =
        new MoodleLoginPage(context).load().logon("tokenuser", "``````").clickCourse(COURSE_NAME);
    coursePage.setEditing(true);

    MoodleSelectionPage moodleSelection = coursePage.selectEquellaResource(WEEK);
    SelectionSession session = moodleSelection.equellaSession();
    SummaryPage summary = session.homeExactSearch(fullName).viewFromTitle(fullName);
    summary.selectItemNoCheckout();
    summary.attachments().selectAttachmentMultiple(alert, coursePage);
    assertTrue(
        coursePage.hasResource(WEEK, fullName), "The title probably hasnt been html encoded");
    assertTrue(coursePage.hasResource(WEEK, alert));

    MoodleResourcePage resourcePage = coursePage.clickResource(WEEK, fullName);

    assertEquals(resourcePage.getDescription(), description);
    assertTrue(
        resourcePage.switchToItem(new SummaryPage(context)).attachments().attachmentExists(alert));
  }

  @DataProvider(name = "versionSelection")
  public Object[][] versionSelection() {
    final String FORCE_LATEST = "Archived Course";
    final String DEFAULT_CURRENT = "Test Course 1";
    final String FORCE_CURRENT = "Restore Course";

    return new Object[][] {
      {DEFAULT_CURRENT, "ItemVersion1", false, 1, "Fixed to v1"},
      {DEFAULT_CURRENT, "ItemVersion2", false, 2, "Fixed to v2"},
      {FORCE_LATEST, "ItemVersion1", true, 2, ""},
      {FORCE_LATEST, "ItemVersion2", true, 2, ""},
      {FORCE_CURRENT, "ItemVersion1", false, 1, ""},
      {FORCE_CURRENT, "ItemVersion2", false, 2, ""}
    };
  }

  @Test(dataProvider = "versionSelection")
  public void versionSelectionTest(
      String course, String item, boolean latest, int expectedVersion, String selected) {
    new MoodleLoginPage(context).load().logon("tokenuser", "``````");

    MoodleCoursePage coursePage = new MoodleIndexPage(context).load().clickCourse(course);
    coursePage.setEditing(true);
    MoodleSelectionPage moodleSelection = coursePage.selectEquellaResource(WEEK);
    SelectionSession session = moodleSelection.equellaSession();

    SummaryPage summary = session.homeExactSearch(item).viewFromTitle(item);
    summary.selectItemNoCheckout();

    SelectionCheckoutPage selections = session.finish();
    if (!Check.isEmpty(selected)) {
      assertTrue(selections.hasVersionSelection());
      assertEquals(selections.versionSelected(), selected);
    } else {
      assertFalse(selections.hasVersionSelection());
    }

    coursePage = selections.returnSelection(coursePage);

    MoodleEditResourcePage editResource = coursePage.editResource(WEEK, item);
    String url = editResource.getUrl();
    assertTrue(url.endsWith((latest ? 0 : expectedVersion) + "/"));
    MoodleResourcePage view = editResource.submitAndView();
    summary = view.switchToItem(new SummaryPage(context));
    assertEquals(summary.getItemId().getVersion(), expectedVersion);
    coursePage = view.returnToMoodle().backToCoursePage();
    coursePage = coursePage.deleteResource(WEEK, item);
  }

  @Override
  public int getWeek() {
    return WEEK;
  }

  @Override
  public String getCourseName() {
    return COURSE_NAME;
  }
}
