package com.tle.webtests.test.contribute.controls.attachments;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentAccessDeniedPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class RestrictedAttachmentsTest extends AbstractSessionTest {
  private final String ITEM = "RESTRICTED_ATTACHMENTS";
  private final String[] ATTACHMENTS = {"url", "web page", "book", "youtube", "iTunesU", "Flickr"};
  private final String ERROR = "Access denied";

  // TODO LTI, Echo360, Kaltura

  @Test
  public void testViewingRestrictedAttachments() {
    logon("autotest", "automated");

    SummaryPage restrictedItem = SearchPage.searchAndView(context, ITEM);
    assertFalse(restrictedItem.hasAttachmentsSection());
    logon("canViewRestricted", "``````");
    AttachmentsPage attachments = SearchPage.searchAndView(context, ITEM).attachments();
    assertEquals(attachments.attachmentCount(), 6, "restricted attachment(s) missing");
    String[] attachmentUrls = new String[6];
    for (int x = 0; x < 6; x++) {
      attachmentUrls[x] = attachments.getAttachmentURL("Restricted " + ATTACHMENTS[x]);
    }

    logon("autotest", "automated");
    for (int x = 0; x < 6; x++) {
      ErrorPage error = new AttachmentAccessDeniedPage(context, attachmentUrls[x]).load();
      assertEquals(error.getMainErrorMessage(false), ERROR);
    }
  }

  @Test
  public void testRestrictedXML() {
    logon("autotest", "automated");
    ItemXmlPage restrictedXML = SearchPage.searchAndView(context, ITEM).itemXml();
    assertTrue(restrictedXML.nodeIsEmpty("item/attachments"));
    logon("canViewRestricted", "``````");
    restrictedXML = SearchPage.searchAndView(context, ITEM).itemXml();
    assertEquals(restrictedXML.getNodeCount("item/attachments/attachment"), ATTACHMENTS.length);
  }
}
