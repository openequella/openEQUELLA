package com.tle.webtests.test.viewing.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.viewitem.ModerationTab;
import com.tle.webtests.pageobject.viewitem.MoveCloneDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import org.testng.annotations.Test;

public class MoveItemTest extends BaseCloneMove {
  private static final int NUM_ITEMS = 2;

  @Test
  @SuppressWarnings("nls")
  public void testMoveAnotherWorkflow() {
    logon("SimpleModerator", "``````");
    String itemName = context.getFullName("Move");
    MoveCloneDialog moveDialog = createItem(itemName).adminTab().move();
    moveDialog.setTargetCollection("Simple 1 Step");
    WizardPageTab wizard = moveDialog.execute();
    wizard.editbox(1, context.getFullName("NewName"));
    SummaryPage finished = wizard.saveNoConfirm();

    ModerationTab moderationTab = finished.moderationTab();
    assertTrue(moderationTab.isTaskPresent("Only Step"));

    SummaryPage summary = moderationTab.summary();
    assertTrue(isTextPresent("Simple 1 Step"));

    verifyAttachment(summary, true);
  }

  @Test
  @SuppressWarnings("nls")
  public void testMoveToNoWorkflow() {
    testSingle("No Workflow", null);
  }

  @Test
  @SuppressWarnings("nls")
  public void testMoveToNoWorkflowXSLT() {
    testSingle("Different Schema", "Standard Schema");
  }

  @SuppressWarnings("nls")
  private void testSingle(String newCollection, String xslt) {
    logon("SimpleModerator", "``````");
    String itemName = context.getFullName("Single");
    MoveCloneDialog moveDialog = createItem(itemName).adminTab().move();
    moveDialog.setTargetCollection(newCollection);
    if (xslt != null) {
      moveDialog.setTransformation(xslt);
    }
    WizardPageTab wizard = moveDialog.execute();
    String newName = context.getFullName("SingleMoved");
    wizard.editbox(1, newName);
    SummaryPage summary = wizard.saveNoConfirm();
    assertEquals(summary.getItemTitle(), newName);
    assertTrue(isTextPresent(newCollection));
    verifyAttachment(summary, true);
  }

  @Test
  public void testBulkMove() {
    testBulk("No Workflow", null); // $NON-NLS-1$
  }

  @SuppressWarnings("nls")
  private void testBulk(String newCollection, String transformName) {
    logon("SimpleModerator", "``````");
    for (int i = 1; i <= NUM_ITEMS; i++) {
      createItem(context.getFullName("Item" + i));
    }
    ItemAdminPage adminPage = setupAdminPage();
    adminPage.exactQuery(context.getFullName(""));
    BulkSection bulk = adminPage.bulk();
    MoveCloneDialog moveDialog = bulk.selectAll().move();
    moveDialog.setTargetCollection(newCollection);
    if (transformName != null) {
      moveDialog.setTransformation(transformName);
    }
    assertTrue(moveDialog.executeBulk().waitAndFinish(adminPage));
    for (int i = 1; i <= NUM_ITEMS; i++) {
      String itemName = context.getFullName("Item" + i);
      ItemListPage results = adminPage.load().exactQuery(itemName, 1);
      assertFalse(results.doesResultExist(itemName, 2));
      SummaryPage summary = results.viewFromTitle(itemName, 1);
      assertTrue(isTextPresent(newCollection));
      verifyAttachment(summary, true);
    }
  }

  @SuppressWarnings("nls")
  @Test
  public void testBulkMoveWithXslt() {
    testBulk("Different Schema", "Standard Schema");
  }
}
