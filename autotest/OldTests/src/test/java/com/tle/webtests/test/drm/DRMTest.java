package com.tle.webtests.test.drm;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.LocalWebDriver;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.DownloadFilePage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.LinkPage;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.DRMAgreementDialogPage;
import com.tle.webtests.pageobject.viewitem.DRMAgreementPage;
import com.tle.webtests.pageobject.viewitem.ImageGalleryPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
@LocalWebDriver
public class DRMTest extends AbstractSessionTest {
  @DataProvider(parallel = false)
  public Object[][] items() {
    // @formatter:off
    return new Object[][] {
      // {"DRM Allowing Composition - Package", true, false},
      //	{"DRM Require Composition Acceptance - Package", false, false},
      {"DRM Show On Summary and Allowing Composition - Package", true, true}
      // ,
      //	{"DRM Show On Summary and Require Composition Acceptance - Package", false, true}
    };
    // @formatter:on
  }

  @Test(dataProvider = "items")
  public void drmWithNoSummaryAcceptance(String name, boolean allowComp, boolean summary) {
    logon("AutoTest", "automated");
    SummaryPage item =
        new SearchPage(context)
            .load()
            .setSort("rank")
            .exactQuery("Link to " + name)
            .getResult(1)
            .viewSummary();
    // Remove when #1160 is fixed. Currently a bug exists that prevents
    // viewFullScreen in the new UI.
    if (!item.usingNewUI()) {
      PackageViewer pack =
          item.attachments().viewFullscreen(getAgree()).preview(new PackageViewer(context));
      testItem(pack, name, allowComp);
      logon("AutoTest", "automated");

      item =
          new SearchPage(context)
              .load()
              .setSort("rank")
              .exactQuery("Link to " + name)
              .getResult(1)
              .viewSummary();
      pack = item.attachments().viewFullscreen(getAgree()).preview(pack);
      testItem2(pack, name, allowComp);
    }
  }

  @Test(dataProvider = "items")
  public void drmWithSummaryAcceptance(String name, boolean allowComp, boolean summary) {
    logon("AutoTest", "automated");

    SummaryPage item =
        new SearchPage(context)
            .load()
            .setSort("rank")
            .exactQuery("Summary DRM - Link to " + name)
            .getResult(1)
            .viewSummary(getAgree())
            .preview(new SummaryPage(context));
    // Remove when #1160 is fixed. Currently a bug exists that prevents
    // viewFullScreen in the new UI.
    if (!item.usingNewUI()) {
      PackageViewer pack = item.attachments().viewFullscreen();

      testItem(pack, name, allowComp);

      logon("AutoTest", "automated");
      item =
          new SearchPage(context)
              .load()
              .setSort("rank")
              .exactQuery("Summary DRM - Link to " + name)
              .getResult(1)
              .viewSummary(getAgree())
              .preview(item);
      pack = item.attachments().viewFullscreen();

      testItem2(pack, name, allowComp);
    }
  }

  @Test(dataProvider = "items")
  public void linkToSummaryPage(String name, boolean allowComp, boolean summary) {
    logon("AutoTest", "automated");
    SummaryPage item =
        new SearchPage(context)
            .load()
            .setSort("rank")
            .exactQuery("Link to Summary of " + name)
            .getResult(1)
            .viewSummary();
    // Remove when #1160 is fixed. Currently a bug exists that prevents
    // viewFullScreen in the new UI.
    if (!item.usingNewUI()) {
      PackageViewer pack =
          item.attachments().viewFullscreen(getAgree()).preview(new PackageViewer(context));
      pack = pack.clickAttachment(name);

      if (summary && !allowComp) {
        pack.switchToSelectedAttachment(getAgree()).preview(new SummaryPage(context));
        pack.returnToPackageViewer();
      }

      assertTrue(pack.selectedAttachmentContainsText("Music History - The Beatles"));

      AttachmentsPage attachments = pack.switchToSelectedAttachment(new AttachmentsPage(context));
      attachments.attachmentExists("Music History - The Beatles");

      if (!allowComp && !summary) {
        pack =
            attachments
                .viewAttachment("Music History - The Beatles", getDialogAgree())
                .preview(new PackageViewer(context));
      } else {
        pack =
            attachments.viewAttachment("Music History - The Beatles", new PackageViewer(context));
      }
      assertTrue(
          pack.selectedAttachmentContainsText("The Beatles were an English rock band, formed in"));
    }
  }

