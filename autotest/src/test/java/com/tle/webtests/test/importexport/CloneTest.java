package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.ClonePage;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.ServerAdminLogonPage;
import com.tle.webtests.pageobject.institution.StatusPage;
import java.io.File;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CloneTest extends AbstractInstTest {
  /** Distinguishes the clone from the institution it was made from, in both URL and short name. */
  private static final String CLONE_SUFFIX = "clone";

  @Override
  protected void prepareBrowserSession() {
    new ServerAdminLogonPage(context)
        .load()
        .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
  }

  @Test(dataProvider = "toClone")
  public void cloneInstitution(File instFolder) {
    String instutionUrl = institutionUrl(instFolder);
    String newShortName = instFolder.getName() + CLONE_SUFFIX;
    String newInstutionUrl = context.getBaseUrl() + newShortName + '/';

    InstitutionListTab listTab = new InstitutionListTab(context).load();
    if (listTab.institutionExists(newInstutionUrl)) {
      StatusPage<InstitutionListTab> statusPage = listTab.delete(newInstutionUrl);
      assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
      listTab = statusPage.back();
    }
    if (listTab.institutionExists(instutionUrl)) {
      ClonePage clone = listTab.clone(instutionUrl);
      StatusPage<InstitutionListTab> statusPage = clone.clone(newInstutionUrl, newShortName);
      assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
      statusPage.back();
    }
  }

  @Test(dependsOnMethods = "cloneInstitution", dataProvider = "toClone", alwaysRun = true)
  public void deleteInstitutions(File instFolder) {
    String instutionUrl = context.getBaseUrl() + instFolder.getName() + CLONE_SUFFIX + '/';

    InstitutionListTab listTab = new InstitutionListTab(context).load();
    if (listTab.institutionExists(instutionUrl)) {
      StatusPage<InstitutionListTab> statusPage = listTab.delete(instutionUrl);
      assertTrue(statusPage.waitForFinish());
      statusPage.back();
    }
  }

  @DataProvider(parallel = false)
  public Object[][] toClone() {
    return institutionFixtures();
  }
}
