package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.CalendarControl;
import com.tle.webtests.pageobject.wizard.controls.GroupControl;
import java.util.Arrays;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class GroupsAndDisabling extends AbstractWizardControlsTest {
  private static final String ITEM_NAME = "Group Item";
  private static final String COLLECTION_NAME =
      "Controls collection (groups, disabling, visibility)";
  private ItemId itemId;

  @Test
  public void contribute() {
    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_NAME));
    GroupControl group = wizardPage.group(2, 2);
    SubWizardPage groupItem1 = group.getGroupItem(0, 0);
    assertTrue(groupItem1.isEditboxDisabled(1));
    CalendarControl cals = groupItem1.calendar(2);
    assertTrue(cals.isRangeDisabled());
    assertTrue(groupItem1.shuffleList(3).isDisabled());
    assertTrue(groupItem1.shuffleGroup(5, 3).isDisabled());
    SubWizardPage groupItem2 = group.getGroupItem(2, 7);
    assertTrue(groupItem2.isDropDownDisabled(1));
    assertTrue(groupItem2.repeater(3).isDisabled());
    group.toggleGroup("group1");
    cals = groupItem1.calendar(2);
    assertFalse(cals.isRangeDisabled());
    assertFalse(groupItem1.isEditboxDisabled(1));
    assertFalse(groupItem1.shuffleList(3).isDisabled());
    assertFalse(groupItem1.shuffleGroup(5, 3).isDisabled());
    groupItem1.editbox(1, "Edit Box");
    group.toggleGroup("group2");
    groupItem2.selectDropDown(1, "1");
    groupItem2.setCheck(2, "2", true);
    group.toggleGroup("group2");

    wizardPage.next();
    wizardPage.prev();
    wizardPage.next();
    wizardPage.editbox(2, "Keep");
    wizardPage.editbox(3, "Lose");
    wizardPage.setCheck(4, "1", true);
    wizardPage.setCheck(5, "2", true);
    wizardPage.setCheckReload(1, "true", true);
    wizardPage.setCheckReload(1, "true", false);
    wizardPage.setCheckReload(1, "true", true);
    wizardPage.next();
    wizardPage.setCheck(1, "1", true);
    wizardPage.setCheck(1, "2", true);
    wizardPage.setCheck(2, "5", true);
    wizardPage.setCheck(2, "6", true);
    wizardPage.prev();
    wizardPage.next();

    itemId = wizardPage.save().publish().getItemId();
  }

  @Test(dependsOnMethods = "contribute")
  public void validateItem() throws Exception {
    soap.login("AutoTest", "automated");
    PropBagEx itemXml = new PropBagEx(soap.getItem(itemId.getUuid(), itemId.getVersion(), null));
    assertEquals(itemXml, "item/same/checkboxes", Arrays.asList("1", "2", "5", "6"));
    PropBagEx cXml = itemXml.getSubtree("item/controls");
    assertEquals(cXml, "group", Arrays.asList("group1"));
    assertEquals(cXml, "editbox", "Edit Box");
    assertFalse(cXml.nodeExists("checkboxes"));
    assertFalse(cXml.nodeExists("listbox"));
    PropBagEx kXml = itemXml.getSubtree("item/keep");
    assertEquals(kXml, "editbox", "Keep");
    assertEquals(kXml, "checkboxes", Arrays.asList("1"));
    PropBagEx lXml = itemXml.getSubtree("item/lose");
    assertFalse(lXml.nodeExists("editbox"));
    assertFalse(lXml.nodeExists("checkboxes"));
  }
}