  private void testItem(PackageViewer pack, String name, boolean allowComp) {
    assertEquals(
        !pack.clickAttachment("Music History - The Beatles")
            .selectedAttachmentContainsText("Terms of use"),
        allowComp,
        allowComp ? "DRM should not be show" : "DRM should be shown");

    assertTrue(
        pack.clickAttachment("Link page")
            .selectedAttachmentContainsText("Music History - The Beatles"));

    LinkPage linkPage = new LinkPage(context);
    assertTrue(linkPage.containsText("Music History - The Beatles"));
    PackageViewer linkedPackage;

    if (!allowComp) {
      linkedPackage = linkPage.clickLink(getAgree()).preview(new PackageViewer(context));
    } else {
      linkedPackage = linkPage.clickLink(context);
    }

    assertTrue(
        linkedPackage
            .clickAttachment("The Beatles")
            .selectedAttachmentContainsText("The Beatles were an English rock band"));
  }

  private void testItem2(PackageViewer pack, String name, boolean allowComp) {

    assertTrue(
        pack.clickAttachment("Link page")
            .selectedAttachmentContainsText("Music History - The Beatles"));

    LinkPage linkPage = new LinkPage(context);
    assertTrue(linkPage.containsText("Music History - The Beatles"));
    PackageViewer linkedPackage;

    if (!allowComp) {
      linkedPackage = linkPage.clickLink(getAgree()).preview(new PackageViewer(context));
    } else {
      linkedPackage = linkPage.clickLink(context);
    }
    assertTrue(
        linkedPackage
            .clickAttachment("The Beatles")
            .selectedAttachmentContainsText("The Beatles were an English rock band"));

    linkPage.restoreFrame();

    assertFalse(
        pack.clickAttachment("Music History - The Beatles")
            .selectedAttachmentContainsText("Terms of use"));
  }

  @Test
  public void attachmentTest() throws UnsupportedEncodingException {
    // DTEC 14598
    // Very similar to DRMPrivilegeTest in Vanilla but for attachments
    final String ERROR_MSG = "Sorry you do not have access to view the page you requested.";
    final String ATTACHMENT_URL =
        "file/677a4bbc-defc-4dc1-b68e-4e2473b66a6a/1/Biscuit factory complex ratios.zip/index.html";
    final String DIRECT_URL =
        "items/677a4bbc-defc-4dc1-b68e-4e2473b66a6a/1/?attachment.uuid=a159f050-15fc-4a95-a9f5-63a821a0426a";
    final String DIRECT_PREVIEW_URL =
        "items/677a4bbc-defc-4dc1-b68e-4e2473b66a6a/1/?attachment.uuid=a159f050-15fc-4a95-a9f5-63a821a0426a&preview=true";

    // Logon and view item with AutoTest
    logon("AutoTest", "automated");
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.exactQuery("DRM IMS package");
    SummaryPage summaryPage = searchPage.results().getResult(1).viewSummary();
    assertTrue(summaryPage.hasAttachmentsSection());
    assertTrue(
        summaryPage.attachments().attachmentExists("Start: Biscuit factory: complex ratios"));
    // Viewing attachments shows acceptance
    if (!summaryPage
        .usingNewUI()) { // Remove when #1160 is fixed. Currently a bug exists that prevents
      // viewFullScreen in the new UI.
      summaryPage
          .attachments()
          .viewAttachment("Start: Biscuit factory: complex ratios", getDialogAgree())
          .preview(
              new VerifyableAttachment(
                  context, "Curriculum Corporation, 2006, except where indicated"));

      // Check that URL is attachment URL
      Assert.assertEquals(getUrl(), context.getBaseUrl() + ATTACHMENT_URL);
    }
    // Logout
    logout();

    // Logon and view item with DRMTest (has discover and not view item)
    logon("DRMTest", "automated");

    // User can find item and view summary
    searchPage = new SearchPage(context).load();
    searchPage.exactQuery("DRM IMS package");
    summaryPage = searchPage.results().getResult(1).viewSummary();
    assertFalse(summaryPage.hasAttachmentsSection());

    // Get the URL and add attachment path
    openUrl(DIRECT_URL);
    assertError(ERROR_MSG, true);

    // User cannot view or preview attachment and does not see agreement
    openUrl(DIRECT_PREVIEW_URL);
    assertError(ERROR_MSG, true);
  }

