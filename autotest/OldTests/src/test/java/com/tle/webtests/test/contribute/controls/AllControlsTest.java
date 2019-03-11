package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.CalendarControl;
import com.tle.webtests.pageobject.wizard.controls.EmailSelectorControl;
import com.tle.webtests.pageobject.wizard.controls.SelectUserControl;
import com.tle.webtests.pageobject.wizard.controls.ShuffleGroupControl;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

@TestInstitution("fiveo")
public class AllControlsTest extends AbstractWizardControlsTest {
  private static final TimeZone USERS_TIMEZONE = TimeZone.getTimeZone("America/Chicago");
  private ItemId itemId;

  @Test
  public void contribute() {
    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Controls collection");
    wizardPage.editbox(1, context.getFullName("Simple Control Test"));

    wizardPage.setCheckReload(2, "true", true);
    wizardPage.editbox(3, "EditBox");
    Assert.assertEquals(wizardPage.getSelectedValueDropDown(4), "2");
    wizardPage.selectDropDown(4, "One");
    wizardPage.selectShuffle(5, "One");
    wizardPage.selectShuffle(5, "Two");
    wizardPage.setCheck(6, "2", true);
    wizardPage.setCheck(6, "3", false);
    wizardPage.setCheck(7, "1", true);

    ShuffleListControl shuffleList = wizardPage.shuffleList(8);
    shuffleList.add("Delete me");
    shuffleList.remove("Delete me");
    shuffleList.add("ShuffleList");

    MultiLingualEditbox multiEditbox = wizardPage.multiEditbox(10);
    multiEditbox.setSelectedLanguage("Afar");
    multiEditbox.setCurrentString("Afar");
    multiEditbox.allMode();
    multiEditbox.editLangString("English", "English modify");
    multiEditbox.singleMode();
    multiEditbox.setSelectedLanguage("English");
    multiEditbox.setCurrentString("English");

    wizardPage.next();
    wizardPage.prev();

    wizardPage.next();
    wizardPage.setCheckAppear(1, "true", true, 2);

    Calendar calNow = Calendar.getInstance(USERS_TIMEZONE);
    wizardPage.calendar(2).setDate(calNow);
    CalendarControl range = wizardPage.calendar(3);
    Calendar cal1970 = Calendar.getInstance(USERS_TIMEZONE);
    cal1970.set(1970, 0, 1);
    range.setDateRange(cal1970, calNow);
    // DTEC 14415
    assertFalse(wizardPage.hasControl(7));
    CalendarControl reload = wizardPage.calendar(6);
    WaitingPageObject<WizardPageTab> waiterAppear = wizardPage.getAppearWaiter(7);
    reload.setDateWithReload(Calendar.getInstance());
    waiterAppear.get();
    assertTrue(isTextPresentInId("rawhtml", "The date has been set and the controls reloaded"));

    waiterAppear = wizardPage.getDisappearWaiter(7);
    reload.clearDateRemove();
    wizardPage = waiterAppear.get();
    assertFalse(wizardPage.hasControl(7));

    wizardPage.next();
    wizardPage.setCheckAppear(1, "true", true, 2);

    SelectUserControl selectUser = wizardPage.selectUser(2);
    SelectUserDialog dialog = selectUser.openDialog();
    dialog.search("Selectable");
    dialog.select("Selectable1");
    dialog.select("Selectable2");
    dialog.finish(selectUser.selectedWaiter("Selectable1"));

    selectUser.removeUser("Selectable2");
    dialog = selectUser.openDialog();
    dialog.search("Selectable");
    dialog.select("Selectable2");
    dialog.finish(selectUser.selectedWaiter("Selectable2"));

    selectUser = wizardPage.selectUser(3);
    dialog = selectUser.openDialog();
    dialog.search("Selectable");
    assertFalse(dialog.containsUsername("Selectable2"));
    dialog.select("Selectable1");
    dialog.finish(selectUser.selectedWaiter("Selectable1"));

    wizardPage.next();
    wizardPage.setCheckAppear(1, "true", true, 2);

    EmailSelectorControl emailSelector = wizardPage.emailSelector(2);
    emailSelector.addEmail("jolse.maginnis@equella.com");
    emailSelector.removeEmail("jolse.maginnis@equella.com");
    emailSelector.addEmail("jolse.maginnis", true);
    assertTrue(emailSelector.isInvalidEmail());
    emailSelector.addEmail("jolse.maginnis@equella.com");

    SelectUserDialog emailDialog = emailSelector.openDialog();
    emailDialog.search("Selectable");
    emailDialog.select("Selectable1");
    emailDialog.finish(emailSelector.selectedWaiter("select1@test.com"));

    emailSelector = wizardPage.emailSelector(3);
    emailSelector.addEmail("aaron.holland@equella.com");
    assertFalse(emailSelector.isAddAvailable());
    wizardPage.next();

    wizardPage.setCheckAppear(1, "true", true, 2);

    ShuffleGroupControl shuffleGroup = wizardPage.shuffleGroup(2, 2);
    SubWizardPage shuffleCtrls = shuffleGroup.add();

    // DTEC 15011
    shuffleCtrls.editbox(1, "Blank");
    shuffleGroup.ok();
    // Assert list box is blank and not "Please select one"
    assertTrue(shuffleGroup.hasValue("Blank /"));
    shuffleGroup.remove("Blank /");
    shuffleCtrls = shuffleGroup.add();
    shuffleCtrls.editbox(1, "ShuffleEditBox");
    shuffleCtrls.selectDropDown(2, "1");
    shuffleGroup.ok();
    shuffleCtrls = shuffleGroup.add();
    shuffleCtrls.editbox(1, "ShuffleEditBox2");
    shuffleCtrls.selectDropDown(2, "2");
    shuffleGroup.ok();
    shuffleCtrls = shuffleGroup.add();
    shuffleCtrls.editbox(1, "ShuffleEditBox3");
    shuffleCtrls.selectDropDown(2, "2");
    shuffleGroup.ok();
    shuffleGroup.remove("ShuffleEditBox2 / 2");

    itemId = wizardPage.save().publish().getItemId();
  }

