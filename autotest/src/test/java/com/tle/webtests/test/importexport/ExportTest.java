package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.ExportPage;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.StatusPage;
import java.io.File;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ExportTest extends AbstractInstTest {

  @Test(dataProvider = "toExport")
  public void exportInstitution(File instFolder) {
    String institutionUrl = testConfig.getInstitutionUrl(instFolder.getName());

    InstitutionListTab listTab = new InstitutionListTab(context).load();
    if (listTab.institutionExists(institutionUrl)) {
      ExportPage export = listTab.export(institutionUrl);
      StatusPage<InstitutionListTab> statusPage = export.export();
      assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
      statusPage.back();
    }
  }

  @DataProvider(parallel = false)
  public Object[][] toExport() {
    return institutionFixtures();
  }
}
