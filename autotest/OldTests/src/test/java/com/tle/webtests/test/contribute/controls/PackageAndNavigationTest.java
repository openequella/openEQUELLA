package com.tle.webtests.test.contribute.controls;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.ItemUrlPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.NavNodePageObject;
import com.tle.webtests.pageobject.wizard.controls.NavigationBuilder;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import java.text.MessageFormat;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class PackageAndNavigationTest extends AbstractCleanupAutoTest {
  private static final String QTI_ZIP = "realqti.zip";
  private static final String IMS_ZIP = "package.zip";
  private static final String SCORM_ZIP = "scorm.zip";
  private static final String NAME_QTI = "BBQs test package";
  private static final String NAME_PKG = "Zou ba! Visiting China: Is this your first visit?";
  private static final String NAME_PKG2 = "Arrays: word problems with products from 10 to 30";
  private static final String ERROR_NONCONTENT = "This file is not a recognised content package: ";
  private static final String ERROR_NONALLOWED =
      "This file is not one of the allowed content packages: ";

  private WaitingPageObject<UniversalControl> newPkg(UniversalControl control, String name) {
    return control.attachNameWaiter(name, false);
  }

  @Test
  public void packageOnly() {
    String itemName = context.getFullName("package");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(4);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));

    String googleZip = "google.zip";
    testBadPackageUpload(file, googleZip, ERROR_NONCONTENT);
    file.uploadPackage(Attachments.get("package2.zip"), newPkg(control, NAME_PKG2));

    String displayName = "EDITED display name";
    control.editResource(file.pkgEditor(), NAME_PKG2);
    file.pkgEditor().setDisplayName(displayName).save();

    SummaryPage item = wizard.save().publish();

    AttachmentsPage attachments = item.attachments();
    assertFalse(attachments.attachmentExists(googleZip));
    assertTrue(attachments.attachmentExists(displayName));

    item.edit();
    control.replaceSingleResource(file, displayName);
    file.uploadPackageReplace(Attachments.get(IMS_ZIP), newPkg(control, NAME_PKG));
    control.editResource(file.pkgEditor(), NAME_PKG).showStructure().save();
    item = wizard.saveNoConfirm();

    attachments = item.attachments();
    assertTrue(attachments.folderExists(NAME_PKG));
    PackageViewer viewer = attachments.viewFullscreen();
    assertTrue(viewer.selectedAttachmentContainsText("Visiting China: is this your first visit?"));
  }

  private void testBadPackageUpload(
      FileUniversalControlType file, String filename, String expectedError) {
    file.uploadError(
        Attachments.get(filename), MessageFormat.format("{0}{1}", expectedError, filename));
  }

  @Test
  public void qtiPackageOnly() {
    String itemName = context.getFullName("QTI package only");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(5);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));

    // Not a package
    testBadPackageUpload(file, "google.zip", ERROR_NONCONTENT);

    // IMS package
    testBadPackageUpload(file, IMS_ZIP, ERROR_NONALLOWED);

    // SCORM package
    testBadPackageUpload(file, SCORM_ZIP, ERROR_NONALLOWED);

    // QTI package
    file.uploadPackage(Attachments.get(QTI_ZIP), newPkg(control, NAME_QTI));

    wizard.save().publish();
  }

  @Test
  public void scormPackageOnly() {
    String itemName = context.getFullName("SCORM package only");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(6);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));

    // Not a package
    testBadPackageUpload(file, "google.zip", ERROR_NONCONTENT);

    // IMS package
    testBadPackageUpload(file, IMS_ZIP, ERROR_NONALLOWED);

    // QTI package
    testBadPackageUpload(file, QTI_ZIP, ERROR_NONALLOWED);

    // SCORM package
    file.uploadPackage(Attachments.get(SCORM_ZIP), newPkg(control, SCORM_ZIP));

    wizard.save().publish();
  }

  @Test
  public void allowedPackagesOnly() {
    String itemName = context.getFullName("QTI and SCORM package only");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(7);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));

    // Not a package
    testBadPackageUpload(file, "google.zip", ERROR_NONCONTENT);

    // IMS package
    testBadPackageUpload(file, IMS_ZIP, ERROR_NONALLOWED);

    // SCORM package
    file.uploadPackage(Attachments.get(SCORM_ZIP), newPkg(control, SCORM_ZIP));
    control.replaceSingleResource(file, SCORM_ZIP);

    // QTI package
    file.uploadPackageReplace(Attachments.get(QTI_ZIP), newPkg(control, NAME_QTI));
    control.editResource(file.pkgEditor(), NAME_QTI).setDisplayName(QTI_ZIP).save();
    Assert.assertTrue(control.hasResource(QTI_ZIP));
    control.deleteResource(QTI_ZIP);

    wizard.save().publish();
  }

  @Test
  public void zipOnly() {
    String itemName = context.getFullName("zip");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType file = control.addResource(new FileUniversalControlType(control));

    file.uploadZip(Attachments.get("google.zip")).selectAll().save();

    SummaryPage item = wizard.save().publish();

    wizard = item.edit();
    control.editResource(file.fileEditor(), "google.zip").select(1).save();
    wizard.saveNoConfirm();

    AttachmentsPage attachments = item.attachments();
    assertTrue(attachments.attachmentExists("google1.jpg"));
    assertFalse(attachments.attachmentExists("google2.jpg"));
  }

  @Test
  public void packageTest() {
    String itemName = context.getFullName("a package");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType file = control.addResource(new FileUniversalControlType(control));
    file.uploadPackageOption(Attachments.get(IMS_ZIP)).showStructure().save();
    wizard.next();
    assertTrue(wizard.navigation().nodeCount() == 2);

    SummaryPage item = wizard.save().publish();
    AttachmentsPage attachments = item.attachments();
    assertTrue(attachments.folderExists(NAME_PKG));

    // DTEC-14918: New version this item, remove the IMS package and ensure
    // that the nodes are removed from the nav builder.
    wizard = item.newVersion();
    wizard.universalControl(2).deleteResource(NAME_PKG);
    wizard.next();
    assertTrue(wizard.navigation().nodeCount() == 0);
    assertFalse(wizard.save().publish().hasAttachmentsSection());
  }

  @Test
  public void cancelPackageTest() {
    String itemName = context.getFullName("a canceled package");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType fileControl =
        control.addResource(new FileUniversalControlType(control));
    fileControl.uploadPackageOption(Attachments.get(IMS_ZIP)).backToStart().close();
    SummaryPage view = wizard.get().save().publish();
    assertFalse(view.hasAttachmentsSection());
    ItemUrlPage tilde = view.tilde();
    assertEquals(tilde.getFolderLink(IMS_ZIP), null);
    assertEquals(tilde.viewFolder("IMS").get().getFileLink(IMS_ZIP), null);
  }

  /**
   * DTEC-14847 - Ensure that if a new node is created, "Multiple resources" checked but no
   * resources selected for the tabs, then everything still keeps going without error.
   */
  @Test
  public void checkNoErrorForEmptyTabs() {
    String itemName = context.getFullName("check no error for empty tabs");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addFile(2, "page.html");
    wizard.next();

    NavigationBuilder navigation = wizard.navigation();
    NavNodePageObject newNode = navigation.addTopLevelNode("Empty Node", null);
    newNode.addTabWithoutResource("Another Tab");

    AttachmentsPage attachments = wizard.save().publish().attachments();
    assertTrue(attachments.attachmentExists("Empty Node"));
    PackageViewer pv = attachments.viewFullscreen();
    assertEquals(pv.tabText("", "Tab 1"), "");
    context.getDriver().navigate().back();
    attachments.get();
    attachments.viewFullscreen();
    assertEquals(pv.tabText("", "Another Tab"), "This is a verifiable attachment");
    // DTEC-14853
    assertEquals(pv.getTabOrder(), Lists.newArrayList("Tab 1", "Another Tab"));
  }

  /**
   * Redmine #5767 - Putting a double-quote in the name of a node cuts off everything after it. Try
   * doing lots of quote-y things and ensure they work as expected.
   */
  @Test
  public void quotesInNodeNames() {
    String itemName = context.getFullName("quote check");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addFile(2, "page.html");
    wizard.next();

    NavigationBuilder navigation = wizard.navigation();
    navigation.addTopLevelNode("1 double \" quote", "page.html");
    navigation.addTopLevelNode("2 double \" quotes\"", "page.html");
    navigation.addTopLevelNode("1 single ' quote", "page.html");
    navigation.addTopLevelNode("2 single ' quotes'", "page.html");

    AttachmentsPage attachments = wizard.save().publish().attachments();
    assertTrue(attachments.attachmentExists("1 double \" quote"));
    assertTrue(attachments.attachmentExists("2 double \" quotes\""));
    assertTrue(attachments.attachmentExists("1 single ' quote"));
    assertTrue(attachments.attachmentExists("2 single ' quotes'"));
  }

  @Test
  public void navigation() {
    String itemName = context.getFullName("navigation");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addFiles(
        2,
        false,
        Attachments.get("page.html"),
        Attachments.get("pageB.html"),
        Attachments.get("pageC.html"));
    wizard.next();

    NavigationBuilder navigation = wizard.navigation();
    navigation.initialiseNavigation(true);
    navigation.removeNode("page.html");
    navigation.addTopLevelNode("A new node", "page.html");
    NavNodePageObject tabNode = navigation.addChild("A new node", "Tab holder", "None");
    tabNode.deleteTab(0);
    tabNode.addTab("pageC.html", "Tab number 1");
    tabNode.addTab("pageB.html", "Tab number 2");
    navigation.setShowOtherAttachments(false);
    SummaryPage item = wizard.save().publish();

    AttachmentsPage attachments = item.attachments();
    assertFalse(attachments.attachmentExists("page.html"));
    assertTrue(attachments.attachmentExists("A new node"));

    PackageViewer viewer = attachments.viewFullscreen();
    viewer.clickAttachment("pageC.html");
    assertTrue(viewer.selectedAttachmentContainsText("This is a verifiable attachment 3"));
    assertEquals(viewer.tabText("Tab holder", "Tab number 2"), "This is a verifiable attachment 2");
    // DTEC-14853
    assertEquals(viewer.getTabOrder(), Lists.newArrayList("Tab number 1", "Tab number 2"));

    item = viewer.clickTitle();
    wizard = item.adminTab().edit();
    wizard.next();
    navigation = wizard.navigation();
    navigation.setSplitView(true);
    item = wizard.saveNoConfirm();
    viewer = item.attachments().viewFullscreen();

    viewer.clickAttachment("Tab holder");
    viewer.clickSplitView();

    assertEquals(
        viewer.tabText("Tab holder", "Tab number 1", 0), "This is a verifiable attachment 3");
    assertEquals(
        viewer.tabText("Tab holder", "Tab number 2", 1), "This is a verifiable attachment 2");
    // DTEC-14853
    assertEquals(viewer.getTabOrder(), Lists.newArrayList("Tab number 1", "Tab number 2"));

    item = viewer.clickTitle();
    assertTrue(
        item.attachments()
            .viewAttachment("A new node", new VerifyableAttachment(context))
            .isVerified());
  }

  @Test
  public void packageNavigation() {
    String itemName = context.getFullName("package navigation");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    control
        .addResource(new FileUniversalControlType(control))
        .uploadPackageOption(Attachments.get(IMS_ZIP))
        .showStructure()
        .save();
    SummaryPage item = wizard.save().publish();

    AttachmentsPage attachments = item.attachments();
    PackageViewer viewer = attachments.viewFullscreen();
    assertTrue(viewer.selectedAttachmentContainsText("Look at shots 1 and 2 from the Feature."));
  }

  @Test
  public void modifyingNavigation() {
    String itemName = context.getFullName("navigation modification");
    WizardPageTab wizard = initialItem(itemName);
    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType fileType = control.addResource(new FileUniversalControlType(control));
    fileType.uploadZip(Attachments.get("google.zip")).save();

    NavigationBuilder nav = wizard.next().navigation();
    nav.initialiseNavigation(true);
    wizard.prev();

    UniversalControl uniControl = wizard.universalControl(2);
    uniControl.deleteResource("google.zip"); // Deleting the zip removed the
    // files

    SummaryPage item = wizard.save().publish();
    assertFalse(item.hasAttachmentsSection());

    wizard = item.adminTab().edit();

    URL theFile = Attachments.get("page.html");
    wizard.addFiles(2, false, theFile, theFile, theFile, theFile, theFile);

    nav = wizard.next().navigation();
    nav.initialiseNavigation(true);
    wizard.prev();

    uniControl = wizard.universalControl(2);
    uniControl.deleteResource("page(5).html");
    uniControl.deleteResource("page(4).html");
    uniControl.deleteResource("page(3).html");
    uniControl.deleteResource("page(2).html");

    item = wizard.saveNoConfirm();
    AttachmentsPage attachments = item.attachments();
    assertEquals(attachments.attachmentCount(), 1);

    wizard = item.adminTab().edit();
    uniControl = wizard.universalControl(2);
    uniControl.deleteResource("page.html");
    wizard.addFiles(2, false, theFile, theFile, theFile, theFile);
    nav = wizard.next().navigation();
    nav.addTopLevelNode("Root 1", "page.html");
    nav.addTopLevelNode("Root 2", "page(2).html");
    nav.addChild("Root 1", "Child 1", "page(3).html");
    nav.addChild("Root 2", "Child 2", "page(4).html");

    item = wizard.saveNoConfirm();
    attachments = item.attachments();
    assertEquals(attachments.attachmentCount(), 4);
    assertTrue(attachments.attachmentExists("Root 1"));
    assertTrue(attachments.attachmentDetails("Root 1").contains("page.html"));
    assertTrue(attachments.attachmentExists("Child 1"));
    assertTrue(attachments.attachmentDetails("Child 1").contains("page(3).html"));

    assertTrue(attachments.attachmentExists("Root 2"));
    assertTrue(attachments.attachmentDetails("Root 2").contains("page(2).html"));
    assertTrue(attachments.attachmentExists("Child 2"));
    assertTrue(attachments.attachmentDetails("Child 2").contains("page(4).html"));

    wizard = item.adminTab().edit();
    nav = wizard.next().navigation();
    nav.setShowOtherAttachments(false);
    nav.removeNode("Root 2");
    item = wizard.saveNoConfirm();
    attachments = item.attachments();
    assertEquals(attachments.attachmentCount(), 2);
    assertTrue(attachments.attachmentExists("Root 1"));
    assertTrue(attachments.attachmentDetails("Root 1").contains("page.html"));
    assertTrue(attachments.attachmentExists("Child 1"));
    assertTrue(attachments.attachmentDetails("Child 1").contains("page(3).html"));
    assertFalse(attachments.attachmentExists("Root 2"));

    wizard = item.adminTab().edit();
    nav = wizard.next().navigation();
    nav.removeNode("Child 1");
    nav.addSibling("Root 1", "Root 2", "page.html");
    item = wizard.saveNoConfirm();
    attachments = item.attachments();
    assertEquals(attachments.attachmentCount(), 2);
    assertTrue(attachments.attachmentExists("Root 1"));
    assertTrue(attachments.attachmentDetails("Root 1").contains("page.html"));
    assertTrue(attachments.attachmentExists("Root 2"));
    assertTrue(attachments.attachmentDetails("Root 2").contains("page.html"));
    assertFalse(attachments.attachmentExists("Child 1"));

    wizard = item.adminTab().edit();
    nav = wizard.next().navigation();
    nav.removeNode("Root 2");
    item = wizard.saveNoConfirm();
    attachments = item.attachments();
    assertTrue(attachments.attachmentExists("Root 1"));
    assertTrue(attachments.attachmentDetails("Root 1").contains("page.html"));
    assertFalse(attachments.attachmentExists("Root 2"));
  }

  /** DTEC-14857 */
  @Test
  public void reordering() {
    String itemName = context.getFullName("reordering");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addFiles(
        2,
        false,
        Attachments.get("page.html"),
        Attachments.get("pageB.html"),
        Attachments.get("pageC.html"));
    wizard.next();

    NavigationBuilder navigation = wizard.navigation();
    navigation.addTopLevelNode("N1", "page.html");
    navigation.addTopLevelNode("N2", "pageB.html");
    navigation.addTopLevelNode("N3", "pageC.html");

    SummaryPage item = wizard.save().publish();
    assertEquals(item.attachments().attachmentOrder(), Lists.newArrayList("N1", "N2", "N3"));

    wizard = item.edit();
    wizard.next();

    navigation = wizard.navigation();
    navigation.moveUp("N2");
    navigation.moveDown("N1");

    item = wizard.saveNoConfirm();
    assertEquals(item.attachments().attachmentOrder(), Lists.newArrayList("N2", "N3", "N1"));

    // TODO: Dragging doesn't seem to work with WebDriver right now, or it
    // does but it's extremely finicky. The XPaths are verified to be
    // correct, but I feel that the mouse move that WebDriver is doing may
    // not be precise enough for the very small drop area.

    // wizard = item.edit();
    // wizard.next();

    // navigation = wizard.navigation();
    // navigation.dragToBefore("N3", "N2");
    // navigation.dragToBefore("N1", "N3");

    // item = wizard.saveNoConfirm();
    // assertEquals(item.attachments().attachmentOrder(),
    // Lists.newArrayList("N1", "N3", "N2"));
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, itemName);
    return wizard;
  }
}
