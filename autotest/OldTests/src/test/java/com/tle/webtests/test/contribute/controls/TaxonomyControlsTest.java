package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Lists;
import com.tle.webtests.framework.SoapHelper;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.framework.soap.SoapService50;
import com.tle.webtests.framework.soap.TermSoapService;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.StringSelectedStuff;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AutoCompleteTermControl;
import com.tle.webtests.pageobject.wizard.controls.PopupTermControl;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.cxf.binding.soap.SoapFault;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class TaxonomyControlsTest extends AbstractCleanupTest {
  private static final String TAX_UUID = "d57a760c-514f-eda6-9f48-306117a31cda";
  private TermSoapService termSoapService;
  private SoapService50 soapService;
  private SoapHelper soapHelper;

  /**
   * DTEC-14591. Ensure that choosing to "add" a term when one hasn't been selected does <b>not</b>
   * add a blank selected term.
   */
  @Test(dependsOnMethods = "setupTaxonomy")
  public void addWithNoTermSelected() throws Exception {
    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Taxonomy Testing Collection");

    AutoCompleteTermControl autoComplete = wizardPage.autoTermControl(3);
    autoComplete.selectNothing();
    assertEquals(autoComplete.getSelections().getSelectionCount(), 0);
    autoComplete.selectExistingTerm("Animal", wizardPage);
    assertEquals(autoComplete.getSelections().getSelections(), Lists.newArrayList("Animal"));

    PopupTermControl popup = wizardPage.popupTermControl(4);
    wizardPage = popup.openDialog().finish(wizardPage);
    assertEquals(popup.getSelections().getSelectionCount(), 0);

    popup = wizardPage.popupTermControl(4);
    WaitingPageObject<StringSelectedStuff> selectWaiter = popup.selectWaiter("Animal");
    popup.openDialog().selectTerm("Animal").finish(selectWaiter);
    assertEquals(popup.getSelections().getSelections(), Lists.newArrayList("Animal"));
    wizardPage.cancel(new ContributePage(context));
  }

  /**
   * DTEC-14453. Double check that "Reload page" setting for the auto-complete edit box works as
   * expected.
   */
  @Test(dependsOnMethods = "setupTaxonomy")
  public void autoCompleteReloadPage() throws Exception {
    final String term1 = "Animal";
    final String term2 = "Car";
    final String vid1 = "termReloadTestValue1";
    final String vid2 = "termReloadTestValue2";

    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Taxonomy Testing Collection");

    assertFalse(isTextPresentInId(vid1, term1));
    assertFalse(isTextPresentInId(vid2, term2));

    wizardPage.autoTermControl(6).selectExistingTerm(term2, wizardPage.getUpdateWaiter(6), 1);

    assertFalse(isTextPresentInId(vid1, term1));
    assertTrue(isTextPresentInId(vid2, term2));

    wizardPage.autoTermControl(3).selectExistingTerm(term1, wizardPage);

    assertFalse(isTextPresentInId(vid1, term1));
    assertTrue(isTextPresentInId(vid2, term2));

    // Select another term in the second control to reload the page
    wizardPage.autoTermControl(6).removeTerm(term2);

    assertTrue(isTextPresentInId(vid1, term1));
    assertFalse(isTextPresentInId(vid2, term2));

    wizardPage.cancel(new ContributePage(context));
  }

  @Test(dependsOnMethods = "setupTaxonomy")
  public void autoCompleteTerms() throws Exception {
    String term1 = "New term";
    String term2 = "And this is another";

    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Taxonomy Testing Collection");
    wizardPage.editbox(1, context.getFullName("Adding terms test"));

    wizardPage.autoTermControl(3).addNewTerm(term1);
    ItemId itemId = wizardPage.save().publish().getItemId();
    isAutoTermOnItem(itemId, term1);

    contributePage = new ContributePage(context).load();
    wizardPage = contributePage.openWizard("Taxonomy Testing Collection");
    wizardPage.editbox(1, context.getFullName("Existing terms test"));

    AutoCompleteTermControl autoTermControl = wizardPage.autoTermControl(3);
    autoTermControl.selectExistingTerm("New", wizardPage, 1);
    autoTermControl.selectExistingTerm("Last", wizardPage);

    String term1Text = autoTermControl.getAddedTermByIndex(1);
    assertEquals(term1Text, term1);
    String term2Text = autoTermControl.getAddedTermByIndex(2);
    assertEquals(term2Text, "Last");

    autoTermControl.addNewTerm(term2);
    itemId = wizardPage.save().publish().getItemId();
    isAutoTermOnItem(itemId, term1, term2, "Last");
  }

  /** Covers the multiple-selection aspects of DTEC-15039. */
  @Test(dependsOnMethods = "setupTaxonomy")
  public void popUpTermSelector() throws Exception {
    String term1 = "Animal";
    String term2 = "This\\Has\\A\\Few\\Children\\Last";

    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Taxonomy Testing Collection");
    wizardPage.editbox(1, context.getFullName("Popup terms test"));
    wizardPage.popupTermControl(4).openDialog().selectTerm(term1).finish(wizardPage);
    wizardPage.popupTermControl(4).openDialog().selectTerm(term2).finish(wizardPage);
    ItemId itemId = wizardPage.save().publish().getItemId();
    isPopTermOnItem(itemId, term1, "Last");

    contributePage = new ContributePage(context).load();
    wizardPage = contributePage.openWizard("Taxonomy Testing Collection");
    wizardPage.editbox(1, context.getFullName("Popup terms search test"));
    wizardPage.popupTermControl(4).openDialog().search("Mineral", 2).finish(wizardPage);
    itemId = wizardPage.save().publish().getItemId();
    isPopTermOnItem(itemId, "Car");
  }

  /** DTEC-15040. Ensure that term selectors are doing the right thing in repeaters. */
  @Test(dependsOnMethods = "setupTaxonomy")
  public void repeaterTest() throws Exception {
    final String term1 = "Animal";
    final String term2 = "Mineral";
    final String term3 = "Vegetable";

    logon("AutoTest", "automated");
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard("Taxonomy Repeater Collection");
    wizardPage.editbox(1, context.getFullName("taxonomy repeater test"));

    RepeaterControl repeater = wizardPage.repeater(2);
    SubWizardPage first = repeater.getControls(2, 1);
    first.autoTermControl(1).selectExistingTerm(term1, wizardPage);
    PopupTermControl popupTermControl = first.popupTermControl(2);
    popupTermControl.openDialog().selectTerm(term1).finish(popupTermControl.selectWaiter(term1));

    SubWizardPage second = repeater.add(3, 2);
    second.autoTermControl(1).selectExistingTerm(term2, wizardPage);
    popupTermControl = second.popupTermControl(2);
    popupTermControl.openDialog().selectTerm(term2).finish(popupTermControl.selectWaiter(term2));

    assertEquals(
        first.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term1));
    assertEquals(
        first.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term1));
    assertEquals(
        second.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term2));
    assertEquals(
        second.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term2));

    SubWizardPage third = repeater.add(4, 3);
    third.autoTermControl(1).selectExistingTerm(term3, wizardPage);
    popupTermControl = third.popupTermControl(2);
    popupTermControl.openDialog().selectTerm(term3).finish(popupTermControl.selectWaiter(term3));

    repeater.remove(1);

    assertEquals(
        first.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term1));
    assertEquals(
        first.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term1));
    assertEquals(
        third.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term3));
    assertEquals(
        third.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term3));

    wizardPage.cancel(new ContributePage(context));
  }

  /**
   * Covered test cases:
   *
   * <ul>
   *   <li>Each of the above are tested for "moving left" and "moving right":
   *       <ul>
   *         <li>If target parent is null (root):
   *             <ul>
   *               <li>Index is zero
   *               <li>Index is negative or greater than the number of root terms minus one.
   *               <li>Index is any other valid number
   *             </ul>
   *         <li>Else target is a valid parent term:
   *             <ul>
   *               <li>Term has no existing children
   *               <li>Index is zero
   *               <li>Index is negative or greater than the number of root terms minus one.
   *               <li>Index is any other valid number
   *             </ul>
   *       </ul>
   *   <li>Moving a term to be an earlier sibling of any parent term
   *   <li>Moving a term to be a later sibling of any parent term
   * </ul>
   */
  @Test(dependsOnMethods = "setupTaxonomy")
  public void movingTerms() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    for (int i = 0; i < 5; i++) {
      termSoapService.insertTerm(TAX_UUID, "", "Move " + i, -1);
      for (int j = 0; j < 6; j++) {
        termSoapService.insertTerm(TAX_UUID, "Move " + i, "Move " + i + j, -1);
      }
    }

    termSoapService.insertTerm(TAX_UUID, "", "Move 5", -1);
    termSoapService.insertTerm(TAX_UUID, "Move 4\\Move 40", "Move 440", -1);

    // If target parent is null (root)
    // Index is zero
    termSoapService.move(TAX_UUID, "Move 0\\Move 00", "", 0);
    validate("Move 00", "", 0);

    // Index is negative or greater than the number of root terms minus one.
    termSoapService.move(TAX_UUID, "Move 0\\Move 01", "", -1);
    validate("Move 01", "", -1);

    termSoapService.move(
        TAX_UUID, "Move 0\\Move 02", "", termSoapService.listTerms(TAX_UUID, "").length);
    validate("Move 02", "", -1);

    int index = termSoapService.listTerms(TAX_UUID, "").length - 1;
    termSoapService.move(TAX_UUID, "Move 0\\Move 03", "", index);
    validate("Move 03", "", index);

    // Index is any other valid number
    termSoapService.move(TAX_UUID, "Move 0\\Move 04", "", 1);
    validate("Move 04", "", 1);
    termSoapService.move(TAX_UUID, "Move 0", "", 0);
    termSoapService.move(TAX_UUID, "Move 0\\Move 05", "", 1);
    validate("Move 05", "", 1);

    // Else target is a valid parent term:
    // Move has no existing children
    termSoapService.move(TAX_UUID, "Move 1\\Move 10", "Move 0", 0);
    validate("Move 10", "Move 0", 0);
    termSoapService.move(TAX_UUID, "Move 1\\Move 11", "Move 5", 0);
    validate("Move 11", "Move 5", 0);

    // Index is zero
    termSoapService.move(TAX_UUID, "Move 1\\Move 12", "Move 0", 0);
    validate("Move 12", "Move 0", 0);
    termSoapService.move(TAX_UUID, "Move 1\\Move 13", "Move 5", 0);
    validate("Move 13", "Move 5", 0);

    // Index is negative or greater than the number of root terms minus one.
    termSoapService.move(TAX_UUID, "Move 1\\Move 14", "Move 0", -1);
    validate("Move 14", "Move 0", -1);
    termSoapService.move(TAX_UUID, "Move 1\\Move 15", "Move 5", -1);
    validate("Move 15", "Move 5", -1);

    termSoapService.move(
        TAX_UUID,
        "Move 2\\Move 20",
        "Move 0",
        termSoapService.listTerms(TAX_UUID, "Move 0").length);
    validate("Move 20", "Move 0", -1);
    termSoapService.move(
        TAX_UUID,
        "Move 2\\Move 21",
        "Move 5",
        termSoapService.listTerms(TAX_UUID, "Move 0").length);
    validate("Move 21", "Move 5", -1);

    // Index is any other valid number
    index = termSoapService.listTerms(TAX_UUID, "Move 0").length - 1;
    termSoapService.move(TAX_UUID, "Move 2\\Move 22", "Move 0", index);
    validate("Move 22", "Move 0", index);
    index = termSoapService.listTerms(TAX_UUID, "Move 5").length - 1;
    termSoapService.move(TAX_UUID, "Move 2\\Move 23", "Move 5", index);
    validate("Move 23", "Move 5", index);

    termSoapService.move(TAX_UUID, "Move 2\\Move 24", "Move 0", 2);
    validate("Move 24", "Move 0", 2);
    termSoapService.move(TAX_UUID, "Move 2\\Move 25", "Move 5", 2);
    validate("Move 25", "Move 5", 2);

    // Moving a term to be a later sibling in the same parent. For example,
    // moving B to the right below:
    termSoapService.move(TAX_UUID, "Move 3\\Move 31", "Move 3", 2);
    validate("Move 31", "Move 3", 2);

    // For root
    index = termSoapService.listTerms(TAX_UUID, "").length - 2;
    termSoapService.move(TAX_UUID, "Move 01", "", index);
    validate("Move 01", "", index);

    // Moving a term to be a later sibling of any parent term. For example,
    // moving C to be a later sibling of its parent:
    termSoapService.move(TAX_UUID, "Move 4\\Move 40\\Move 440", "Move 4", 2);
    validate("Move 440", "Move 4", 2);

    // for root
    index = termSoapService.listTerms(TAX_UUID, "").length - 2;
    termSoapService.move(TAX_UUID, "Move 4\\Move 40", "", index);
    validate("Move 40", "", index);

    List<String> expectedRoot =
        Lists.newArrayList(
            "Move 0", "Move 05", "Move 00", "Move 04", "Move 1", "Move 2", "Move 3", "Move 4",
            "Move 5", "Move 03", "Move 40", "Move 01", "Move 02");
    validate(termSoapService.listTerms(TAX_UUID, ""), expectedRoot);

    List<String> expectedNode0 =
        Lists.newArrayList("Move 12", "Move 10", "Move 24", "Move 14", "Move 22", "Move 20");
    validate(termSoapService.listTerms(TAX_UUID, "Move 0"), expectedNode0);

    List<String> expectedNode3 =
        Lists.newArrayList("Move 30", "Move 32", "Move 31", "Move 33", "Move 34", "Move 35");
    validate(termSoapService.listTerms(TAX_UUID, "Move 3"), expectedNode3);

    List<String> expectedNode4 =
        Lists.newArrayList("Move 41", "Move 440", "Move 42", "Move 43", "Move 44", "Move 45");
    validate(termSoapService.listTerms(TAX_UUID, "Move 4"), expectedNode4);

    List<String> expectedNode5 =
        Lists.newArrayList("Move 13", "Move 11", "Move 25", "Move 15", "Move 23", "Move 21");
    validate(termSoapService.listTerms(TAX_UUID, "Move 5"), expectedNode5);

    termSoapService.unlockTaxonomy(TAX_UUID, true);
  }

  private void validate(String term, String parent, int index) {
    String[] listTerms = termSoapService.listTerms(TAX_UUID, parent);
    if (index == -1) {
      assertEquals(listTerms[listTerms.length - 1], term);
    } else {
      assertEquals(listTerms[index], term);
    }
  }

  private void validate(String listTerms[], Collection<String> expected) {
    ArrayList<String> terms = new ArrayList<String>();
    for (String term : listTerms) {
      if (term.startsWith("Move")) {
        terms.add(term);
      }
    }
    assertEquals(terms, expected);
  }

  @Test(dependsOnMethods = "setupTaxonomy")
  public void moveInsertTest() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    termSoapService.insertTerm(TAX_UUID, "", "1", -1);
    termSoapService.insertTerm(TAX_UUID, "", "2", -1);
    termSoapService.insertTerm(TAX_UUID, "", "3", -1);

    termSoapService.insertTerm(TAX_UUID, "3", "1", -1);
    termSoapService.insertTerm(TAX_UUID, "3", "2", -1);
    termSoapService.insertTerm(TAX_UUID, "3", "4", -1);
    termSoapService.insertTerm(TAX_UUID, "3", "5", -1);

    termSoapService.insertTerm(TAX_UUID, "3", "0", 0);
    termSoapService.insertTerm(TAX_UUID, "3", "3", 3);
    termSoapService.insertTerm(TAX_UUID, "3", "6", -1);

    String[] terms = termSoapService.listTerms(TAX_UUID, "3");
    assertEquals(terms.length, 7);

    for (int i = 0; i < 7; i++) {
      assertEquals(terms[i], String.valueOf(i), "Terms are not in the correct order");
    }

    termSoapService.insertTerm(TAX_UUID, "1", "0", -1);
    termSoapService.insertTerm(TAX_UUID, "1", "3", -1);
    termSoapService.insertTerm(TAX_UUID, "1", "6", -1);

    termSoapService.insertTerm(TAX_UUID, "2", "1", -1);
    termSoapService.insertTerm(TAX_UUID, "2", "2", -1);
    termSoapService.insertTerm(TAX_UUID, "2", "4", -1);
    termSoapService.insertTerm(TAX_UUID, "2", "5", -1);

    termSoapService.move(TAX_UUID, "1\\0", "2", 0);
    termSoapService.move(TAX_UUID, "1\\3", "2", 3);
    termSoapService.move(TAX_UUID, "1\\6", "2", -1);

    assertEquals(
        termSoapService.listTerms(TAX_UUID, "1").length, 0, "This node should have no children");
    terms = termSoapService.listTerms(TAX_UUID, "2");
    assertEquals(terms.length, 7);

    for (int i = 0; i < 7; i++) {
      assertEquals(terms[i], String.valueOf(i), "Terms are not in the correct order");
    }

    termSoapService.unlockTaxonomy(TAX_UUID, true);
  }

  @Test(dependsOnMethods = "setupTaxonomy")
  public void renameTerms() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    termSoapService.insertTerm(TAX_UUID, "", "a", -1);
    termSoapService.insertTerm(TAX_UUID, "a", "b", -1);
    termSoapService.insertTerm(TAX_UUID, "a\\b", "c", -1);

    assertEquals(termSoapService.listTerms(TAX_UUID, "a\\b")[0], "c");
    termSoapService.renameTermValue(TAX_UUID, "a\\b\\c", "d");
    assertEquals(termSoapService.listTerms(TAX_UUID, "a\\b")[0], "d");

    termSoapService.renameTermValue(TAX_UUID, "a\\b", "ba");
    termSoapService.renameTermValue(TAX_UUID, "a", "ab");

    assertEquals(termSoapService.listTerms(TAX_UUID, "ab\\ba")[0], "d");
    assertEquals(termSoapService.listTerms(TAX_UUID, "ab")[0], "ba");

    termSoapService.unlockTaxonomy(TAX_UUID, true);
  }

  @Test(dependsOnMethods = "setupTaxonomy")
  public void lockAndUnlock() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);
    soapHelper.clearCookies(soapService);
    soapHelper.clearCookies(termSoapService);
    soapService.login("AutoTest", "automated");
    soapHelper.copyCookies(soapService, termSoapService);

    String message = "";

    try {
      termSoapService.lockTaxonomyForEditing(TAX_UUID);
    } catch (SoapFault ex) {
      message = ex.getReason();
    } finally {
      assertEquals(
          message,
          "Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter"
              + " value of true.");
    }

    message = "";
    try {
      termSoapService.unlockTaxonomy(TAX_UUID, false);
    } catch (SoapFault ex) {
      message = ex.getReason();
    } finally {
      assertEquals(
          message,
          "Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter"
              + " value of true.");
    }

    soapService.login("NoSearchCreateUser", "``````");
    soapHelper.copyCookies(soapService, termSoapService);

    message = "";
    try {
      termSoapService.lockTaxonomyForEditing(TAX_UUID);
    } catch (SoapFault ex) {
      message = ex.getReason();
    } finally {
      assertEquals(
          message, "Taxonomy is locked by another user: adfcaf58-241b-4eca-9740-6a26d1c3dd58");
    }

    message = "";
    try {
      termSoapService.unlockTaxonomy(TAX_UUID, false);
    } catch (SoapFault ex) {
      message = ex.getReason();
    } finally {
      assertEquals(
          message,
          "You do not own the lock on this taxonomy.  It is held by user ID"
              + " adfcaf58-241b-4eca-9740-6a26d1c3dd58");
    }

    login();
  }

  @Test(dependsOnMethods = "setupTaxonomy")
  public void taxonomySoapTest() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    termSoapService.insertTerm(TAX_UUID, "", "Root", -1);
    for (int i = 0; i < 10; i++) {
      termSoapService.insertTerm(TAX_UUID, "Root", "Term " + i, -1);
      for (int j = 0; j < 10; j++) {
        termSoapService.insertTerm(TAX_UUID, "Root\\Term " + i, "Term " + i + j, -1);
      }
    }

    termSoapService.unlockTaxonomy(TAX_UUID, true);

    String[] terms = termSoapService.listTerms(TAX_UUID, "Root");
    assertEquals(terms.length, 10);
    for (int i = 0; i < 10; i++) {
      assertEquals(terms[i], "Term " + i);
    }

    for (int i = 0; i < 10; i++) {
      termSoapService.setData(TAX_UUID, "Root\\Term " + i, "data1", String.valueOf(i));
    }

    for (int i = 0; i < 10; i++) {
      assertEquals(
          termSoapService.getData(TAX_UUID, "Root\\Term " + i, "data1"), String.valueOf(i));
    }
  }

  private void login() throws Exception {
    soapService.login("AutoTest", "automated");
    soapHelper.copyCookies(soapService, termSoapService);
    termSoapService.unlockTaxonomy(TAX_UUID, true);
  }

  private void isTermOnItem(ItemId itemId, String node, String... terms) throws Exception {
    soapService.login("AutoTest", "automated");
    soapHelper.copyCookies(soapService, termSoapService);
    PropBagEx xml = new PropBagEx(soapService.getItem(itemId.getUuid(), itemId.getVersion(), null));
    List<String> termList = xml.getNodeList(node);
    for (int i = 0; i < terms.length; i++) {
      assertTrue(
          termList.contains(terms[i]),
          "Looking for " + terms[i] + " but only found " + termList.toString());
    }
  }

  private void isAutoTermOnItem(ItemId itemId, String... terms) throws Exception {
    isTermOnItem(itemId, "item/controls/taxonomy/auto", terms);
  }

  private void isPopTermOnItem(ItemId itemId, String... terms) throws Exception {
    isTermOnItem(itemId, "item/controls/taxonomy/popup", terms);
  }

  @Test
  public void setupTaxonomy() throws Exception {
    soapHelper = new SoapHelper(context);
    termSoapService =
        soapHelper.createSoap(
            TermSoapService.class,
            "services/taxonomyTerm.service",
            "http://taxonomy.core.tle.com",
            null);
    soapService =
        soapHelper.createSoap(
            SoapService50.class,
            "services/SoapService50",
            "http://soap.remoting.web.tle.com",
            null);

    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    termSoapService.insertTerm(TAX_UUID, "", "Animal", -1);
    termSoapService.insertTerm(TAX_UUID, "", "Vegetable", -1);
    termSoapService.insertTerm(TAX_UUID, "", "Mineral", -1);

    termSoapService.insertTerm(TAX_UUID, "Animal", "Dog", -1);
    termSoapService.insertTerm(TAX_UUID, "Animal", "Cat", -1);
    termSoapService.insertTerm(TAX_UUID, "Animal", "Mouse", -1);

    termSoapService.insertTerm(TAX_UUID, "Vegetable", "Tree", -1);
    termSoapService.insertTerm(TAX_UUID, "Vegetable", "Carrot", -1);

    termSoapService.insertTerm(TAX_UUID, "Mineral", "Mountain", -1);
    termSoapService.insertTerm(TAX_UUID, "Mineral", "Stone", -1);
    termSoapService.insertTerm(TAX_UUID, "Mineral", "Table", -1);
    termSoapService.insertTerm(TAX_UUID, "Mineral", "Car", -1);

    termSoapService.insertTerm(TAX_UUID, "", "This", -1);
    termSoapService.insertTerm(TAX_UUID, "This", "Has", -1);
    termSoapService.insertTerm(TAX_UUID, "This\\Has", "A", -1);
    termSoapService.insertTerm(TAX_UUID, "This\\Has\\A", "Few", -1);
    termSoapService.insertTerm(TAX_UUID, "This\\Has\\A\\Few", "Children", -1);
    termSoapService.insertTerm(TAX_UUID, "This\\Has\\A\\Few\\Children", "Last", -1);

    termSoapService.unlockTaxonomy(TAX_UUID, true);

    setDeleteCredentials("AutoTest", "automated");
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    login();
    termSoapService.lockTaxonomyForEditing(TAX_UUID);

    String[] terms = termSoapService.listTerms(TAX_UUID, "");
    for (int i = 0; i < terms.length; i++) {
      termSoapService.deleteTerm(TAX_UUID, terms[i]);
    }

    termSoapService.unlockTaxonomy(TAX_UUID, true);
    super.cleanupAfterClass();
  }
}
