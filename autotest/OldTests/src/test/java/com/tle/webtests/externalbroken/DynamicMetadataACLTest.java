package com.tle.webtests.externalbroken;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.component.SelectGroupDialog;
import com.tle.webtests.pageobject.generic.component.SelectRoleDialog;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.portal.MenuSection;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.SelectGroupControl;
import com.tle.webtests.pageobject.wizard.controls.SelectRoleControl;
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("acl")
public class DynamicMetadataACLTest extends AbstractCleanupAutoTest {
  private static final String NO_ACL_ITEM_NAME = "NoACLtest";
  private static final String ACL_ITEM_NAME = "ACLtest";
  private static final String COLLECTION = "Dynamic Metadata ACLs";

  @Test
  public void contributeDynamicACLItem() {
    // Contribute item with users, groups and roles selected
    String itemName = context.getFullName(ACL_ITEM_NAME);
    WizardPageTab wizard = initialItem(itemName);

    // Select 2 users: Automated Testing + Andrew Gibb
    SelectUserControl userControl = wizard.selectUser(2);
    SelectUserDialog userDialog = userControl.openDialog();
    userDialog.search("A*");
    userDialog.select("andrew.gibb");
    userDialog.select("AutoTest");
    userDialog.finish(userControl.selectedWaiter("andrew.gibb"));
    userControl.removeUser("AutoTest");

    // Select 2 groups: cool and uncool
    SelectGroupControl groupControl = wizard.selectGroup(4);
    SelectGroupDialog groupDialog = groupControl.openDialog();
    groupDialog.search("cool");
    groupDialog.select("uncool");
    groupDialog.select("cool");
    groupDialog.finish(groupControl.selectedWaiter("uncool"));
    groupControl.removeGroup("cool");

    // Select 2 roles: herp and derp
    SelectRoleControl roleControl = wizard.selectRole(6);
    SelectRoleDialog roleDialog = roleControl.openDialog();
    roleDialog.search("role");
    roleDialog.select("role1");
    roleDialog.select("role2");
    roleDialog.finish(roleControl.selectedWaiter("role1"));
    roleControl.removeRole("role2");

    // Save
    SummaryPage item = wizard.save().publish();
    assertEquals(itemName, item.getItemTitle());

    item.commentsSection().addComment("My first comment", 3);
    assertTrue(item.commentsSection().hasComments());

    // Contribute second item with no restrictions
    itemName = context.getFullName(NO_ACL_ITEM_NAME);
    wizard = initialItem(itemName);
    wizard.save().publish();
    assertEquals(item.getItemTitle(), itemName);
    item.commentsSection().addComment("My second comment", 1);
    assertTrue(item.commentsSection().containsComment("My second comment"));
  }

  @Test(dependsOnMethods = "contributeDynamicACLItem")
  public void userBasedPermissions() {
    // Login as user
    logon("andrew.gibb", "Tle160cst");
    SearchPage sp = new SearchPage(context).load();
    SummaryPage summary = sp.search(ACL_ITEM_NAME).getResults().get(0).viewSummary();

    // Check permissions have applied to only that item for that user
    assertFalse(summary.hasCommentSection("list"));
    assertFalse(summary.hasCommentSection("add"));

    // Check other item in same collection has comments section
    sp = new SearchPage(context).load();
    summary = sp.search(NO_ACL_ITEM_NAME).getResults().get(0).viewSummary();
    assertTrue(summary.hasCommentSection("list"));
    assertTrue(summary.hasCommentSection("add"));
    assertTrue(summary.commentsSection().containsComment("My second comment"));

    // logout
    logout();

    // login as different user and check permissions do not apply
    logon("AutoTest", "automated");
    sp = new SearchPage(context).load();
    summary = sp.search(ACL_ITEM_NAME).getResults().get(0).viewSummary();
    assertTrue(summary.hasCommentSection("add"));
    assertTrue(summary.hasCommentSection("list"));
    assertTrue(summary.commentsSection().containsComment("My first comment"));
  }

  @Test(dependsOnMethods = "contributeDynamicACLItem")
  public void groupBasedPermissions() {
    String action = "Delete this version";

    // Check permission has applied for group members
    logon("nicholas.read", "Tle160cst");
    assertFalse(checkAction(ACL_ITEM_NAME, action));
    assertTrue(checkAction(NO_ACL_ITEM_NAME, action));

    logon("aaron.holland", "Tle160cst");
    assertFalse(checkAction(ACL_ITEM_NAME, action));
    assertTrue(checkAction(NO_ACL_ITEM_NAME, action));

    // login as different user and check permissions do not apply
    logon("andrew.gibb", "Tle160cst");
    assertTrue(checkAction(ACL_ITEM_NAME, action));
  }

  @Test(dependsOnMethods = "contributeDynamicACLItem")
  public void roleBasedPermissions() {
    String action = "Clone item into a collection";

    // Check permission has applied for role members (groups and users)
    logon("aaron.holland", "Tle160cst");
    assertFalse(checkAction(ACL_ITEM_NAME, action));
    assertTrue(checkAction(NO_ACL_ITEM_NAME, action));

    logon("int.demoteacher", "equella");
    assertFalse(checkAction(ACL_ITEM_NAME, action));
    assertTrue(checkAction(NO_ACL_ITEM_NAME, action));

    logon("int.demostudent", "equella");
    assertFalse(checkAction(ACL_ITEM_NAME, action));
    assertTrue(checkAction(NO_ACL_ITEM_NAME, action));

    // login as different user and check permissions do not apply
    logon("nicholas.read", "Tle160cst");
    assertTrue(checkAction(ACL_ITEM_NAME, action));
  }

  private boolean checkAction(String item, String action) {
    // Login as user
    SearchPage sp = new SearchPage(context).load();
    SummaryPage summary = sp.search(item).getResults().get(0).viewSummary();

    // Check permissions have applied to only that item for that user
    return summary.hasAction(action);
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard = new MenuSection(context).get().clickContribute(COLLECTION);
    wizard.editbox(1, itemName);
    return wizard;
  }
}
