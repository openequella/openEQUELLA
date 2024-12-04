package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.HarvesterSkipDrmPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class HarvesterSkipDrmTest extends AbstractSessionTest {

  @Test
  public void testToggleSkipDrm() {
    HarvesterSkipDrmPage skipDrmPage = logonToHarvestSkipDrm();
    boolean oldSetting = skipDrmPage.getSkipDrmChecked();
    skipDrmPage.toggleSkipDrmChecked();
    boolean sanityCheck = skipDrmPage.getSkipDrmChecked();
    assertTrue(oldSetting != sanityCheck, "Expected checkbox to change value on click");
    skipDrmPage.save();
    logout();

    // log back in again, check reset value has persisted
    skipDrmPage = logonToHarvestSkipDrm();
    assertTrue(
        sanityCheck == skipDrmPage.getSkipDrmChecked(),
        "Expected value after earlier click to persist");
  }

  private HarvesterSkipDrmPage logonToHarvestSkipDrm() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.harvestSkipDrmSettings();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    HarvesterSkipDrmPage skipDrmPage = logonToHarvestSkipDrm();
    if (skipDrmPage.getSkipDrmChecked()) {
      skipDrmPage.toggleSkipDrmChecked();
      skipDrmPage.save();
    }
  }
}
