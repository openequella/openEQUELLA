/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import io.github.openequella.pages.dashboard.DashboardPage;
import io.github.openequella.pages.dashboard.PortletType$;
import io.github.openequella.pages.dashboard.portlets.PortletFactory;
import io.github.openequella.pages.dashboard.portlets.ScriptedPortlet;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;
import testng.annotation.NewUIOnly;
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

  private static final String TIMER_SPAN_ID = "timer";

  @Test
  public void testCreateEntity() {
    logon();
    ShowUserScriptsPage scriptsPage = new SettingsPage(context).load().userScriptsPage();

    scriptsPage = createDisplayScript(scriptsPage);
    scriptsPage = createExecutableScriptFromDisplay(scriptsPage);
    scriptsPage = createSupportingModules(scriptsPage);
    scriptsPage = validateAndDisableFaultyScript(scriptsPage);

    verifyScriptsPresent(scriptsPage);
  }

  @Test(dependsOnMethods = {"testCreateEntity"})
  public void scriptModuleTest() {
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

  @NewUIOnly
  @Test(dependsOnMethods = {"testCreateEntity"})
  public void testPortletScriptLoadingNewUi() {
    logon();
    DashboardPage dashboardPage = new DashboardPage(context).get();
    dashboardPage.openCreatePortletPage(PortletType$.MODULE$.Scripted());

    dashboardPage = setupScriptedPortletConfiguration().save(new DashboardPage(context));
    dashboardPage.get();

    String portletTitle = scriptedPortlet.toString();

    ScriptedPortlet scriptPortlet =
        dashboardPage.getPortlet(PortletFactory.Scripted$.MODULE$, portletTitle);
    boolean isScriptExecuted = scriptPortlet.isScriptCountdownFinished(TIMER_SPAN_ID);

    Assert.assertTrue(dashboardPage.hasPortlet(portletTitle), "portlet didn't save");
    Assert.assertTrue(isScriptExecuted, "script didn't work :(");
  }

  @OldUIOnly
  @Test(dependsOnMethods = {"testCreateEntity"})
  public void testPortletScriptLoading() {
    logon().addPortal(new FreemarkerPortalEditPage(context));
    HomePage home = setupScriptedPortletConfiguration().save(new HomePage(context));

    String portletTitle = scriptedPortlet.toString();
    boolean isScriptExecuted =
        new FreemarkerPortalSection(context, portletTitle).isScriptCountdownFinished(TIMER_SPAN_ID);
    Assert.assertTrue(home.portalExists(portletTitle), "portlet didn't save");
    Assert.assertTrue(isScriptExecuted, "script didn't work :(");
  }

  private ShowUserScriptsPage createDisplayScript(ShowUserScriptsPage scriptsPage) {
    EditUserScriptPage scriptEdit = scriptsPage.createScript();

    scriptEdit.setName(displayScript);
    scriptEdit.pickScriptType("display");
    scriptEdit.setScript(DISPLAY_SCRIPT, false);

    return scriptEdit.save();
  }

  private ShowUserScriptsPage createExecutableScriptFromDisplay(ShowUserScriptsPage scriptsPage) {
    EditUserScriptPage scriptEdit = scriptsPage.cloneScript(displayScript);

    scriptEdit.setName(executableScript);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("module1");
    scriptEdit.setScript(EXECUTE_SCRIPT, true);

    return scriptEdit.save();
  }

  private ShowUserScriptsPage createSupportingModules(ShowUserScriptsPage scriptsPage) {
    EditUserScriptPage scriptEdit = scriptsPage.createScript();

    scriptEdit.setName(moduleItemNamePrefix);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("prefixModule");
    scriptEdit.setScript(MODULE_PREFIX, true);
    scriptsPage = scriptEdit.save();

    scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(moduleItemName);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("itemNameModule");
    scriptEdit.setScript(MODULE_ITEMNAME, true);
    scriptsPage = scriptEdit.save();

    scriptEdit = scriptsPage.createScript();
    scriptEdit.setName(moduleSetItemName);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setModuleName("itemNameModule");
    scriptEdit.setScript(MODULE_ITEMMODIFY, true);
    scriptEdit.saveWithErrors("Module name has to be unique");

    scriptEdit.setModuleName("itemModify");

    return scriptEdit.save();
  }

  private ShowUserScriptsPage validateAndDisableFaultyScript(ShowUserScriptsPage scriptsPage) {
    EditUserScriptPage scriptEdit = scriptsPage.createScript();

    // It is unclear why there are different error messages in old and new UI here, but this
    // matches existing test behaviour.
    scriptEdit.saveWithErrors(
        testConfig.isNewUI() ? "There is no script to save" : "You must enter a title");

    scriptEdit.setName(disabledScript);
    scriptEdit.pickScriptType("executable");
    scriptEdit.setScript("\"", true);
    Assert.assertFalse(scriptEdit.syntaxOk());

    scriptEdit.setScript("var whatever;", true);
    Assert.assertTrue(scriptEdit.syntaxOk());

    scriptEdit.saveWithErrors("Module name is mandatory");

    scriptEdit.setModuleName("whatever");
    scriptsPage = scriptEdit.save();
    scriptsPage.disableEntity(disabledScript);

    return scriptsPage;
  }

  private void verifyScriptsPresent(ShowUserScriptsPage scriptsPage) {
    final List<PrefixedName> enabledScripts =
        List.of(
            executableScript,
            displayScript,
            moduleItemNamePrefix,
            moduleItemName,
            moduleSetItemName);
    enabledScripts.forEach(script -> Assert.assertFalse(scriptsPage.isEntityDisabled(script)));

    Assert.assertTrue(scriptsPage.isEntityDisabled(disabledScript));
  }

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
    new DashboardAdminPage(context).load().deleteAllPortlet(scriptedPortlet.toString());
  }

  private FreemarkerPortalEditPage setupScriptedPortletConfiguration() {
    FreemarkerPortalEditPage editPortlet = new FreemarkerPortalEditPage(context).get();
    editPortlet.setTitle(scriptedPortlet);
    editPortlet.loadFreemarkerScript(displayScript);
    editPortlet.switchToClientScript();
    editPortlet.loadClientSideScript(executableScript);
    return editPortlet;
  }
}
