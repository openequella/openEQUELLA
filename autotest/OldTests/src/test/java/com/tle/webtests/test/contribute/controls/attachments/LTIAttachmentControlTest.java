package com.tle.webtests.test.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.LTIAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.LTIUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class LTIAttachmentControlTest extends AbstractCleanupAutoTest {
  // a preconfigured collection to which LTI attachment's can be added.
  private static final String COLLECTION_NAME = "ExternalTool (LTI) Collection";

  // After name & description, comes the 3rd control, Add a new (or view
  // existing) resource link
  private static final int RSRC_LINK = 3;

  private static final String rsrcTitle = "this is an attachment name";
  // We'll avoid this call because it includes the method name and we want
  // to pass names across methods
  // String itemTitle = context.getFullName(itemTitle);
  private static final String itemTitle =
      LTIAttachmentControlTest.class.getSimpleName() + " - Plain LTI";

  @Test
  public void testAddSimpleExternalTool() {
    WizardPageTab wizard = initialItem(itemTitle);

    UniversalControl control = wizard.universalControl(RSRC_LINK);
    LTIUniversalControlType ltiType = control.addResource(new LTIUniversalControlType(control));
    UniversalControl outOfControl = ltiType.addPage(1, null, "Automatic, based on URL", rsrcTitle);
    // Assert something?
    assertTrue(
        outOfControl.hasResource(rsrcTitle), "\"" + rsrcTitle + "\" not seen in attachments");
    WizardPageTab wpg = (WizardPageTab) outOfControl.getPage();
    SummaryPage finis = wpg.save().publish();
    // double check the obvious
    assertNotNull(finis.getItemTitle(), "Item has unexpected null title");
    assertTrue(
        finis.getItemTitle().endsWith(itemTitle),
        "Expected title to end with \"" + itemTitle + "\"");
  }

  /**
   * Presupposes success of earlier test to that item named <i>itemTile</i> can be found via search
   * and reloaded
   */
  @Test(dependsOnMethods = {"testAddSimpleExternalTool"})
  public void testEditOfExisting() {
    WizardPageTab wiz = SearchPage.searchAndView(context, itemTitle).edit();
    UniversalControl underControl = new UniversalControl(context, RSRC_LINK, wiz);
    LTIAttachmentEditPage editPage =
        underControl.editResource(new LTIAttachmentEditPage(underControl), rsrcTitle);

    String attachmentNewName = "new attachment name";
    editPage.enterValues(
        attachmentNewName,
        -1,
        "http://and-did-those-feet.in-ancient.ws/walk/upon/England%2Cs?mountains=green");
    editPage.enterAdvancedValues(
        null, null, "mykey=myvalue", Boolean.FALSE, Boolean.TRUE, Boolean.FALSE);
    underControl = editPage.save();

    SummaryPage sumpa = wiz.saveNoConfirm();
    AttachmentsPage attachmentsPage = sumpa.attachments();
    Boolean resourceExists = attachmentsPage.attachmentExists(attachmentNewName);
    assertTrue(resourceExists, "\"" + attachmentNewName + "\" not seen in attachments");
  }

  @Test
  public void testAddAndEdit() {
    final String addEditTitle = context.getFullName(", added & edited");
    WizardPageTab wizard = initialItem(addEditTitle);

    UniversalControl control = wizard.universalControl(RSRC_LINK);
    LTIUniversalControlType ltiType = control.addResource(new LTIUniversalControlType(control));
    UniversalControl outOfControl = ltiType.addPage(1, null, "Automatic, based on URL", rsrcTitle);
    LTIAttachmentEditPage ltiEdit =
        outOfControl.editResource(new LTIAttachmentEditPage(outOfControl), rsrcTitle);
    ltiEdit.enterValues(
        "attachment URL to Larry's place",
        -1,
        "http://purple-excess.dyndns.ws:8075/~larry/index.html");
    ltiEdit.enterAdvancedValues(
        "doesn't matter that its just a simple URL",
        "doesn't matter if it's not valid",
        null,
        Boolean.TRUE,
        null,
        null);
    ltiEdit.save();
    SummaryPage finis = wizard.save().publish();
    // double check the obvious
    assertNotNull(finis.getItemTitle(), "Item has unexpected null title");
    assertEquals(
        finis.getItemTitle(), addEditTitle, "Expected title to end with \"" + itemTitle + "\"");
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION_NAME);
    wizard.editbox(1, itemName);
    return wizard;
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
    ItemAdminPage filterListPage = new ItemAdminPage(context).load();
    ItemListPage filterResults = filterListPage.all().exactQuery(this.getClass().getSimpleName());
    if (filterResults.isResultsAvailable()) {
      filterListPage.bulk().deleteAll();
      filterListPage.get().search(this.getClass().getSimpleName());
      filterListPage.bulk().purgeAll();
    }
    super.cleanupAfterClass();
  }
}
