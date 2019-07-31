package com.tle.webtests.failalways;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.UrlUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.Arrays;
import java.util.List;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class ReorderAttachmentsTest extends AbstractCleanupTest {
  private static String COLLECTION3 = "Basic Attachments";

  @Test
  public void testReorderAttachments() {
    final String urlOne = "http://urlOne.com";
    final String urlTwo = "http://urlTwo.com";
    final String urlThree = "http://urlThree.com";
    List<String> urls = Arrays.asList(urlOne, urlTwo, urlThree);

    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

    WizardPageTab wiz = new ContributePage(context).load().openWizard(COLLECTION3);
    String itemName = context.getFullName("reorder item");
    wiz.editbox(1, itemName);

    for (String u : urls) {
      UniversalControl universal = wiz.universalControl(3);
      UrlUniversalControlType url = universal.addResource(new UrlUniversalControlType(universal));
      universal = url.addUrl(u, u);
    }

    AttachmentsPage attachments = wiz.save().publish().attachments();
    assertEquals(attachments.attachmentOrder(), urls);
    attachments.startReorder();
    attachments.reorder(urlThree, true);
    attachments.reorder(urlThree, true);
    attachments.reorder(urlOne, false);
    attachments.finishReorder();

    logout();
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    attachments = SearchPage.searchAndView(context, itemName).attachments();

    urls.set(0, urlThree);
    urls.set(2, urlOne);
    assertEquals(attachments.attachmentOrder(), urls);
  }
}