  @Test
  public void multiImageTest() {
    final String ITEM_NAME = "DRM Image test";

    logon("AutoTest", "automated");

    // Load Item with images
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.exactQuery(ITEM_NAME);
    SummaryPage summaryPage = searchPage.results().getResult(1).viewSummary();
    DRMAgreementDialogPage drmDialog =
        summaryPage.attachments().viewAttachment("cat2.png", new DRMAgreementDialogPage(context));

    // Preview DRM
    ImageGalleryPage imgPage = drmDialog.preview(new ImageGalleryPage(context));

    // Click through
    assertImages(imgPage);

    // Close gallery
    imgPage.closeGallery(summaryPage);
  }

  @Test
  public void redirectTest() throws Exception {
    // DTEC 14600
    final String IMS_DOWNLOAD_URL =
        "items/677a4bbc-defc-4dc1-b68e-4e2473b66a6a/1/viewims.jsp?viewMethod=download";

    HomePage loginPage = logon("AutoTest", "automated");
    openUrl(IMS_DOWNLOAD_URL);
    if (!loginPage.usingNewUI()) {
      DownloadFilePage dlfPage =
          getAgree().preview(new DownloadFilePage(context, "Biscuit factory complex ratios.zip"));
      Assert.assertEquals(getUrl(), context.getBaseUrl() + IMS_DOWNLOAD_URL);
      assertTrue(dlfPage.fileIsDownloaded());
      assertTrue(dlfPage.deleteFile());
    }
  }

  private void openUrl(String url) {
    context.getDriver().get(context.getBaseUrl() + url);
  }

  private String getUrl() throws UnsupportedEncodingException {
    return URLDecoder.decode(context.getDriver().getCurrentUrl(), "UTF-8");
  }

  private void assertError(String error) {
    assertTrue(
        new ErrorPage(context)
            .get()
            .getSubErrorMessage(false)
            .toLowerCase()
            .contains(error.toLowerCase()));
  }

  private void assertError(String error, boolean forceOld) {
    assertTrue(
        new ErrorPage(context, forceOld)
            .get()
            .getSubErrorMessage(false)
            .toLowerCase()
            .contains(error.toLowerCase()));
  }

  private DRMAgreementPage getAgree() {
    return new DRMAgreementPage(context);
  }

  private DRMAgreementDialogPage getDialogAgree() {
    return new DRMAgreementDialogPage(context);
  }

  private void assertImages(ImageGalleryPage imgPage) {
    // Ensure first image
    while (imgPage.hasPrevious()) {
      imgPage.clickPrevious();
    }

    // Click through them
    String current = "current";
    String previous = "previous";
    for (int i = 0; i < 3; i++) {
      // Get currently displayed image and compare to previous
      current = imgPage.getDisplayedFilename();
      assertTrue(!current.equals(previous));
      imgPage.clickNext();
      previous = current;
    }
  }
}
