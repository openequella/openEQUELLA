package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.AddShortcutURLPage;
import com.tle.webtests.pageobject.settings.ShortcutURLsSettingsPage;
import com.tle.webtests.test.AbstractIntegrationTest;
import java.util.List;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

@TestInstitution("fiveo")
public class ShortcutURLsTest extends AbstractIntegrationTest {
  public static final String DISCARDABLE_URL =
      "http://purple-excess.dyndns.ws"; // Larry's sometime personal URL, try :8078
  private final List<String> addedList = Lists.newArrayList();
  private String shortcutName;

  @Test
  public void testAddShortcutURL() {
    ShortcutURLsSettingsPage susp = logonToShortcutURLsSettingsPage();
    shortcutName = getClass().getSimpleName();
    susp.addShortcutTextAndURL(shortcutName, DISCARDABLE_URL);
    addedList.add(shortcutName);
    assertTrue(susp.containsShortcut(shortcutName), "Expected " + shortcutName + " to be there");
  }

  @Test
  public void testServlet() {
    ShortcutURLsSettingsPage susp = logonToShortcutURLsSettingsPage();
    susp.addShortcutTextAndURL("integtester", IntegrationTesterPage.getUrl());
    addedList.add("integtester");
    assertTrue(susp.containsShortcut("integtester"), "Expected integtester to be there");
    context.getDriver().get(context.getBaseUrl() + "s/integtester");
    new IntegrationTesterPage(context, "", "").get();
  }

  @Test(dependsOnMethods = "testAddShortcutURL")
  public void testDeleteShortcutUrl() {
    ShortcutURLsSettingsPage susp = logonToShortcutURLsSettingsPage();
    susp.deleteShortcut(shortcutName);
    assertFalse(susp.containsShortcut(shortcutName), "Expected " + shortcutName + " to be gone");
    addedList.remove(shortcutName);
  }

  @Test
  public void testInvalidEntry() {
    ShortcutURLsSettingsPage susp = logonToShortcutURLsSettingsPage();
    AddShortcutURLPage dialog =
        susp.addShortcutTextAndURLFailure("", "", false, "Shortcut cannot be blank.");
    susp = dialog.close();
    susp.load();
    dialog = susp.addShortcutTextAndURLFailure("valid", "", true, "URL cannot be blank.");
    susp = dialog.close();
    susp.load();
    dialog =
        susp.addShortcutTextAndURLFailure(
            "valid",
            "This has no chance of being a URL",
            true,
            "The text entered \"This has no chance of being a URL\" is not a valid URL.");
    dialog.close();
  }

  private ShortcutURLsSettingsPage logonToShortcutURLsSettingsPage() {
    logon("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.shortcutURLsSettingsPage();
  }

  @Override
  protected void cleanupAfterClass() {
    ShortcutURLsSettingsPage susp = logonToShortcutURLsSettingsPage();
    for (String shortcut : addedList) {
      susp.deleteShortcut(shortcut);
    }
  }
}
