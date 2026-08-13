package com.tle.webtests.test.searching.manage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.scripting.BulkExecuteScriptDialog;
import com.tle.webtests.pageobject.searching.BulkPreviewPage;
import com.tle.webtests.pageobject.searching.BulkResultsPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.Random;
import org.testng.annotations.Test;

@TestInstitution("manageresources")
public class BulkScriptExecutionTest extends AbstractCleanupTest {
  private final String USER_A = "AutoLogin";
  private final String USER_B = "AutoTest";
  private final String PASSWORD = "automated";
  private final Random RAND = new Random();

  @Test
  public void testBulkExecution() {

    logon(USER_A, PASSWORD);
    for (int x = 0; x < 5; x++) {
      MenuSection menu = new MenuSection(context).get();
      WizardPageTab wizard = menu.clickContribute("Metadata testing collection");
      wizard.editbox(1, namePrefix + " " + x);
      wizard.save().publish();
    }
    logout();
    logon(USER_B, PASSWORD);
    ItemAdminPage admin = new ItemAdminPage(context).load();
    admin.search(namePrefix);
    admin.bulk().selectAll();
    BulkExecuteScriptDialog scriptDialog = admin.bulk().exectueScript();
    scriptDialog.typeCode("[messed up) parenthesis {;");
    scriptDialog.checkSyntax();
    assertTrue(scriptDialog.syntaxError());
    assertTrue(scriptDialog.errorMessageContains("missing ] after element list"));
    scriptDialog.typeCode("missing semicolons between variables");
    scriptDialog.checkSyntax();
    assertTrue(scriptDialog.syntaxError());
    assertTrue(scriptDialog.errorMessageContains("missing ; before statement"));
    scriptDialog.typeCode("currentItem.setOwner(user.getID());");
    scriptDialog.checkSyntax();
    assertTrue(scriptDialog.syntaxPass());
    BulkResultsPage results = scriptDialog.execute();
    assertTrue(results.waitAndFinish(admin));

    SummaryPage summary = SearchPage.searchAndView(context, namePrefix + " " + RAND.nextInt(4));
    assertEquals(USER_B, summary.getOwner());
  }

  @Test(dependsOnMethods = {"testBulkExecution"})
  public void testScriptPreview() {
    logon(USER_B, PASSWORD);
    ItemAdminPage manageResource = new ItemAdminPage(context).load();
    manageResource.search(namePrefix);
    manageResource.bulk().selectAll();
    BulkExecuteScriptDialog dialog = manageResource.bulk().exectueScript();
    dialog.typeCode("asdf");
    dialog.checkSyntax();
    BulkPreviewPage preview = dialog.preview();
    assertTrue(preview.isPreviewErrored());
    assertTrue(preview.getErrorMessage().contains("is not defined"));
    dialog = preview.previous(dialog);
    dialog.typeCode("xml.set(\"item/year\",\"changed\");");

    dialog.checkSyntax();
    preview = dialog.preview();
    assertTrue(preview.isNodePresent("year: changed"));
    preview.execute().waitAndFinish(manageResource);

    SummaryPage summary = SearchPage.searchAndView(context, namePrefix + " " + RAND.nextInt(4));
    assertTrue(summary.getItemDescription().equals("changed"));
  }
}
