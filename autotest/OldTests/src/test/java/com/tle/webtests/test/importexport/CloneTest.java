package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.ClonePage;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.ServerAdminLogonPage;
import com.tle.webtests.pageobject.institution.StatusPage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CloneTest extends AbstractInstTest {
  @Override
  protected void prepareBrowserSession() {
    new ServerAdminLogonPage(context)
        .load()
        .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
  }

  @Test(dataProvider = "toClone")
  public void cloneInstitution(File instFolder) {
    String shortName = instFolder.getName();
    String instutionUrl = context.getBaseUrl() + shortName + '/';
    String newInstutionUrl = context.getBaseUrl() + shortName + "clone/";
    String newShortName = instFolder.getName() + "clone";

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
    String origShortName = instFolder.getName();
    String instutionUrl = context.getBaseUrl() + origShortName + "clone/";

    InstitutionListTab listTab = new InstitutionListTab(context).load();
    if (listTab.institutionExists(instutionUrl)) {
      StatusPage<InstitutionListTab> statusPage = listTab.delete(instutionUrl);
      assertTrue(statusPage.waitForFinish());
      statusPage.back();
    }
  }

  @DataProvider(parallel = false)
  public Object[][] toClone() throws Exception {
    File[] institutions = new File(testConfig.getTestFolder(), "tests").listFiles();
    List<Object[]> instDirs = new ArrayList<Object[]>();
    for (File instDir : institutions) {
      if (new File(instDir, INSTITUTION_FOLDER).exists()) {
        instDirs.add(new Object[] {instDir});
      }
    }
    return instDirs.toArray(new Object[instDirs.size()][]);
  }
}
