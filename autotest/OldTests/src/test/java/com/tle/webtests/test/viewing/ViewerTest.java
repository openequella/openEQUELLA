package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.LocalWebDriver;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.DownloadFilePage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.ImageGalleryPage;
import com.tle.webtests.pageobject.viewitem.ImagePage;
import com.tle.webtests.pageobject.viewitem.LTIViewerPage;
import com.tle.webtests.pageobject.viewitem.LargeImageViewerPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.viewitem.VideoPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("contribute")
@LocalWebDriver
public class ViewerTest extends AbstractCleanupAutoTest {

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @Test
  public void downloadFileTest() {
    String type = "Download file";
    SummaryPage summary = SearchPage.searchAndView(context, "BasicViewerImage");

    DownloadFilePage fileDownload =
        new DownloadFilePage(context, "google(4).png", "169e859db7f28a01e1b51e1c9e2d6b2b");
    fileDownload.deleteFile();

    assertTrue(summary.attachments().viewAttachment(type, fileDownload).fileIsDownloaded());
    fileDownload.deleteFile();
  }

  @Test
  public void galleryTest() {
    String type = "Gallery";
    SummaryPage summary = SearchPage.searchAndView(context, "BasicViewerImage");
    String displayedTitle =
        summary
            .attachments()
            .viewAttachment(type, new ImageGalleryPage(context))
            .getDisplayedTitle();
    assertEquals(displayedTitle, type);
  }

  @Test
  public void livTest() {
    String type = "Large image viewer";
    SummaryPage summary = SearchPage.searchAndView(context, "BasicViewerImage");
    LargeImageViewerPage liv =
        summary.attachments().viewAttachment(type, new LargeImageViewerPage(context));
    assertTrue(liv.zoomInButtonExists());
  }

  @Test
  public void fileViewerTest() {
    String type = "File viewer";
    SummaryPage summary = SearchPage.searchAndView(context, "BasicViewerImage");
    ImagePage image = summary.attachments().viewAttachment(type, new ImagePage(context));
    assertTrue(
        image.imageSource().contains("file/136f2956-21f9-4a80-8af1-8dbb18ca34e9/1/google.png"));
  }

  @Test
  public void testHtml5Player() {
    String type = "mp4 video";
    // TODO: ogg and webm videos
    SummaryPage summary = SearchPage.searchAndView(context, "html5 viewer item");
    VideoPage vid = summary.attachments().viewAttachment(type, new VideoPage(context));
    assertTrue(
        vid.videoSource().contains("oceans-clip.mp4") || vid.videoSource().equals("flash"),
        "wrong source, found: " + vid.videoSource());
    assertTrue(vid.ensureLibrariesLoaded(), "html5 player libraries not loaded");
  }

  @Test
  public void testLTIViewer() {
    SummaryPage summary = SearchPage.searchAndView(context, "lti viewer item");
    LTIViewerPage lti =
        summary.attachments().viewAttachment("youtube tool", new LTIViewerPage(context));
    lti.searchYoutube("ghosts");
    lti.embedYTResult(4);
  }
}
