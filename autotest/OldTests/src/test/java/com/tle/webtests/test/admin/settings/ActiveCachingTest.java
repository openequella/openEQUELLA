package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.ActiveCacheTreeNode;
import com.tle.webtests.pageobject.settings.ActiveCachingPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ActiveCachingTest extends AbstractSessionTest {
  private static final String DISCARDABLE_GROUP_NAME = "Gang's all here";

  /** Initial test in sequence, assumes page loads with checkbox unselected and tree not visible. */
  @Test
  public void testToggleEnableUse() {
    ActiveCachingPage acp = logonToActiveCachingPage();
    acp.setEnableUse(true);
    assertTrue(acp.userTreeVisible(), "Expected tree visibility after toggle of use");
    acp.save();
    logout();

    // having reversed the enable/view controls button, we expect a
    // reversal; of tree view/hide
    acp = logonToActiveCachingPage();
    assertTrue(acp.userTreeVisible(), "Expected tree visibility");
    // turn out the lights before leaving ...
    acp.setEnableUse(false);
    acp.save();
    assertFalse(acp.userTreeVisible(), "Expected tree invisibility");
    logout();
  }

  @Test(dependsOnMethods = "testToggleEnableUse")
  public void testAddSubgroup() {
    ActiveCachingPage acp = logonToActiveCachingPage();
    acp.setEnableUse(true);
    acp.save();

    // throws an exception if no unique root
    ActiveCacheTreeNode root = acp.findRootNode();
    // Add a group
    root.addSubGroup(DISCARDABLE_GROUP_NAME); // no problems anticipated
    acp.save();
  }

  @Test(dependsOnMethods = "testAddSubgroup")
  public void testDeleteNode() {
    ActiveCachingPage acp = logonToActiveCachingPage();
    acp.deleteNode(DISCARDABLE_GROUP_NAME);
    acp.save();
  }

  // @Test
  // public void testAttemptAddEmptyName()
  // {
  // ActiveCachingPage acp = logonToActiveCachingPage();
  // boolean oldEnableSetting = acp.getEnableUseChecked();
  // if( !oldEnableSetting )
  // {
  // acp.setEnableUse(true);
  // acp.save();
  // acp.get();
  // }
  // WebElement root = acp.highlightRootNode();
  // AbstractPage<?> outcome = acp.addSubgroup(root, "", true);
  // assertTrue(LoginPage.class.equals(outcome.getClass()),
  // "Expected outcome to be " + LoginPage.class.getSimpleName() + " but was "
  // + outcome.getClass().getSimpleName() + '.');
  // logout();
  // }

  private ActiveCachingPage logonToActiveCachingPage() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.activeCachingSettings();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    ActiveCachingPage acp = logonToActiveCachingPage();
    if (acp.getEnableUseChecked()) {
      acp = acp.setEnableUse(true);
    }
    if (acp.doesNodeExist(DISCARDABLE_GROUP_NAME)) {
      acp = acp.deleteNode(DISCARDABLE_GROUP_NAME);
    }

    acp = acp.setEnableUse(false);
    acp.save();
  }
}
