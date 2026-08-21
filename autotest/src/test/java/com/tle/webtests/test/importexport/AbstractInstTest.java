package com.tle.webtests.test.importexport;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.pageobject.institution.InstitutionListTab;
import com.tle.webtests.pageobject.institution.ServerAdminLogonPage;
import com.tle.webtests.pageobject.institution.StatusPage;
import com.tle.webtests.test.AbstractTest;
import java.util.Arrays;
import java.util.Objects;

/**
 * Shared ground for the tests that drive institutions from the server admin console - importing,
 * exporting and cloning them - rather than working inside one.
 */
public abstract class AbstractInstTest extends AbstractTest {
  @Override
  protected boolean isInstitutional() {
    return false;
  }

  /** All three of these tests do their work logged in to the server admin console. */
  @Override
  protected void prepareBrowserSession() {
    new ServerAdminLogonPage(context)
        .load()
        .logon(testConfig.getAdminPassword(), new InstitutionListTab(context));
  }

  /**
   * Every fixture folder holding an exploded institution, as single-argument rows for a TestNG data
   * provider. Folders without one - {@code importexport}, which holds whole archives instead - are
   * not fixtures these tests can drive, and are left out.
   */
  protected static Object[][] institutionFixtures() {
    return Arrays.stream(Objects.requireNonNull(TestConfig.getInstitutionsFolder().listFiles()))
        .filter(instDir -> TestConfig.getInstitutionTree(instDir.getName()).isDirectory())
        .map(instDir -> new Object[] {instDir})
        .toArray(Object[][]::new);
  }

  /**
   * Deletes the institution at this URL if the server has one, and hands back a list tab to carry
   * on from.
   *
   * <p>Every one of these tests has to cope with an institution already being there, whether from a
   * previous run or from {@code setupForTests}.
   */
  protected InstitutionListTab deleteIfPresent(InstitutionListTab listTab, String institutionUrl) {
    if (!listTab.institutionExists(institutionUrl)) {
      return listTab;
    }
    StatusPage<InstitutionListTab> statusPage = listTab.delete(institutionUrl);
    assertTrue(statusPage.waitForFinish(), statusPage.getErrorText());
    return statusPage.back();
  }
}
