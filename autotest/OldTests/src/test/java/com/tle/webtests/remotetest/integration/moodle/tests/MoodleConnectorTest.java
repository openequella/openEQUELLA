package com.tle.webtests.remotetest.integration.moodle.tests;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.pageobject.NotPrefixedName;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.cal.CALActivatePage;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.connectors.EditMoodleConnectorPage;
import com.tle.webtests.pageobject.connectors.ShowConnectorsPage;
import com.tle.webtests.pageobject.connectors.ShowMoodleConnectorsPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleAdminSettings;
import com.tle.webtests.pageobject.integration.moodle.MoodleCoursePage;
import com.tle.webtests.pageobject.integration.moodle.MoodleIndexPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleLoginPage;
import com.tle.webtests.pageobject.integration.moodle.MoodleResourcePage;
import com.tle.webtests.pageobject.reporting.ConnectorReportOptionsPage;
import com.tle.webtests.pageobject.reporting.ConnectorReportPage;
import com.tle.webtests.pageobject.reporting.ReportingPage;
import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.EditExternalResourcePage;
import com.tle.webtests.pageobject.searching.ExternalMoveDialog;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.ManageExternalResourcePage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.FindUsesPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage.LMSCourseNode;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.remotetest.integration.ConnectorHelper;
import com.tle.webtests.remotetest.integration.moodle.AbstractParallelMoodleTest;
import java.util.List;
import java.util.TimeZone;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class MoodleConnectorTest extends AbstractParallelMoodleTest {
  // TODO dates seem to change for 2.7, test after upgrade
  private static final String COURSE_NAME = "Test Course 1";
  private static final String ARCHIVED_COURSE_NAME = "Archived Course";
  private static final String SECTION_NAME = "General";
  private static final String SECTION_DATE = "24 September - 30 September";
  private static final int WEEK = 0;

  private PrefixedName CONNECTOR_NAME;
  private PrefixedName CONNECTOR_NAME2;

  private String webToken;
  private ConnectorHelper connectorHelper;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    connectorHelper = new ConnectorHelper(context, getConnectorName());
  }

  private PrefixedName getConnectorName() {
    if (CONNECTOR_NAME == null) {
      CONNECTOR_NAME = new NotPrefixedName("Moodle" + context.getNamePrefix());
    }
    return CONNECTOR_NAME;
  }

  private PrefixedName getConnectorName2() {
    if (CONNECTOR_NAME2 == null) {
      CONNECTOR_NAME2 = new NotPrefixedName("Moodle2" + context.getNamePrefix());
    }
    return CONNECTOR_NAME2;
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void moodleTest() {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());

    String fullName = context.getFullName("LMS Push");
    String attachment = "page.html";

    SummaryPage item = createTestItem(fullName);

    LMSExportPage lms = item.lmsPage();
    connectorHelper.selectConnectorError(lms, getConnectorName2());
    assertFalse(lms.hasSummaryCheckbox());
    assertEquals(
        lms.getError(),
        "There are no resources available to push and selecting the resource summary has been"
            + " disabled");
    item = lms.summary();
    WizardPageTab wizard = item.edit();

    wizard.addFile(3, "page.html");
    wizard.waitForSelectedItem(attachment);
    item = wizard.saveNoConfirm();

    lms = item.lmsPage();
    connectorHelper.selectConnector(lms, getConnectorName2());
    assertFalse(lms.hasSummaryCheckbox());
    lms.showArchived(true);
    lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
    lms.selectAttachment(attachment, false);
    lms.publish();

    MoodleIndexPage indexPage = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = indexPage.clickCourse(COURSE_NAME);

    assertTrue(course.hasResource(WEEK, attachment));

    ShowConnectorsPage page = new ShowConnectorsPage(context).load();
    assertTrue(page.entityExists(getConnectorName2()));
    page.disableEntity(getConnectorName2());
    page.deleteEntity(getConnectorName2());
    assertFalse(page.entityExists(getConnectorName2()));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void moodleReport() {
    logon("AutoTest", "automated");

    String fullName = context.getFullName("report");
    SummaryPage item = createTestItem(fullName);
    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);
    connectorHelper.addToCourse(item, ARCHIVED_COURSE_NAME, SECTION_NAME);

    MoodleIndexPage indexPage = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = indexPage.clickCourse(COURSE_NAME);
    assertTrue(course.hasResource(WEEK, fullName));

    logon("AutoTest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    assertTrue(reports.isReportExisting("External Report"));

    ConnectorReportOptionsPage optionsPage =
        reports.getReport("External Report", new ConnectorReportOptionsPage(context));
    optionsPage.selectConnector(getConnectorName());
    optionsPage.showArchived(false);
    ConnectorReportPage reportPage = optionsPage.execute();

    assertTrue(reportPage.hasResource(fullName, COURSE_NAME));
    assertFalse(reportPage.hasResource(fullName, ARCHIVED_COURSE_NAME));
    reportPage.close();

    reports = new ReportingPage(context).load();
    optionsPage = reports.getReport("External Report", new ConnectorReportOptionsPage(context));
    optionsPage.selectConnector(getConnectorName());
    optionsPage.showArchived(true);
    reportPage = optionsPage.execute();

    assertTrue(reportPage.hasResource(fullName, COURSE_NAME));
    assertTrue(reportPage.hasResource(fullName, ARCHIVED_COURSE_NAME));
    reportPage.close();
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void findUsesTest() {
    String[] locations =
        new String[] {
          "1 September - 7 September",
          "8 September - 14 September",
          "15 September - 21 September",
          "22 September - 28 September",
          "29 September - 5 October",
          "6 October - 12 October",
          "13 October - 19 October",
          "20 October - 26 October",
          "27 October - 2 November",
          "3 November - 9 November"
        };
    String fullName = context.getFullName("Find uses");

    logon("AutoTest", "automated");
    AdminTabPage item = createTestItem(fullName).adminTab();
    FindUsesPage findUses = item.findUsesPage();

    connectorHelper.selectConnector(findUses);

    assertFalse(findUses.hasEntry(COURSE_NAME, SECTION_NAME));

    LMSExportPage lms = connectorHelper.addToCourse(findUses, COURSE_NAME, SECTION_NAME);
    findUses = lms.findUsesPage();
    connectorHelper.selectConnector(findUses);
    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
    assertFalse(findUses.hasFilterBox());

    // Test find uses table filtering
    lms = findUses.lmsPage();
    connectorHelper.selectConnector(lms);
    LMSCourseNode course = lms.clickCourse(COURSE_NAME);
    lms.selectSummary();

    for (String loc : locations) {
      course.clickSection(loc);
    }
    lms.publish();

    findUses = lms.findUsesPage();
    findUses = connectorHelper.selectConnector(findUses);
    assertTrue(findUses.hasFilterBox());

    for (String loc : locations) {
      assertTrue(findUses.hasEntry(COURSE_NAME, loc));
    }

    findUses = findUses.filterUses("September", COURSE_NAME, "General", false);
    for (String loc : locations) {
      assertEquals(findUses.hasEntry(COURSE_NAME, loc), loc.contains("September"));
    }

    findUses = findUses.filterUses("General", COURSE_NAME, "General", true);
    for (String loc : locations) {
      assertFalse(findUses.hasEntry(COURSE_NAME, loc));
    }

    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));

    findUses = findUses.filterUses("", COURSE_NAME, locations[0], true);

    // Test find uses table sorting
    assertEquals(findUses.getSort(), "Date added");
    assertTrue(findUses.isAscendingSort());
    findUses.sortBy("Date added", true);
    assertFalse(findUses.hasEntryAt(1, COURSE_NAME, SECTION_NAME));
    findUses.sortBy("Date added", false);
    assertTrue(findUses.hasEntryAt(1, COURSE_NAME, SECTION_NAME));

    findUses.sortBy("Location", true);
    assertTrue(findUses.hasEntryAt(1, COURSE_NAME, "1 September - 7 September"));
    assertTrue(findUses.hasEntryAt(2, COURSE_NAME, "13 October - 19 October"));
    findUses.sortBy("Location", false);
    assertTrue(findUses.hasEntryAt(1, COURSE_NAME, SECTION_NAME));
    assertTrue(findUses.hasEntryAt(2, COURSE_NAME, "8 September - 14 September"));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void courseFiltering() {
    String fullName = context.getFullName("Course filtering");

    logon("AutoTest", "automated");
    AdminTabPage item = createTestItem(fullName).adminTab();
    FindUsesPage findUses = item.findUsesPage();

    connectorHelper.selectConnector(findUses);
    assertFalse(findUses.hasEntry(COURSE_NAME, SECTION_NAME));

    LMSExportPage lms = findUses.lmsPage();
    connectorHelper.selectConnector(lms);
    lms.selectSummary();

    assertTrue(lms.hasCourse(COURSE_NAME));
    lms.filterCourses("Archive", COURSE_NAME, false);
    assertFalse(lms.hasCourse(COURSE_NAME));
    assertFalse(lms.hasCourse("Archived Course"));

    lms.showArchived(true);
    assertTrue(lms.hasCourse("Archived Course"));

    lms.filterCourses("Test", COURSE_NAME, true);
    assertTrue(lms.hasCourse(COURSE_NAME));
    lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
    lms.publish();

    findUses = lms.findUsesPage();
    findUses = connectorHelper.selectConnector(findUses);
    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void archivedTest() {
    String fullName = context.getFullName("archive");

    logon("AutoTest", "automated");
    createTestItem(fullName);

    MoodleIndexPage moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = moodle.clickCourse(ARCHIVED_COURSE_NAME);
    course.setEditing(true);
    course = course.addItem(WEEK, fullName);

    logon("AutoTest", "automated");
    FindUsesPage uses =
        SearchPage.searchExact(context, fullName).getResult(1).viewSummary().findUsesPage();

    connectorHelper.selectConnector(uses);

    assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));
    uses.showArchived(true);
    assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));

    assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_DATE));

    LMSExportPage lmsPage = uses.lmsPage();
    connectorHelper.selectConnector(lmsPage);
    lmsPage.selectSummary();

    lmsPage.showArchived(false);
    assertFalse(lmsPage.hasCourse(ARCHIVED_COURSE_NAME));
    lmsPage.showArchived(true);
    assertTrue(lmsPage.hasCourse(ARCHIVED_COURSE_NAME));

    lmsPage.clickCourse(ARCHIVED_COURSE_NAME).clickSection(SECTION_DATE);
    lmsPage.publish();

    uses = lmsPage.findUsesPage();
    connectorHelper.selectConnector(uses);

    uses.showArchived(false);
    assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_DATE));

    uses.showArchived(true);
    assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_DATE));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void versionTest() {
    String fullName = context.getFullName("version");
    String fullNameV2 = context.getFullName("v2 version");

    logon("AutoTest", "automated");
    SummaryPage item = createTestItem(fullName);

    LMSExportPage lms = connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    FindUsesPage findUses = lms.findUsesPage();
    connectorHelper.selectConnector(findUses);

    findUses.showAllVersions(true);
    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, "Resource version", "1"));

    WizardPageTab wizard = item.adminTab().newVersion();
    wizard.editbox(1, fullNameV2);
    item = wizard.save().publish();

    lms = connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    findUses = lms.findUsesPage();
    connectorHelper.selectConnector(findUses);

    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, "Resource version", "2"));
    assertFalse(findUses.hasDetail(COURSE_NAME, SECTION_NAME, "Resource version", "1"));
    findUses.showAllVersions(true);
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, 2, "Resource version", "1"));
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, 1, "Resource version", "2"));

    findUses =
        new ItemAdminPage(context)
            .load()
            .exactQuery(fullName)
            .getResult(1)
            .viewSummary()
            .findUsesPage();
    connectorHelper.selectConnector(findUses);

    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, 1, "Resource version", "1"));
    findUses.showAllVersions(true);
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, 2, "Resource version", "1"));
    assertTrue(findUses.hasDetail(COURSE_NAME, SECTION_NAME, 1, "Resource version", "2"));
  }

  @Test(dependsOnMethods = "findUsesTest")
  public void newVersionShowAllVersions() {
    logon("AutoTest", "automated");
    LMSExportPage lms =
        createTestItem(context.getFullName("New version version1")).adminTab().lmsPage();
    connectorHelper.selectConnector(lms);
    lms.selectSummary();
    lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
    lms = lms.publish();

    WizardPageTab wiz = lms.summary().adminTab().newVersion();
    wiz.editbox(1, context.getFullName("New version version2"));

    AdminTabPage admin = wiz.save().publish().adminTab();
    FindUsesPage findUses = admin.findUsesPage();
    connectorHelper.selectConnector(findUses);
    assertFalse(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
    findUses = findUses.showAllVersions(true);

    assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void manageExternalResourcesTest() {
    String fullName = context.getFullName("external");
    String nameEdit = fullName + " edited";
    String descriptionEdit = context.getFullName("description");

    logon("AutoTest", "automated");
    SummaryPage item = createTestItem(fullName);

    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);
    LMSExportPage lms = connectorHelper.addToCourse(item, ARCHIVED_COURSE_NAME, SECTION_NAME);

    FindUsesPage uses = connectorHelper.selectConnector(lms.findUsesPage());
    uses.showArchived(true);
    assertTrue(uses.hasEntry(COURSE_NAME, SECTION_NAME));
    assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));

    ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
    assertFalse(external.hasResults());
    connectorHelper.selectConnector(external);
    assertTrue(external.hasResults());

    external.search(fullName);
    assertEquals(external.results().getResults().size(), 2);
    external.showArchived(false);
    assertEquals(external.results().getResults().size(), 1);
    ItemSearchResult result = external.results().getResults().get(0);
    assertEquals(result.getDetailText("Course"), COURSE_NAME);
    assertEquals(result.getDetailText("Location"), SECTION_NAME);

    external.showArchived(true);
    assertEquals(external.results().getResults().size(), 2);
    external.selectCourse(ARCHIVED_COURSE_NAME);
    assertEquals(external.results().getResults().size(), 1);
    result = external.results().getResults().get(0);
    assertEquals(result.getDetailText("Course"), ARCHIVED_COURSE_NAME);
    assertEquals(result.getDetailText("Location"), SECTION_NAME);
    result.clickActionConfirmAndRemove("Remove", true, external);

    external.selectCourse("");
    assertEquals(external.results().getResults().size(), 1);
    result = external.results().getResults().get(0);
    EditExternalResourcePage editPage =
        result.clickAction("Edit", new EditExternalResourcePage(context, result));
    editPage.setName(nameEdit);
    editPage.setDescription(descriptionEdit);
    result = editPage.save();
    assertEquals(result.getTitle(), nameEdit);

    uses = SearchPage.searchAndView(context, fullName).findUsesPage();
    connectorHelper.selectConnector(uses);
    uses.showArchived(true);
    assertTrue(uses.hasEntry(COURSE_NAME, SECTION_NAME));
    assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));

    MoodleIndexPage moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = moodle.clickCourse(COURSE_NAME);
    assertTrue(course.hasResource(WEEK, nameEdit));
    MoodleResourcePage resource = course.clickResource(WEEK, nameEdit);
    assertEquals(resource.getDescription(), descriptionEdit);

    moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    course = moodle.clickCourse(ARCHIVED_COURSE_NAME);
    assertFalse(course.hasResource(WEEK, fullName));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void bulkMoveActionTest() {
    String moveName1 = context.getFullName("move");
    String moveName2 = context.getFullName("move 2");

    logon("AutoTest", "automated");
    SummaryPage item = createTestItem(moveName1);
    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    item = createTestItem(moveName2);
    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    MoodleIndexPage moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = moodle.clickCourse(COURSE_NAME);
    assertTrue(course.hasResource(WEEK, moveName1));
    assertTrue(course.hasResource(WEEK, moveName2));

    moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    course = moodle.clickCourse(ARCHIVED_COURSE_NAME);
    assertFalse(course.hasResource(WEEK, moveName1));
    assertFalse(course.hasResource(WEEK, moveName2));

    logon("AutoTest", "automated");
    ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
    connectorHelper.selectConnector(external);
    external.search(moveName1);
    List<ItemSearchResult> results = external.results().getResults();
    assertEquals(results.size(), 2);
    results.get(0).setChecked(true);
    results.get(1).setChecked(true);

    ExternalMoveDialog moveDialog = external.externalBulk().move();

    assertFalse(moveDialog.hasCourse(ARCHIVED_COURSE_NAME));
    moveDialog.showArchived(true);
    assertTrue(moveDialog.hasCourse(ARCHIVED_COURSE_NAME));

    moveDialog.clickCourse(ARCHIVED_COURSE_NAME).clickSection(SECTION_NAME);
    assertTrue(moveDialog.execute(external));

    moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    course = moodle.clickCourse(COURSE_NAME);
    assertFalse(course.hasResource(WEEK, moveName1));
    assertFalse(course.hasResource(WEEK, moveName2));

    moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    course = moodle.clickCourse(ARCHIVED_COURSE_NAME);
    assertTrue(course.hasResource(WEEK, moveName1));
    assertTrue(course.hasResource(WEEK, moveName2));
  }

  @Test(dependsOnMethods = "setupMoodle")
  public void bulkRemoveActionTest() {
    String deleteName1 = context.getFullName("delete");
    String deleteName2 = context.getFullName("delete 2");

    logon("AutoTest", "automated");
    SummaryPage item = createTestItem(deleteName1);
    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    item = createTestItem(deleteName2);
    connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

    MoodleIndexPage moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleCoursePage course = moodle.clickCourse(COURSE_NAME);
    assertTrue(course.hasResource(WEEK, deleteName1));
    assertTrue(course.hasResource(WEEK, deleteName2));

    logon("AutoTest", "automated");
    ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
    connectorHelper.selectConnector(external);
    external.search(deleteName1);
    List<ItemSearchResult> results = external.results().getResults();
    assertEquals(results.size(), 2);
    BulkSection bulk = external.bulk();
    bulk.selectAll();
    assertTrue(bulk.executeCommand("remove"));

    moodle = new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    course = moodle.clickCourse(COURSE_NAME);
    assertFalse(course.hasResource(WEEK, deleteName1));
    assertFalse(course.hasResource(WEEK, deleteName2));
  }

  @Test(dependsOnMethods = {"setupMoodle", "moodleTest"})
  public void testCourseFilteringOnActivation() {
    final String CAL_PORTION = "Testable CAL Book - Chapter 1 moodle" + getMoodleVersion();
    final String ATT_NAME = "page.html";
    logon("teacher", "``````");
    SummaryPage summary = SearchPage.searchAndView(context, CAL_PORTION);
    LMSExportPage lmsExportPage = summary.lmsPage();
    lmsExportPage.selectConnector(CONNECTOR_NAME);
    assertFalse(lmsExportPage.attachmentsSelectable());
    CALSummaryPage calSummary = SearchPage.searchAndView(context, CAL_PORTION).cal();
    // Moodle Course courseId == Test Course 1 courseId
    CALActivatePage<CALSummaryPage> activate = calSummary.activate(1, "page.html");
    activate.setCourse("Moodle Course");
    activate.setDates(getNowRange());
    activate.activate();
    lmsExportPage = summary.get().lmsPage();
    lmsExportPage.selectConnector(CONNECTOR_NAME);
    assertTrue(lmsExportPage.hasCourse("Backup Course"));
    assertTrue(lmsExportPage.hasCourse("Test Course 1"));
    lmsExportPage = lmsExportPage.selectAttachment(ATT_NAME, true);
    assertFalse(lmsExportPage.hasCourse("Backup Course"));
    assertTrue(lmsExportPage.hasCourse("Test Course 1"));
  }

  @Test
  public void setupMoodle() {
    if (Check.isEmpty(context.getIntegUrl())) {
      throw new SkipException("moodle url not set");
    }

    new MoodleLoginPage(context).load().logon("tokenuser", "``````");
    MoodleAdminSettings adminSettings = new MoodleAdminSettings(context);
    adminSettings.enableWebServiceAccessForUser("Token User");
    adminSettings.enableWebServices();
    adminSettings.enableRest();
    webToken = adminSettings.addToken("Token User");

    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    ShowConnectorsPage page = new ShowConnectorsPage(context).load();
    ShowMoodleConnectorsPage.addMoodleConnection(
        page, getConnectorName(), context.getIntegUrl(), webToken, "admin");
    assertTrue(page.entityExists(getConnectorName()));
    EditMoodleConnectorPage editPage =
        page.editConnector(new EditMoodleConnectorPage(page), getConnectorName());
    assertTrue(editPage.testConnection());
    page = editPage.save();
    ShowMoodleConnectorsPage.addMoodleConnection(
        page, getConnectorName2(), context.getIntegUrl(), webToken, "admin", false);
  }

  private SummaryPage createTestItem(String fullName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Moodle Items");
    wizard.editbox(1, fullName);
    return wizard.save().publish();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    if (!Check.isEmpty(testConfig.getIntegrationUrl("moodle"))) {
      logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
      ShowConnectorsPage page = new ShowConnectorsPage(context).load();
      page.deleteAllNamed(Lists.newArrayList(CONNECTOR_NAME, CONNECTOR_NAME2));

      MoodleCoursePage coursePage =
          new MoodleLoginPage(context).load().logon("admin", "admin").clickCourse(COURSE_NAME);
      coursePage.setEditing(true);
      coursePage.deleteAllForWeek(WEEK);

      for (int i = 1; i < 11; i++) {
        coursePage = coursePage.deleteResourceStartingWith(i, context.getNamePrefix());
      }

      coursePage =
          new MoodleLoginPage(context)
              .load()
              .logon("admin", "admin")
              .clickCourse(ARCHIVED_COURSE_NAME);
      coursePage.setEditing(true);
      coursePage.deleteAllForWeek(WEEK);

      for (int i = 1; i < 11; i++) {
        coursePage = coursePage.deleteResourceStartingWith(i, context.getNamePrefix());
      }
    }
  }

  private java.util.Calendar[] getNowRange() {
    return getNowRange(TimeZone.getTimeZone("America/Chicago"));
  }

  private java.util.Calendar[] getNowRange(TimeZone zone) {
    return com.tle.webtests.pageobject.generic.component.Calendar.getDateRange(zone, false, false);
  }
}
