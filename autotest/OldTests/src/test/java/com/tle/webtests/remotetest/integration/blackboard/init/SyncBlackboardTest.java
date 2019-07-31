package com.tle.webtests.remotetest.integration.blackboard.init;

import com.tle.webtests.pageobject.integration.blackboard.BlackboardBuildingBlockListPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardCoursePage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardEquellaSettingsPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardLoginPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardMyInstitutionPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardUploadBuildingBlockPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardUploadWebServicePage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardWebServicesListPage;
import com.tle.webtests.remotetest.integration.blackboard.AbstractBlackboardTest;
import java.util.List;
import org.testng.annotations.Test;

@Test(groups = "syncBlackboard")
public class SyncBlackboardTest extends AbstractBlackboardTest {
  @Override
  protected void prepareBrowserSession() {
    new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  @Test
  public void replaceModule() {
    BlackboardBuildingBlockListPage blockPage = new BlackboardBuildingBlockListPage(context).load();
    blockPage.deleteEquella();

    BlackboardUploadBuildingBlockPage upload =
        new BlackboardUploadBuildingBlockPage(context).load();
    blockPage = upload.uploadLatest();
    // blockPage = blockPage.activateEquella();
  }

  @Test(dependsOnMethods = "replaceModule")
  public void replaceWebService() {
    BlackboardWebServicesListPage webServices =
        new BlackboardWebServicesListPage(context).load().deleteEquella();
    webServices = new BlackboardUploadWebServicePage(context).load().uploadLatest();
    webServices.activateEquella();
  }

  @Test(dependsOnMethods = "replaceModule")
  public void syncBlackboard() {
    BlackboardMyInstitutionPage indexPage =
        new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);

    List<String> courses = indexPage.listCourses();
    for (String courseName : courses) {
      BlackboardCoursePage course = indexPage.clickCourse(courseName);
      course.bulkDelete();
      course.setAvailible();
      indexPage = new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);
    }
  }

  @Test(dependsOnMethods = "replaceModule")
  public void setupBuildingBlock() {
    new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);
    BlackboardEquellaSettingsPage settingsPage =
        new BlackboardBuildingBlockListPage(context).load().equellaSettings();
    sleepyTime(750);
    settingsPage.setInstitutionUrl(context.getBaseUrl());
    settingsPage.setSharedSecretId("blackboard");
    settingsPage.setSharedSecretPassword("blackboard");
    settingsPage.setOauthDetails(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET);
    settingsPage.setNewWindow(true);
    settingsPage.save();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    // nothing
  }
}
