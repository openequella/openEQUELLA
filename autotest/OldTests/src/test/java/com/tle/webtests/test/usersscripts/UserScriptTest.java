package com.tle.webtests.test.usersscripts;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.portal.DashboardAdminPage;
import com.tle.webtests.pageobject.portal.FreemarkerPortalEditPage;
import com.tle.webtests.pageobject.portal.FreemarkerPortalSection;
import com.tle.webtests.pageobject.scripting.BulkExecuteScriptDialog;
import com.tle.webtests.pageobject.searching.BulkPreviewPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.userscripts.EditUserScriptPage;
import com.tle.webtests.pageobject.userscripts.ShowUserScriptsPage;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import testng.annotation.OldUIOnly;

@TestInstitution("asc")
public class UserScriptTest extends AbstractCleanupTest {
  @Name("Display Script")
  private static PrefixedName displayScript;

  @Name("Executable Script")
  private static PrefixedName executableScript;

  @Name("Disabled Script")
  private static PrefixedName disabledScript;

  @Name("Scripted Portlet")
  private static PrefixedName scriptedPortlet;

  @Name("Item Name Prefix Module")
  private static PrefixedName moduleItemNamePrefix;

  @Name("Export Item Name Module")
  private static PrefixedName moduleItemName;

  @Name("Set Item Name Module")
  private static PrefixedName moduleSetItemName;

  private static final String DISPLAY_SCRIPT = "<span id=\"timer\"></span>";
  private static final String EXECUTE_SCRIPT =
      "var count=5;var counter=setInterval(timer, 1000);function timer(){if (count <="
          + " 0){clearInterval(counter);document.getElementById(\"timer\").innerHTML=\"finished!\";return;}document.getElementById(\"timer\").innerHTML=count"
          + " + \" seconds\";count=count-1;}";
  private static final String MODULE_PREFIX =
      "var itemNamePrefix = \"test-\"; exports.itemNamePrefix = itemNamePrefix;";
  private static final String MODULE_ITEMNAME =
      "exports.getItemName = function(){return xml.get(\"/name\");}";
  private static final String MODULE_ITEMMODIFY =
      "var prefix = require(\"prefixModule\"); var itemFun = require(\"itemNameModule\");"
          + " exports.setName = function(){var newItemName = prefix.itemNamePrefix +"
          + " itemFun.getItemName();xml.set(\"/newItemName\",newItemName);};";
  private static final String SET_ITEM_NAME_SCRIPT = "require(\"itemModify\").setName();";

  @Test
  public void testCreateEntity() {
    logon();
    ShowUserScriptsPage scriptsPage = new SettingsPage(context).load().userScriptsPage();
    // create 1 entity
    EditUserScriptPage scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(displayScript);
    scriptEdit.pickScriptType("display");
    scriptEdit.setScript(DISPLAY_SCRIPT, false);
    scriptsPage = scriptEdit.save();
    // clone -> change type
    scriptEdit = scriptsPage.cloneScript(displayScript);
    scriptEdit.setName(executableScript);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("module1");
    scriptEdit.setScript(EXECUTE_SCRIPT, true);
    scriptsPage = scriptEdit.save();

    // create module prefixModule
    scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(moduleItemNamePrefix);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("prefixModule");
    scriptEdit.setScript(MODULE_PREFIX, true);
    scriptsPage = scriptEdit.save();

    // create module itemNameModule
    scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(moduleItemName);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("itemNameModule");
    scriptEdit.setScript(MODULE_ITEMNAME, true);
    scriptsPage = scriptEdit.save();

    // create module prefixModule
    scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(moduleSetItemName);
    scriptEdit.pickScriptType("executable");
    // set an exist module name
    scriptEdit.setModuleName("itemNameModule");
    scriptEdit.setScript(MODULE_ITEMMODIFY, true);
    scriptEdit.saveWithErrors();
    Assert.assertTrue(scriptEdit.isModuleNameInvalid());
    scriptEdit.setModuleName("itemModify");
    scriptsPage = scriptEdit.save();

    // create 2 more, disable one, delete other one
    scriptEdit = scriptsPage.createScript();
    scriptEdit.saveWithErrors();
    // Skip this check in new UI until Github issue #1150 is solved
    if (!scriptEdit.usingNewUI()) {
      Assert.assertTrue(scriptEdit.isNameInvalid());
    }
    Assert.assertTrue(scriptEdit.isScriptInvalid());
    scriptEdit.setName(disabledScript);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setScript("\"", true);
    Assert.assertFalse(scriptEdit.syntaxOk());
    scriptEdit.setScript("var whatever;", true);
    Assert.assertTrue(scriptEdit.syntaxOk());
    scriptEdit.saveWithErrors();
    Assert.assertTrue(scriptEdit.isModuleNameInvalid());
    scriptEdit.setModuleName("whatever");
    scriptsPage = scriptEdit.save();
    scriptsPage.disableEntity(disabledScript);

    Assert.assertTrue(scriptsPage.entityExists(executableScript));
    Assert.assertTrue(scriptsPage.entityExists(disabledScript));
    Assert.assertTrue(scriptsPage.entityExists(displayScript));
    Assert.assertTrue(scriptsPage.entityExists(moduleItemNamePrefix));
    Assert.assertTrue(scriptsPage.entityExists(moduleItemName));
    Assert.assertTrue(scriptsPage.entityExists(moduleSetItemName));
    Assert.assertTrue(scriptsPage.isEntityDisabled(disabledScript));
  }

  @Test(dependsOnMethods = {"testCreateEntity"})
  public void scriptMoudleTest() {
    logon();
    ItemAdminPage admin = new ItemAdminPage(context).load();
    admin.search("Facet 4");
    admin.bulk().selectAll();
    BulkExecuteScriptDialog scriptDialog = admin.bulk().exectueScript();
    scriptDialog.typeCode(SET_ITEM_NAME_SCRIPT);
    scriptDialog.checkSyntax();
    assertTrue(scriptDialog.syntaxPass());
    BulkPreviewPage preview = scriptDialog.preview();
    assertTrue(preview.isNodePresent("newItemName: test-Facet 4"));
    preview.closeDialog(new ItemAdminPage(context));
  }

  // TODO: OEQ-2720 enable test in new UI.
  @OldUIOnly
  @Test(dependsOnMethods = {"testCreateEntity"})
  public void testPortletScriptLoading() {
    HomePage home = logon();
    FreemarkerPortalEditPage editPortlet = new FreemarkerPortalEditPage(context);
    editPortlet = home.addPortal(editPortlet);
    editPortlet.setTitle(scriptedPortlet);
    editPortlet.loadFreemarkerScript(displayScript);
    editPortlet.switchToClientScript();
    editPortlet.loadClientSideScript(executableScript);
    home = editPortlet.save(home);
    Assert.assertTrue(home.portalExists(scriptedPortlet.toString()), "portlet didn't save");

    FreemarkerPortalSection portal =
        new FreemarkerPortalSection(context, scriptedPortlet.toString()).get();
    Assert.assertTrue(portal.scriptCountdownTest("timer"), "script didn't work :(");
  }

  // TODO: if need coverage can test script loading on URL checking settings
  // and Bulk execute scrip op.

  @Override
  protected void cleanupAfterClass() throws Exception {
    ShowUserScriptsPage sp = new ShowUserScriptsPage(context).load();
    sp.deleteAllNamed(
        executableScript,
        disabledScript,
        displayScript,
        moduleItemNamePrefix,
        moduleItemName,
        moduleSetItemName);
    new DashboardAdminPage(context).load().deleteAll(scriptedPortlet.toString());
  }
}
