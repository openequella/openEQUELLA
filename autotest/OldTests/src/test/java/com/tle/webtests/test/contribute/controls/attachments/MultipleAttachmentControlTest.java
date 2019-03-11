package com.tle.webtests.test.contribute.controls.attachments;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class MultipleAttachmentControlTest extends AbstractCleanupTest {
  private static final String COLLECTION = "Attachment Control";
  private static final String URL_1 = "http://www.google.com";
  private static final String FILE_1 = "page.html";
  private static final String URL_3 = "http://pearson.com.au";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void multipleAttachmentSections() {
    String itemName = context.getFullName("multiple attachment control test");
    WizardPageTab wizard = initialItem(itemName);
    wizard.addUrl(2, URL_1);
    wizard.addFile(3, FILE_1);
    wizard.addUrl(3, URL_3);
    SummaryPage itemSummary = wizard.save().publish();
    assertTrue(itemSummary.attachments().existInFirstAttachmentList(URL_1));
    assertTrue(itemSummary.attachments().existInFirstAttachmentList(FILE_1));
    assertTrue(itemSummary.attachments().existInFirstAttachmentList(URL_3));
    assertTrue(itemSummary.attachments().existInSecondAttachmentList(URL_1));
    assertTrue(itemSummary.attachments().existInThirdAttachmentsList(FILE_1));
    assertTrue(itemSummary.attachments().existInThirdAttachmentsList(URL_3));

    PackageViewer packageViewer = itemSummary.attachments().chooseFullscreenToView("3");
    assertFalse(packageViewer.hasAttachmentNode(URL_1));
    assertTrue(packageViewer.hasAttachmentNode(FILE_1));
    assertTrue(packageViewer.hasAttachmentNode(URL_3));
  }

  private WizardPageTab initialItem(String itemName) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1, itemName);
    return wizard;
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("AutoTest", "automated");
    ItemAdminPage filterListPage = new ItemAdminPage(context).load();
    ItemListPage filterResults =
        filterListPage.all().exactQuery("multiple attachment control test");
    if (filterResults.isResultsAvailable()) {
      filterListPage.bulk().deleteAll();
      filterListPage.get().search("multiple attachment control test");
      filterListPage.bulk().purgeAll();
    }
    super.cleanupAfterClass();
  }
}