  @Test(dependsOnMethods = "contribute")
  public void validateItem() throws Exception {
    soap.login("AutoTest", "automated");
    PropBagEx itemXml = new PropBagEx(soap.getItem(itemId.getUuid(), itemId.getVersion(), null));
    PropBagEx cXml = itemXml.getSubtree("item/controls");
    assertEquals(cXml, "editbox", "EditBox");
    assertEquals(cXml, "listbox", "1");
    assertEquals(cXml, "shufflelist", Arrays.asList("ShuffleList"));
    assertEquals(cXml, "shufflebox", Arrays.asList("1", "2"));
    assertEquals(cXml, "checkboxes", Arrays.asList("2"));
    assertEquals(cXml, "radiogroup", Arrays.asList("1"));
    Calendar today = Calendar.getInstance(USERS_TIMEZONE);
    String todayStr =
        String.format(
            "%4d-%02d-%02d",
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH));

    assertEquals(cXml, "calendar/today", todayStr);
    assertEquals(cXml, "calendar/birthday", "1980-04-22");
    assertEquals(cXml, "calendar/nodefault", todayStr);
    assertEquals(cXml, "calendar/range", Arrays.asList("1970-01-01", todayStr));

    assertEquals(
        cXml,
        "userselector/allusers",
        Arrays.asList(
            "6285301a-4e6e-e925-aa17-7d6435678f46", "0dc35aad-cc0f-f470-ceac-810b36f3fd56"));
    assertEquals(cXml, "userselector/group", Arrays.asList("6285301a-4e6e-e925-aa17-7d6435678f46"));

    assertEquals(
        cXml,
        "emailselector/multiple",
        Arrays.asList("jolse.maginnis@equella.com", "select1@test.com"));
    assertEquals(cXml, "emailselector/single", Arrays.asList("aaron.holland@equella.com"));

    assertEquals(cXml, "shufflegroup[0]/editbox", "ShuffleEditBox");
    assertEquals(cXml, "shufflegroup[1]/editbox", "ShuffleEditBox3");
    assertEquals(cXml, "shufflegroup[0]/listbox", "1");
    assertEquals(cXml, "shufflegroup[1]/listbox", "2");
    PropBagEx multiXml = cXml.getSubtree("multilanguage");
    XPath xpath = XPathFactory.newInstance().newXPath();
    Element multiElem = multiXml.getRootElement();
    checkExists(xpath, "//string[@language='aa' and text()='Afar']", multiElem);
    checkExists(xpath, "//string[@language='en' and text()='English']", multiElem);
  }
}
