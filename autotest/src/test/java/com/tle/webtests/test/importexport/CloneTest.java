package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.institution.ClonePage;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.StatusPage;
import java.io.File;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CloneTest extends AbstractInstTest {
  /** Distinguishes the clone from the institution it was made from, in both URL and short name. */
  private static final String CLONE_SUFFIX = "clone";

  @Test(dataProvider = "toClone")
  public void cloneInstitution(File instFolder) {
    String institutionUrl = testConfig.getInstitutionUrl(instFolder.getName());
    String cloneShortName = cloneShortName(instFolder);

    InstitutionListTab listTab = new InstitutionListTab(context).load();
    listTab = deleteIfPresent(listTab, cloneUrl(instFolder));
    if (listTab.institutionExists(institutionUrl)) {
      ClonePage clone = listTab.clone(institutionUrl);
      StatusPage<InstitutionListTab> statusPage = clone.clone(cloneUrl(instFolder), cloneShortName);
      assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
      statusPage.back();
    }
  }

  @Test(dependsOnMethods = "cloneInstitution", dataProvider = "toClone", alwaysRun = true)
  public void deleteInstitutions(File instFolder) {
    deleteIfPresent(new InstitutionListTab(context).load(), cloneUrl(instFolder));
  }

  private String cloneShortName(File instFolder) {
    return instFolder.getName() + CLONE_SUFFIX;
  }

  /** The clone's URL, on the same scheme as the fixture it was made from. */
  private String cloneUrl(File instFolder) {
    return testConfig.getInstitutionUrl(cloneShortName(instFolder), testConfig.isSsl(instFolder));
  }

  @DataProvider(parallel = false)
  public Object[][] toClone() {
    return institutionFixtures();
  }
}
