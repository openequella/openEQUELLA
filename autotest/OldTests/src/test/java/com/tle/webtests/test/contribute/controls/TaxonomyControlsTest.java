package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.common.PathUtils;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.StringSelectedStuff;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.AutoCompleteTermControl;
import com.tle.webtests.pageobject.wizard.controls.PopupTermControl;
import com.tle.webtests.pageobject.wizard.controls.RepeaterControl;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class TaxonomyControlsTest extends AbstractWizardControlsTest {
  private static final String TAX_UUID = "d57a760c-514f-eda6-9f48-306117a31cda";

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
    Assert.assertEquals(autoComplete.getSelections().getSelectionCount(), 0);
    autoComplete.selectExistingTerm("Animal", wizardPage);
    Assert.assertEquals(autoComplete.getSelections().getSelections(), Lists.newArrayList("Animal"));

    PopupTermControl popup = wizardPage.popupTermControl(4);
    wizardPage = popup.openDialog().finish(wizardPage);
    Assert.assertEquals(popup.getSelections().getSelectionCount(), 0);

    popup = wizardPage.popupTermControl(4);
    WaitingPageObject<StringSelectedStuff> selectWaiter = popup.selectWaiter("Animal");
    popup.openDialog().selectTerm("Animal").finish(selectWaiter);
    Assert.assertEquals(popup.getSelections().getSelections(), Lists.newArrayList("Animal"));
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
    Assert.assertEquals(term1Text, term1);
    String term2Text = autoTermControl.getAddedTermByIndex(2);
    Assert.assertEquals(term2Text, "Last");

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

    Assert.assertEquals(
        first.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term1));
    Assert.assertEquals(
        first.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term1));
    Assert.assertEquals(
        second.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term2));
    Assert.assertEquals(
        second.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term2));

    SubWizardPage third = repeater.add(4, 3);
    third.autoTermControl(1).selectExistingTerm(term3, wizardPage);
    popupTermControl = third.popupTermControl(2);
    popupTermControl.openDialog().selectTerm(term3).finish(popupTermControl.selectWaiter(term3));

    repeater.remove(1);

    Assert.assertEquals(
        first.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term1));
    Assert.assertEquals(
        first.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term1));
    Assert.assertEquals(
        third.autoTermControl(1).getSelections().getSelections(), Lists.newArrayList(term3));
    Assert.assertEquals(
        third.popupTermControl(2).getSelections().getSelections(), Lists.newArrayList(term3));

    wizardPage.cancel(new ContributePage(context));
  }

  private void isTermOnItem(ItemId itemId, String node, String... terms) throws Exception {
    PropBagEx xml = getItemXml(itemId);
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
    lock(TAX_UUID);
    String animalUuid = createTerm(TAX_UUID, null, "Animal", null);
    String vegetableUuid = createTerm(TAX_UUID, null, "Vegetable", null);
    String mineralUuid = createTerm(TAX_UUID, null, "Mineral", null);

    createTerm(TAX_UUID, null, "Dog", animalUuid);
    createTerm(TAX_UUID, null, "Cat", animalUuid);
    createTerm(TAX_UUID, null, "Mouse", animalUuid);

    createTerm(TAX_UUID, null, "Tree", vegetableUuid);
    createTerm(TAX_UUID, null, "Carrot", vegetableUuid);

    createTerm(TAX_UUID, null, "Mountain", mineralUuid);
    createTerm(TAX_UUID, null, "Stone", mineralUuid);
    createTerm(TAX_UUID, null, "Table", mineralUuid);
    createTerm(TAX_UUID, null, "Car", mineralUuid);

    String thisUuid = createTerm(TAX_UUID, null, "This", null);
    String hasUuid = createTerm(TAX_UUID, null, "Has", thisUuid);
    String aUuid = createTerm(TAX_UUID, null, "A", hasUuid);
    String fewUuid = createTerm(TAX_UUID, null, "Few", aUuid);
    String childrenUuid = createTerm(TAX_UUID, null, "Children", fewUuid);
    createTerm(TAX_UUID, null, "Last", childrenUuid);

    unlock(TAX_UUID);
    setDeleteCredentials("AutoTest", "automated");
  }

  private String createTerm(
      String taxonomyUuid, @Nullable String termUuid, String term, @Nullable String parentTermUuid)
      throws IOException {
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), "api/taxonomy", taxonomyUuid, "term");
    final ObjectNode jsonObj = mapper.createObjectNode();
    jsonObj.put("uuid", termUuid);
    jsonObj.put("term", term);
    jsonObj.put("parentUuid", parentTermUuid);

    final String jsonStr = jsonObj.toString();
    final HttpResponse response = postEntity(jsonStr, uri, getToken(), false);
    assertResponse(response, 201, "failed to create term");
    final String location = response.getFirstHeader("Location").getValue();
    return location.substring(location.lastIndexOf('/') + 1);
  }

  private void unlock(String taxonomyUuid) throws IOException {
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), "api/taxonomy", taxonomyUuid, "lock");
    HttpResponse response = deleteResource(uri, getToken(), "force", true);
    assertResponse(response, 204, "failed to unlock taxonomy");
  }

  private void lock(String taxonomyUuid) throws IOException, URISyntaxException {
    final String uri =
        PathUtils.urlPath(context.getBaseUrl(), "api/taxonomy", taxonomyUuid, "lock");
    final HttpPost request = new HttpPost(new URI(uri));
    HttpResponse response = execute(request, true, getToken());
    assertResponse(response, 201, "failed to lock taxonomy");
  }
}
