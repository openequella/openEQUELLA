package com.tle.webtests.test.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.common.legacyio.FileUtils;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileAttachmentEditPage;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.pageobject.wizard.controls.universal.WebPagesUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;
import io.github.openequella.pages.search.NewSearchPage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class FileAttachmentControlTest extends AbstractCleanupAutoTest {
  @Test
  public void webPages() {
    String itemName = context.getFullName("Web Pages");
    WizardPageTab wizard = initialItem(itemName);

    // Add but cancel
    UniversalControl control = wizard.universalControl(2);
    WebPagesUniversalControlType pagesType =
        control.addResource(new WebPagesUniversalControlType(control));
    pagesType.openPage("Cancel this", "Who cares?", false).close();
    assertFalse(control.hasResource("Cancel this"));
    control.addResource(pagesType).addPage("Test page", "This is a verifiable attachment");

    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("Test page"));
    assertFalse(item.attachments().attachmentExists("Cancel this"));
    assertTrue(
        item.attachments()
            .viewAttachment("Test page", new VerifyableAttachment(context))
            .isVerified());

    String scrapbookItem = context.getFullName("An authored page");

    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .authorWebPage(scrapbookItem, "A Page", "This is a verifiable attachment");

    if (!testConfig.isNewUI()) {
      myResourcesPage.resetFilters();
    }
    assertTrue(myResourcesPage.isScrapbookCreated(scrapbookItem));

    itemName = context.getFullName("Web Page from scrapbook");
    wizard = initialItem(itemName);
    control.addResource(pagesType).importPageFromScrapbook(scrapbookItem, "A Page");

    item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("A Page"));
    assertTrue(
        item.attachments()
            .viewAttachment("A Page", new VerifyableAttachment(context))
            .isVerified());

    itemName = context.getFullName("Web Pages 2");
    wizard = initialItem(itemName);

    control.addResource(pagesType).addPage("Test page 2", "A heading");

    String uuid = control.getAttachmentUuid("Test page 2");

    WebPagesUniversalControlType pages =
        control.replaceResource(new WebPagesUniversalControlType(control), "Test page 2");

    pages.openPage("New page", "some content", true).replace("New page");

    assertEquals(control.getAttachmentUuid("New page"), uuid);
    item = wizard.save().publish();
    assertTrue(item.itemXml().nodeHasValue("/item/attachments/attachment/uuid", uuid));
  }

  @Test
  public void multipleControls() {
    String itemName = context.getFullName("Multiple controls");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl universalControl = wizard.universalControl(2);
    universalControl
        .addResource(new WebPagesUniversalControlType(universalControl))
        .addPage("Test page", "A heading");
    wizard.addSingleFile(3, Attachments.get("page2.html"));

    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("Test page"));
    assertTrue(item.attachments().attachmentExists("page2.html"));
    wizard = item.edit();
    wizard.universalControl(2).deleteResource("Test page");

    item = wizard.saveNoConfirm();
    assertFalse(item.attachments().attachmentExists("Test page"));
    assertTrue(item.attachments().attachmentExists("page2.html"));

    wizard = item.edit();
    wizard.universalControl(3).deleteResource("page2.html");
    item = wizard.saveNoConfirm();
    assertFalse(item.hasAttachmentsSection());
  }

  @Test
  public void specialCharacters() {
    String FILE1 = "Special characters - 'test1.jpg";
    String FILE2 = "Special characters - хцч test2.jpg";
    String FILE3 = "Special characters - script %test3+.zip";
    String FILE3_NEW = "Special characters - <script> %test3+.zip";
    String FILE4 = "Special characters - #& test4.html";

    String rename = "<script>alert('fail')</script>";

    String itemName = context.getFullName("Special characters");
    WizardPageTab wizard = initialItem(itemName);

    wizard.addFile(2, FILE1);
    wizard.addFile(2, FILE2);
    wizard.addFile(2, FILE4);

    UniversalControl control = wizard.universalControl(2);
    FileAttachmentEditPage file =
        control.editResource(new FileUniversalControlType(control), FILE1);
    file.setDisplayName(rename).save();

    file = control.editResource(new FileUniversalControlType(control), rename);
    file.setDisplayName(FILE1).save();

    file = control.editResource(new FileUniversalControlType(control), FILE1);
    file.setDisplayName(rename).save();

    SummaryPage item = wizard.save().publish();

    assertTrue(item.attachments().attachmentExists(rename));
    assertTrue(item.attachments().attachmentExists(FILE2));
    assertTrue(item.attachments().attachmentExists(FILE4));
    assertTrue(
        item.attachments().viewAttachment(FILE4, new VerifyableAttachment(context)).isVerified());

    context.getFullName("Special characters 2");
    wizard = initialItem(itemName);

    File renamedFile = getRenamedFile(FILE3, FILE3_NEW);
    String uploaded;
    control = wizard.universalControl(2);
    FileUniversalControlType fileType = control.addResource(new FileUniversalControlType(control));
    if (renamedFile.exists()) {
      fileType.uploadZip(renamedFile).save();
      uploaded = renamedFile.getName();
    } else {
      fileType.uploadZip(Attachments.get(FILE3)).save();
      uploaded = FILE3;
    }
    item = wizard.save().publish();

    assertTrue(item.attachments().attachmentExists(FILE1));
    assertTrue(item.attachments().attachmentExists(FILE2));
    assertTrue(item.attachments().attachmentExists(uploaded));
    assertTrue(item.attachments().attachmentExists(FILE4));
    assertTrue(
        item.attachments().viewAttachment(FILE4, new VerifyableAttachment(context)).isVerified());
  }

  private File getRenamedFile(String fileName, String newName) {
    File file = new File(AbstractPage.getPathFromUrl(Attachments.get(fileName)));
    File newFile = new File(FileUtils.getTempDirectory(), newName);
    try {
      FileUtils.copyFile(file, newFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return newFile;
  }

  @Test
  public void file() {
    String itemName = context.getFullName("file");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addFile(2, "page.html");
    UniversalControl control = wizard.universalControl(2);
    FileAttachmentEditPage file =
        control.editResource(new FileUniversalControlType(control), "page.html");
    file.setDisplayName("page2.html").save();

    SummaryPage item = wizard.save().publish();
    assertTrue(item.attachments().attachmentExists("page2.html"));
    assertTrue(
        item.attachments()
            .viewAttachment("page2.html", new VerifyableAttachment(context))
            .isVerified());
  }

  @Test
  public void sameFile() {
    String scrapbookItem = "page.html";
    String itemName = context.getFullName("file");
    WizardPageTab wizard = initialItem(itemName);

    URL theFile = Attachments.get("page.html");
    wizard.addFiles(2, false, theFile, theFile, theFile, theFile);

    AttachmentsPage attachments = wizard.save().publish().attachments();
    assertTrue(attachments.attachmentExists("page.html"));
    assertTrue(attachments.attachmentExists("page(2).html"));
    assertTrue(attachments.attachmentExists("page(3).html"));
    assertTrue(attachments.attachmentExists("page(4).html"));

    assertTrue(
        attachments.viewAttachment("page(4).html", new VerifyableAttachment(context)).isVerified());

    new MyResourcesPage(context, "scrapbook").load().uploadFile(theFile, scrapbookItem, "");
    if (testConfig.isNewUI()) {
      NewSearchPage searchPage = new NewSearchPage(context).load();
      searchPage.changeQuery(itemName);
      searchPage.waitForSearchCompleted(1);
      searchPage.selectItem(itemName).adminTab().edit();
    } else {
      wizard = SearchPage.searchExact(context, itemName).viewFromTitle(itemName).adminTab().edit();
    }

    UniversalControl control = wizard.universalControl(2);
    FileAttachmentEditPage file =
        control.editResource(new FileUniversalControlType(control), "page.html");
    file.setDisplayName("page(1).html").save();

    FileUniversalControlType fileType = control.addResource(new FileUniversalControlType(control));
    fileType.importFromScrapbook(scrapbookItem);

    control.editResource(fileType, "page.html").setDisplayName("page(5).html").save();

    fileType = control.addResource(fileType);
    fileType.importFromScrapbook(scrapbookItem);

    control.editResource(fileType, "page.html").setDisplayName("page(6).html").save();

    attachments = wizard.saveNoConfirm().attachments();
    for (int i = 1; i < 7; i++) {
      assertTrue(attachments.attachmentExists("page(" + i + ").html"));
    }
    assertTrue(
        attachments.viewAttachment("page(6).html", new VerifyableAttachment(context)).isVerified());
  }

  @Test
  public void zipping() {
    String itemName = context.getFullName("file");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType fileType = control.addResource(new FileUniversalControlType(control));
    fileType.uploadZip(Attachments.get("google.zip")).save();

    SummaryPage item = wizard.save().publish();

    assertTrue(item.attachments().attachmentExists("google.zip"));
    assertTrue(item.attachments().attachmentExists("google1.jpg"));
    assertTrue(item.attachments().attachmentExists("google2.jpg"));

    wizard = item.adminTab().edit();
    FileAttachmentEditPage file = control.editResource(fileType.fileEditor(), "google.zip");
    file.setAttachZip(false).save(true);
    item = wizard.saveNoConfirm();
    assertFalse(item.attachments().attachmentExists("google.zip"));
    assertTrue(item.attachments().attachmentExists("google1.jpg"));
    assertTrue(item.attachments().attachmentExists("google2.jpg"));

    itemName = context.getFullName("unzip");
    wizard = initialItem(itemName);

    FileUniversalControlType unzip = control.addResource(fileType);
    unzip.uploadFile(Attachments.get("google.zip"));

    control = wizard.universalControl(3);
    FileUniversalControlType noZip =
        control.addDefaultResource(new FileUniversalControlType(control));
    noZip.uploadFile(Attachments.get("google.zip"), "google(2).zip");
  }

  @Test
  public void zipWithFolders() {
    String itemName = context.getFullName("zip");
    WizardPageTab wizard = initialItem(itemName);

    UniversalControl control = wizard.universalControl(2);
    FileUniversalControlType unzip = control.addResource(new FileUniversalControlType(control));

    unzip.uploadAndGoBack(Attachments.get("google2.zip")).close();
    SummaryPage view = wizard.save().publish();
    assertFalse(view.hasAttachmentsSection());

    wizard = view.edit();
    control = wizard.universalControl(2);
    control.addResource(unzip).uploadZip(Attachments.get("google2.zip")).save();
    view = wizard.saveNoConfirm();
    AttachmentsPage attachments = view.attachments();

    assertTrue(attachments.attachmentExists("google2.zip"));
    for (int i = 1; i < 6; i++) {
      String file = "google" + i + ".jpg";
      assertTrue(attachments.attachmentExists(file), "File doesn't exist: " + file);
    }
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard =
        new ContributePage(context).load().openWizard("Navigation and Attachments");
    wizard.editbox(1, itemName);
    return wizard;
  }
}
