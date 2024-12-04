package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.externaltools.EditExternalToolPage;
import com.tle.webtests.pageobject.externaltools.ShowExternalToolsPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class LTISettingsTest extends AbstractSessionTest {
  @Name("test tool")
  private static PrefixedName LTI_TITLE;

  private static final String LAUNCHABLE_WIKIPEDIA_LTI =
      "https://www.edu-apps.org/tool_redirect?id=wikipedia";

  @Test
  public void testAddLTI() {
    ShowExternalToolsPage tools = logonToLTISettings();
    // TODO test form validation (dodgy base url etc)
    tools = createNewExternalTool(tools);
    assertTrue(tools.entityExists(LTI_TITLE), "Expected new tool but found none");
  }

  // TODO test the created tool works before deletion

  /**
   * Presupposes that the LTI titled {@value LTISettingsTest#LTI_TITLE} exists, and is hereby tidied
   * up while testing deletions.
   */
  @Test(dependsOnMethods = "testAddLTI")
  public void testDeleteExisting() {
    ShowExternalToolsPage tools = logonToLTISettings();
    tools.disableEntity(LTI_TITLE);
    tools.deleteEntity(LTI_TITLE);
    assertFalse(tools.entityExists(LTI_TITLE));
  }

  private ShowExternalToolsPage createNewExternalTool(ShowExternalToolsPage tools) {
    EditExternalToolPage editPage = tools.createTool();
    editPage.setName(LTI_TITLE);
    editPage.setBaseUrl(LAUNCHABLE_WIKIPEDIA_LTI);
    editPage.setDescription("descr");
    editPage.setKeySecret("woopwoop", "calldapolice");
    editPage.setCustomParams("custard=yellow matter");
    editPage.setShareOptions(true, false);
    return editPage.save();
  }

  private ShowExternalToolsPage logonToLTISettings() {
    logon("autotest", "automated");
    return new SettingsPage(context).load().externalToolsSettings();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    ShowExternalToolsPage tools = logonToLTISettings();
    tools.deleteAllNamed(LTI_TITLE);
    super.cleanupAfterClass();
  }
}
