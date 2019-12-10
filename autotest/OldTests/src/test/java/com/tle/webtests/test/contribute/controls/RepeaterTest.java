package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.WizardUrlPage;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class RepeaterTest extends AbstractWizardControlsTest {
  private static final String ITEM_NAME = "Repeater Item";
  private static final String COLLECTION_NAME = "Repeater Collection";
  private ItemId itemId;

  @Test
  public void contribute() {
    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_NAME));
    wizardPage.setCheckAppear(2, "true", true, 3);

    RepeaterControl repeater = wizardPage.repeater(3);
    Assert.assertTrue(repeater.getAddNoun().equalsIgnoreCase("Thing"));
    repeater.remove(1);
    repeater.remove(0);
    assertTrue(repeater.isShowingMinError(), "Should be an min error message");
    SubWizardPage firstEntry = repeater.add(6, 13);
    SubWizardPage secondEntry = repeater.add(8, 18);
    assertFalse(repeater.isShowingMinError(), "Should be no error message");
    firstEntry.editbox(1, "First");
    secondEntry.selectDropDown(2, "1");
    RepeaterControl subRepeater1 = firstEntry.repeater(3);
    SubWizardPage subEntry1 = subRepeater1.getControls(7, 16);
    subEntry1.editbox(1, "SubRepeater");
    repeater.add(0, 0);
    repeater.add(0, 0);
    repeater.add(0, 0);
    assertTrue(repeater.isAddDisabled());
    repeater.remove(2);
    repeater.remove(2);
    repeater.remove(2);
    assertFalse(repeater.isShowingAnyError(), "Should be no error message on repeater themselves");
    assertFalse(
        subRepeater1.isShowingAnyError(), "Should be no error message on repeater themselves");
    RepeaterControl subRepeater2 = secondEntry.repeater(3);
    SubWizardPage subEntry2 = subRepeater2.getControls(9, 21);
    subEntry2.editbox(1, "SubRepeater2 - Before Edit");

    itemId = wizardPage.save().publish().getItemId();
    wizardPage = new WizardUrlPage(context, itemId).edit();
    repeater = wizardPage.repeater(3);
    secondEntry = repeater.getControls(4, 8);
    subRepeater2 = secondEntry.repeater(3);
    subEntry2 = subRepeater2.getControls(5, 11);
    subEntry2.editbox(1, "SubRepeater2");
    wizardPage.saveNoConfirm();
  }

  @Test(dependsOnMethods = "contribute")
  public void validateItem() throws Exception {
    soap.login("AutoTest", "automated");
    PropBagEx itemXml = new PropBagEx(soap.getItem(itemId.getUuid(), itemId.getVersion(), null));
    PropBagEx cXml = itemXml.getSubtree("item/controls");
    assertEquals(cXml, "repeater[0]/editbox", "First");
    assertEquals(cXml, "repeater[1]/editbox", "DefaultValue");
    assertEquals(cXml, "repeater[0]/listbox", "2");
    assertEquals(cXml, "repeater[1]/listbox", "1");
    assertEquals(cXml, "repeater[0]/subrepeater[0]/editbox", "SubRepeater");
    assertEquals(cXml, "repeater[1]/subrepeater[0]/editbox", "SubRepeater2");
  }
}
