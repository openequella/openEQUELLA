package com.tle.webtests.test.myresources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.myresources.MyResourcesUploadFilesPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.Test;

@TestInstitution("myresources")
public class MyResourcesTest extends AbstractCleanupTest {

  static final String FILE_NAME = "page.html";

  // Indexes are offset (ie: 0-based)
  static final int SEARCH_TAGS_INDEX = 1;
  static final int PAGE_LINKS_INDEX = 1;

  @Test
  public void testAddMyResource() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("A description");
    uploadAndVerify(scrapbookItem);

    WizardPageTab wizard =
        new ContributePage(context).load().openWizard(GENERIC_TESTING_COLLECTION);

    String itemName = context.getFullName("Import from scrapbook");
    wizard.editbox(1, itemName);

    UniversalControl control = wizard.universalControl(4);
    control
        .addDefaultResource(new FileUniversalControlType(control))
        .importFromScrapbook(scrapbookItem);
    wizard.save().publish();

    ItemListPage itemList = new ItemAdminPage(context).load().exactQuery(itemName);

    assertTrue(itemList.doesResultExist(itemName));
    AttachmentsPage attachments = itemList.getResultForTitle(itemName).viewSummary().attachments();
    assertTrue(attachments.attachmentExists(scrapbookItem));
    assertTrue(
        attachments.viewAttachment(scrapbookItem, new VerifyableAttachment(context)).isVerified());
  }

  @Test
  public void testAddAndDeleteFileUploadItem() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("Another description");
    MyResourcesPage myResourcesPage = uploadAndVerify(scrapbookItem);
    deleteAndVerifyItem(myResourcesPage, scrapbookItem);
  }

  private void deleteAndVerifyItem(MyResourcesPage myResourcesPage, String itemName) {
    myResourcesPage.deleteScrapbook(itemName);
    assertTrue(myResourcesPage.isScrapbookDeleted(itemName));
  }

  @Test
  public void testDndFile() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    MyResourcesPage myResourcesPage = new MyResourcesPage(context, "scrapbook").load();
    MyResourcesUploadFilesPage uploadPage = myResourcesPage.getUploadPage();
    assertTrue(uploadPage.hasArchiveOption("Upload the archive only"));
    assertTrue(uploadPage.hasArchiveOption("Extract files and keep the archive"));
    assertTrue(uploadPage.hasArchiveOption("Extract files, discard the archive"));
    assertTrue(uploadPage.hasDndTagField());
    assertTrue(uploadPage.hasDndDropZone());
  }

  /** test case 14958. */
  @Test
  public void testAddAndEditFileUploadItem() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("Another description");
    MyResourcesPage myResourcesPage = uploadAndVerify(scrapbookItem);
    String originalSearchTags = myResourcesPage.getScrapbookTags(scrapbookItem);

    // Edit the page but abandon changes.
    MyResourcesUploadFilesPage editWizard = myResourcesPage.editFile(scrapbookItem);
    editWizard.setDescription(
        "And did those feet in ancient time walk upon England's mountains green?");
    editWizard.editSearchTags("Blake Mattress");
    myResourcesPage = editWizard.cancel();

    // Tags should not be changed.
    assertEquals(myResourcesPage.getScrapbookTags(scrapbookItem), originalSearchTags);

    // Edit tags again and save the changes.
    editWizard = myResourcesPage.editFile(scrapbookItem);
    // Prefixing the new description (ie: the item name) will ensure the
    // renamed item will be included in post-test cleanup.
    String descStr =
        context.getFullName("And was the holy lamb of god on England's pleasant pastures seen?");
    editWizard.setDescription(descStr);
    String newTags = "Python Monty";
    editWizard.editSearchTags(newTags);
    editWizard.save();

    myResourcesPage.exactQuery(descStr);
    assertEquals(myResourcesPage.getScrapbookTags(descStr), newTags);
  }

  /**
   * convenience local method to upload an item into scrapbook
   *
   * @param scrapbookItem
   * @return results page
   */
  private MyResourcesPage uploadAndVerify(String scrapbookItem) {
    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .uploadFile(Attachments.get(FILE_NAME), scrapbookItem, "page");

    myResourcesPage.exactQuery(scrapbookItem);

    assertTrue(myResourcesPage.isScrapbookCreated(scrapbookItem));

    return myResourcesPage;
  }
}
