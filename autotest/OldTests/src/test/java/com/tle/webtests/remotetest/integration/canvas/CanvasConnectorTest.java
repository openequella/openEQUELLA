package com.tle.webtests.remotetest.integration.canvas;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import com.tle.webtests.framework.Name;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.connectors.EditCanvasConnectorPage;
import com.tle.webtests.pageobject.connectors.ShowCanvasConnectorsPage;
import com.tle.webtests.pageobject.connectors.ShowConnectorsPage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasModulePage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasSettingsPage;
import com.tle.webtests.pageobject.integration.canvas.course.settings.CanvasAppsTab;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.remotetest.integration.ConnectorHelper;
import org.testng.annotations.Test;

@Test(groups = "tests")
public class CanvasConnectorTest extends AbstractCanvasTest {
  @Name("Canvas Connector")
  private static PrefixedName CONNECTOR;

  private static final String ACCESS_TOKEN =
      "OjJlNipUH4ZeSvJn3M0dRTehJ9Fmq9QvwT9fYz8b6yGCsqS4vmnLxys0i93xeu9J";
  private static final String COURSE_NO_TOOL = "Autotest course - no tool";
  private static String ITEM_NAME;

  private ConnectorHelper connectorHelper;

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    ITEM_NAME = context.getFullName("whatevs");
    connectorHelper = new ConnectorHelper(context, CONNECTOR);
  }

  @Test(groups = "setupModuleAndConnector")
  public void setupCanvas() {
    logon("TLE_ADMINISTRATOR", "tle010");
    ShowConnectorsPage page = new ShowConnectorsPage(context).load();
    ShowCanvasConnectorsPage.createConnector(page, CONNECTOR, ACCESS_TOKEN);
    EditCanvasConnectorPage editPage =
        page.editConnector(new EditCanvasConnectorPage(page), CONNECTOR);
    editPage.setAccessToken("gargage");
    assertFalse(editPage.testAccessToken());
    editPage.setAccessToken(ACCESS_TOKEN);
    assertTrue(editPage.testAccessToken());
    editPage.cancel();
    // create test item
    connectorHelper.createTestItem(ITEM_NAME).adminTab().lmsPage().get();
  }

  @Test(dependsOnMethods = {"setupCanvas"})
  public void pushWithError() {
    logon("autotest", "automated");

    LMSExportPage lms = SearchPage.searchAndView(context, ITEM_NAME).lmsPage().get();
    lms.selectSummary();
    lms.showArchived(true);
    lms.clickCourse(COURSE_NO_TOOL).clickSection(MODULE_NAME);
    ErrorPage noTool = lms.publishWithError();
    String errorDetail = noTool.getDetail();
    assertTrue(errorDetail.contains("There is no matching EQUELLA External App configured"));
  }

  @Test(dependsOnGroups = {"setupModuleAndConnector"})
  public void testAddToExternalSystem() {
    logon("autotest", "automated");
    SummaryPage item = SearchPage.searchAndView(context, ITEM_NAME);
    connectorHelper.addToCourse(item, COURSE_NAME, MODULE_NAME);

    // check item exists
    CanvasModulePage modules = getToCourseModules(COURSE_NAME);
    assertTrue(modules.moduleItemExists(MODULE_NAME, ITEM_NAME));
    modules.logout();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    logon("TLE_ADMINISTRATOR", "tle010");
    ShowConnectorsPage page = new ShowConnectorsPage(context).load();
    page.deleteAllNamed(CONNECTOR);
    CanvasModulePage modules = getToCourseModules(COURSE_NAME);
    modules.deleteModuleItemsNamed(MODULE_NAME, ITEM_NAME);
    modules.logout();
    // delete external tool from CanvasContributeTest
    CanvasSettingsPage settings = getToCourseSettings(COURSE_NAME);
    CanvasAppsTab apps = settings.apps();
    apps.deleteAllApps(EXTERNAL_TOOL_NAME);
  }
}
