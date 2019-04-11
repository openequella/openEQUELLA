package com.tle.webtests.test.reporting;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.reporting.NoParamsReportWindow;
import com.tle.webtests.pageobject.reporting.ReportingPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("reporting")
public class RunReportTest extends AbstractSessionTest {
  private static final String COLLECTION_1 = "Collection 1";
  private static final String COLLECTION_2 = "Collection 2";
  private static final String COLLECTION_3 = "Collection 3";

  @Test
  // Scenario: there are two report groups, report group one has
  // EXECUTE_REPORT permission to Report 1.
  // report group two has EXECUTE_REPORT permission to Report 2.
  public void checkReports() {
    ReportingPage reporting;

    // userC is in neither of these two report groups.
    logon("userC", "equella");
    reporting = new ReportingPage(context).load();
    assertTrue(reporting.isNoReportsAvailable());
    logout();

    // userA is in report group one
    logon("userA", "equella");
    reporting = new ReportingPage(context).load();
    assertTrue(reporting.isReportExisting("Report 1"));
    assertFalse(reporting.isReportExisting("Report 2"));
    assertFalse(reporting.isReportExisting("ItemCount"));
    logout();

    // userB is in report group two
    logon("userB", "equella");
    reporting = new ReportingPage(context).load();
    assertTrue(reporting.isReportExisting("Report 2"));
    assertFalse(reporting.isReportExisting("Report 1"));
    assertFalse(reporting.isReportExisting("ItemCount"));
    logout();

    // user AutoTest is in both report groups.
    logon("AutoTest", "automated");
    reporting = new ReportingPage(context).load();
    assertTrue(reporting.isReportExisting("Report 1"));
    assertTrue(reporting.isReportExisting("Report 2"));
    assertTrue(reporting.isReportExisting("ItemCount"));
    assertTrue(reporting.isReportExisting("MultiValue"));
    // verify hidden report
    assertFalse(reporting.isReportExisting("HiddenReport"));
    logout();
  }

  @Test
  public void runMultiValueReport() {
    logon("AutoTest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    MultiValueReportOptionsPage report =
        reports.getReport("MultiValue", new MultiValueReportOptionsPage(context));
    report.selectValueFromLeft("value 1");
    report.selectValueFromLeft("value 2");

    MultiValueReportPage reportPage = report.execute();
    assertEquals("value 1", reportPage.getReportValue(2));
    assertEquals("value 2", reportPage.getReportValue(3));
    report.select();
    assertTrue(report.isEditParameterButtonEnabled());

    reportPage.close();
    logout();
  }

  @Test
  public void runItemCountReport() {
    logon("AutoTest", "automated");
    SearchPage search = new SearchPage(context).load();
    search.setWithinAll();
    String resultFromSearch = search.totalItemFound();
    ReportingPage reports = new ReportingPage(context).load();
    NoParamsReportWindow<ItemCountReportPage> report =
        reports.getReport("ItemCount", new ItemCountReportPage(context));
    assertFalse(report.isEditParameterButtonEnabled());
    String resultFromReport = report.getReport().getReportResult();
    assertEquals(resultFromSearch, resultFromReport);
    report.close();
    logout();
  }

  @Test
  public void runUsersAndItemsReport() {
    logon("AutoTest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    NoParamsReportWindow<UsersReportPage> report =
        reports.getReport("Users", new UsersReportPage(context));
    UsersReportPage usersReport = report.getReport();
    assertEquals(usersReport.getUserName(0), "AutoTest");
    assertEquals(usersReport.getUserName(1), "contributor");
    assertEquals(usersReport.getUserName(2), "userA");
    assertEquals(usersReport.getUserName(3), "userB");
    assertEquals(usersReport.getUserName(4), "userC");

    usersReport.openSubReport("a2a86033-83ec-44af-b4c6-38edb025b9b3");
    ItemsReportPage itemsReport = reports.getSubReport(new ItemsReportPage(context)).getReport();
    assertEquals(itemsReport.getItemName(), "Reporting Item 3");
    usersReport.close();
    logout();
  }

  @Test
  public void runFreetextReports() {
    logon("Autotest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    NoParamsReportWindow<FreeTextReportPage> report =
        reports.getReport("Freetext report", new FreeTextReportPage(context));
    FreeTextReportPage ftReport = report.getReport();
    // Basic Query
    assertTrue(ftReport.checkBasicReportResults());
    // Count
    assertTrue(ftReport.checkBasicCountResults());
    // List Files
    assertTrue(ftReport.checkListFilesResults());
    // Matrix Search
    assertTrue(ftReport.checkMatrixResults());
    // Matrix Count
    assertTrue(ftReport.checkMatrixCountResults());
    logout();
  }

  @Test
  public void runCascadingParametersTest() {
    logon("Autotest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    CascadingParametersReportParametersPage report =
        reports.getReport(
            "Cascading parameters report", new CascadingParametersReportParametersPage(context));
    report.selectItem("Item with a few versions");
    report.selectVersion(1);
    report.selectDescription("Description for version 1");
    CascadingParametersReportPage results = report.execute();
    assertEquals(results.getReportValue(), "Description for version 1");

    report.close();
    logout();
  }

  @Test
  public void runDisplayTextParametersTest() {
    String COLLECTION_1_id;
    String COLLECTION_2_id;
    String COLLECTION_3_id;

    logon("Autotest", "automated");
    ReportingPage reports = new ReportingPage(context).load();
    DisplayTextReportParametersPage report =
        reports.getReport(
            "DisplayText parameters report", new DisplayTextReportParametersPage(context));

    COLLECTION_1_id = report.getCollectionId(COLLECTION_1);
    COLLECTION_2_id = report.getCollectionId(COLLECTION_2);
    COLLECTION_3_id = report.getCollectionId(COLLECTION_3);

    report.setDisplayTextParam(COLLECTION_1);
    report.selectValueFromLeft(COLLECTION_2);
    report.selectValueFromLeft(COLLECTION_3);

    report.setRegularParamList(COLLECTION_1_id);
    report.setRegularParamNonList("Display text params are amazing");
    DisplayTextReportPage results = report.execute();

    assertEquals(results.getReportValue(0, false), COLLECTION_1_id);
    assertEquals(results.getReportValue(0, true), COLLECTION_1);

    // order of display names in report not fixed
    String reportDisplayCollections = results.getReportValue(1, true);
    if (("[" + COLLECTION_2 + ", " + COLLECTION_3 + "]").equals(reportDisplayCollections)) {
      assertEquals(
          results.getReportValue(1, false), "[" + COLLECTION_2_id + ", " + COLLECTION_3_id + "]");
    } else {
      assertEquals(reportDisplayCollections, "[" + COLLECTION_3 + ", " + COLLECTION_2 + "]");
      assertEquals(
          results.getReportValue(1, false), "[" + COLLECTION_3_id + ", " + COLLECTION_2_id + "]");
    }
    assertEquals(results.getReportValue(2, false), "");
    assertEquals(results.getReportValue(2, true), "");

    assertEquals(results.getReportValue(3, false), COLLECTION_1_id);
    assertEquals(results.getReportValue(3, true), COLLECTION_1_id);

    assertEquals(results.getReportValue(4, false), "Display text params are amazing");
    assertEquals(results.getReportValue(4, true), "Display text params are amazing");
  }
}
