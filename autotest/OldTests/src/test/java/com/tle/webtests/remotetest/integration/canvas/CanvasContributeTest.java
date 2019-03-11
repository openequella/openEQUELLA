package com.tle.webtests.remotetest.integration.canvas;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.LtiAutoConfigPage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasAddModuleItemDialog;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasModulePage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasSettingsPage;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasTextEditor;
import com.tle.webtests.pageobject.integration.canvas.course.CanvasWikiPage;
import com.tle.webtests.pageobject.integration.canvas.course.settings.CanvasAddNewAppDialog;
import com.tle.webtests.pageobject.integration.canvas.course.settings.CanvasAppsTab;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import org.testng.annotations.Test;

@Test(groups = "tests")
public class CanvasContributeTest extends AbstractCanvasTest {
  private static final String OAUTH_CLIENT_ID = "canvas-autotest";
  private static final String OAUTH_CLIENT_SECRET = "ff835cf8-8a34-4f8c-b94d-10b892ed65be";
  private String selectItem;
  private String editorItem;

  @Test(groups = "setupModuleAndConnector")
  public void testAddExternalTool() {
    LtiAutoConfigPage configXmlPage = new LtiAutoConfigPage(context).load();
    String autoConfigXML = configXmlPage.getXML();

    CanvasSettingsPage settings = getToCourseSettings(COURSE_NAME);
    CanvasAppsTab apps = settings.apps();
    CanvasAddNewAppDialog addNewApp = apps.addNewApp();
    addNewApp.addByPasting(
        EXTERNAL_TOOL_NAME, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, autoConfigXML, apps);
    assertTrue(apps.appExists(EXTERNAL_TOOL_NAME));
    apps.logout();
  }

  @Test(dependsOnMethods = {"testAddExternalTool"})
  public void testExternalToolModules() {
    selectItem = context.getFullName("select me");
    logon("autotest", "automated");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, selectItem);
    wizard.save().publish();

    CanvasModulePage modules = getToCourseModules(COURSE_NAME);
    CanvasAddModuleItemDialog addItem = modules.addModuleItem(MODULE_NAME);
    SelectionSession selection = addItem.startSelectionSession(EXTERNAL_TOOL_NAME);
    ItemListPage searchPage = selection.homeExactSearch(selectItem);
    SelectionCheckoutPage checkout =
        searchPage
            .getResult(1)
            .clickAction("Select summary page", new SelectionCheckoutPage(context));
    addItem = checkout.returnSelection(addItem);
    addItem.add(modules);
    modules.waitForModuleItem(MODULE_NAME, selectItem);
    assertTrue(modules.moduleItemExists(MODULE_NAME, selectItem));
    modules.logout();
  }

  @Test(dependsOnMethods = {"testAddExternalTool"})
  public void testExternalToolPages() {
    final String IMAGE = "veronicas_wall1.jpg";
    editorItem = context.getFullName("from an editor yo");

    logon("autotest", "automated");
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, editorItem);
    wizard.addFile(2, IMAGE);
    wizard.save().publish();

    CanvasWikiPage wikiPage = getToCourseWiki(COURSE_NAME);
    CanvasTextEditor editor = wikiPage.newPage();
    editor.setTitle(editorItem);
    SelectionSession selectionSession = editor.startSelectionSession();
    ItemListPage searchPage = selectionSession.homeExactSearch(editorItem);
    SelectionCheckoutPage checkout =
        searchPage.getResult(1).viewSummary().attachments().selectSingleAttachment(IMAGE);
    checkout.returnSelection(editor);
    assertTrue(editor.picEmedded(IMAGE));
    editor.cancel(wikiPage);
    wikiPage.logout();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    CanvasModulePage modules = getToCourseModules(COURSE_NAME);
    modules.deleteModuleItemsNamed(MODULE_NAME, selectItem);
  }
}
