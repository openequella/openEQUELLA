/** */
package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.SelectionSessionSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class SelectionSessionSettingsTest extends AbstractSessionTest {
  public static final String SELECTION_SESSION_SETTINGS_LINK =
      "Selection sessions"; // original property key: quickcontributeandversionsettings.title

  @Test
  public void testQuickContributeSetting() {
    SelectionSessionSettingsPage sssp = logonToSelectionSessionSettings();
    int availableCollections = sssp.countAvailableCollections();
    switch (availableCollections) {
      case 0:
        fail("Test requires that institution have selectable collections");
        break;
      case 1:
      case 2:
        sssp.selectCollectionByIndex(availableCollections - 1);
        break;
      default:
        sssp.selectCollectionByIndex(2);
    }
    sssp = sssp.save();
    String chosenCollection = sssp.getSelectedCollection();
    logout();
    sssp = logonToSelectionSessionSettings();
    assertTrue(
        chosenCollection.equals(sssp.getSelectedCollection()),
        "Expected " + chosenCollection + " but got " + sssp.getSelectedCollection() + '.');
    logout();
  }

  @Test
  public void testVersionViewOptions() {
    SelectionSessionSettingsPage sssp = logonToSelectionSessionSettings();
    int numVersionViewOptions = sssp.getVersionViewOptionsSize();
    assertTrue(
        numVersionViewOptions >= 4,
        "This test assumes there are at least 4 configurable version view options, but only found "
            + numVersionViewOptions
            + '.');
    String selectedOptionVal = sssp.getCheckedViewOptionValue();
    // If no checked option exists, check the first one (we'll save that as the default from now on)
    if (selectedOptionVal == null) {
      sssp.selectVersionViewOptionByIndex(0);
      // The 'checked' attribute of the input doesn't update immediately, so we save & get
      sssp.save();
      selectedOptionVal = sssp.getCheckedViewOptionValue();
    }

    // set to the second, save, check persistence
    sssp.selectVersionViewOptionByIndex(1);
    sssp.save();
    String secondOptionVal = sssp.getCheckedViewOptionValue();
    sssp.save();
    logout();
    sssp = logonToSelectionSessionSettings();
    assertTrue(
        sssp.getCheckedViewOptionValue().equals(secondOptionVal),
        "Expected "
            + secondOptionVal
            + " to persist, but we have "
            + sssp.getCheckedViewOptionValue()
            + '.');

    // set to the second, save, check persistence
    sssp.selectVersionViewOptionByIndex(2);
    sssp.save();
    String thirdOptionVal = sssp.getCheckedViewOptionValue();
    sssp.save();
    logout();
    sssp = logonToSelectionSessionSettings();
    assertTrue(
        sssp.getCheckedViewOptionValue().equals(thirdOptionVal),
        "Expected "
            + thirdOptionVal
            + " to persist, but we have "
            + sssp.getCheckedViewOptionValue()
            + '.');
  }

  private SelectionSessionSettingsPage logonToSelectionSessionSettings() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.selectionSessionSettingsPage();
  }

  @Override
  protected void cleanupAfterClass() {
    SelectionSessionSettingsPage sssp = logonToSelectionSessionSettings();
    sssp.selectCollectionByIndex(0);
    sssp.selectVersionViewOptionByIndex(0);
    sssp.save();
  }
}
