package com.tle.webtests.externalbroken;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.kaltura.KalturaServerEditor;
import com.tle.webtests.pageobject.kaltura.KalturaServerList;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.GenericAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.GoogleBooksUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.KalturaUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class KalturaContributionTest extends AbstractCleanupAutoTest {
  private static final String COLLECTION = "Navigation and Attachments";

  private static final String EP = "http://www.kaltura.com";
  private static final String PID = "794862";
  private static final String AS = "3ed91bc51898104f0facd07684ff2ccc";
  private static final String US = "541b9c75a8e366c09aeb8f83cb943897";

  @Test(groups = "first")
  public void addExisting() {
    final String query = "AutoTest";

    String itemName = context.getFullName("Kaltura existing single");
    WizardPageTab wizard = initialItem(itemName);

    String vidName = "Single Existing Video Test";
    UniversalControl control = wizard.universalControl(2);
    KalturaUniversalControlType kaltura =
        control.addResource(new KalturaUniversalControlType(control));
    kaltura.search(query).selectExistingVideo(1).setDisplayName(vidName).save();

    // Add
    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("Single Existing Video Test"));
    assertEquals(item.attachments().attachmentCount(), 1);
    // Add Multiple
    wizard = item.adminTab().edit();

    control = wizard.universalControl(2);
    kaltura = control.addResource(new KalturaUniversalControlType(control));

    kaltura.search(query).addExistingVideos(2, 3, 4);
    item = wizard.saveNoConfirm();
    assertEquals(item.attachments().attachmentCount(), 4);

    // Edit
    wizard = item.adminTab().edit();
    control = wizard.universalControl(2);
    GenericAttachmentEditPage editPage =
        control.editResource(new KalturaUniversalControlType(control), vidName);
    vidName = "A Kaltura Video";
    editPage.setDisplayName(vidName).save();
    item = wizard.saveNoConfirm();
    assertTrue(item.attachments().attachmentExists(vidName));

    // Replace
    wizard = item.adminTab().edit();
    control = wizard.universalControl(2);
    String uuid = control.getAttachmentUuid(vidName);
    GoogleBooksUniversalControlType book =
        control.replaceResource(new GoogleBooksUniversalControlType(control), vidName);

    String bookName = "A Book";
    book.search("google").selectBook(1).setDisplayName(bookName).save();

    assertEquals(control.getAttachmentUuid(bookName), uuid);

    // Remove
    String removeVid = "AutoTest-3";
    assertTrue(control.hasResource(removeVid));
    control.deleteResource(removeVid);
    assertTrue(!control.hasResource(removeVid));
    item = wizard.saveNoConfirm();
    assertTrue(!item.attachments().attachmentExists(removeVid));
  }

  @Test(dependsOnGroups = "first")
  public void disableExistingKalturaServer() {
    String kalturaSaaS = "Kaltura.com SaaS";

    // Disable
    KalturaServerList ksl = new KalturaServerList(context).load();
    ksl.enableServer(kalturaSaaS, false);

    // Contribute
    String itemName = context.getFullName("Kaltura existing single");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    assertTrue(control.addResource(new KalturaUniversalControlType(control)).isDisabled());

    // Enable
    ksl = new KalturaServerList(context).load();
    ksl.enableServer(kalturaSaaS, true);

    // Check failure
    KalturaServerEditor kse = ksl.editServer(kalturaSaaS);
    assertTrue(kse.isWaiting());
    kse.saveWithErrors();
    // Check for validation on test
    assertEquals(
        kse.getError(), "You must successfully test the Kaltura server details before saving.");
    kse.setEndpoint("");
    kse.test();
    assertEquals(kse.getError(), "You must enter an endpoint");
    kse.setEndpoint(EP);
    kse.setPartnerId("");
    kse.test();
    assertTrue(kse.isSuccessful());
    kse.setAdminSecret("");
    kse.test();
    assertEquals(kse.getError(), "You must enter an administrator secret");
    kse.setAdminSecret(AS);
    kse.setUserSecret("");
    kse.test();
    assertEquals(kse.getError(), "You must enter a user secret");
    kse.setUserSecret(US);
    kse.test();
    assertTrue(kse.isSuccessful());

    // Change details after test
    kse.setEndpoint(EP + "fail");
    kse.saveWithErrors();
    assertEquals(kse.getTestStatus(), "Server details do not match those tested. Please re-test");
    assertTrue(kse.isFailure());
    kse.setEndpoint(EP);

    // Save should fail
    kse.test();
    assertTrue(kse.isSuccessful());

    // Check save
    ksl = kse.saveSuccessful();

    ReceiptPage.waiter("Kaltura server saved successfully", ksl).get();
  }

  /** http://dev.equella.com/issues/7288 */
  @Test(groups = "first")
  public void testNoSelections() {
    logon("AutoTest", "automated");
    String itemName = context.getFullName("Kaltura - no selections");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    KalturaUniversalControlType kaltura =
        control.addResource(new KalturaUniversalControlType(control));
    kaltura.search("test");
    Assert.assertFalse(kaltura.canAdd(), "Able to add with no selections");

    UniversalControl close = kaltura.close();
    close.getPage().cancel(new ContributePage(context));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    String kalturaSaaS = "Kaltura.com SaaS";

    // enable
    KalturaServerList ksl = new KalturaServerList(context).load();
    if (!ksl.isEnabled(kalturaSaaS)) {
      ksl.enableServer(kalturaSaaS, true);
    }

    super.cleanupAfterClass();
  }

  public void addNew() {
    /*
     * TODO At this stage we cannot interact well enough with the flash
     * widget to test this properly
     */

    // final String query = "AutoTest";
    //
    // String itemName = context.getFullName("Kaltura new single");
    // WizardPageTab wizard = initialItem(itemName);
    //
    // String vidName = "Single New Video Test";
    // wizard.addNewKalturaVideo(2, "AutoTest-6", "autotest, even");
    //
    // // Add
    // SummaryTabPage item = wizard.save().publish();
    // assertTrue(item.attachments().attachmentExists("Single New Video Test"));
    // assertEquals(item.attachments().attachmentCount(), 1);
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1, itemName);
    return wizard;
  }
}
