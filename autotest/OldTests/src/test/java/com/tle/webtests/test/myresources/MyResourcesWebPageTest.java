package com.tle.webtests.test.myresources;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.page.VerifyableAttachment;
import com.tle.webtests.pageobject.myresources.MyResourcesAuthorWebPage;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.annotations.Test;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14446 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14449 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14450 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14959 Test Reference:
 * http://time/DTEC/test/editTest.aspx?testId=14966
 *
 * @author larry
 */
@TestInstitution("myresources")
public class MyResourcesWebPageTest extends AbstractCleanupTest {

  @Test
  public void addNewWebPage() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("An authored page");
    String pageTitle = "A Page";
    String pageContent = "This is a verifiable attachment";
    int presumedPageNo = 1;

    authorWebPageAndVerify(scrapbookItem, pageTitle, pageContent)
        .openWebpage(scrapbookItem, pageTitle);

    assertTrue(new VerifyableAttachment(context).get().isVerified());
  }

  /** Test Reference: http://time/DTEC/test/editTest.aspx?testId=14449 */
  @Test
  public void deleteOneFromMultipleWebPage() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

    int deleteIndex = 1;
    String itemName = context.getFullName("Hesitation is the trademark of deliberation");
    String[] pageTitles = {
      "This page title stays", "This page title gets deleted", "This page title also stays"
    };
    MyResourcesAuthorWebPage editor = openEditorToMultiWebPageItem(itemName, pageTitles);

    editor.deletePage(pageTitles[deleteIndex]);
    MyResourcesPage myResourcesPage = editor.save();

    if (testConfig.isNewUI()) {
      // This Scrapbook had 5 pages. Because one was deleted there should be 4 pages in the
      // attachment list now.
      myResourcesPage.checkAttachmentNumber(itemName, 4);
    }
    for (int i = 0; i < pageTitles.length; ++i) {
      assertEquals(
          i == deleteIndex, myResourcesPage.isIndividualPageDeleted(itemName, pageTitles[i]));
    }
  }

  @Test
  public void addAndEditWebPageItem() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("Another web-page");
    String[] pageTitles = {
      "Chapter 1",
      "Chapter 2",
      "Chapter 3",
      "Chapter 4",
      "Chapter 5",
      "Chapter 6",
      "Chapter 7",
      "Chapter 8"
    };

    String abandonedDescStr =
        "And did those feet in ancient time walk upon England's mountains green?";
    String pageName = "Chapter 4";
    String pageTitle = "An alternate Name";

    // Edit the page but abandon changes.
    MyResourcesAuthorWebPage editor = openEditorToMultiWebPageItem(scrapbookItem, pageTitles);
    editor.setDescription(abandonedDescStr);
    editor.editPage(pageName);
    editor.setTitle(pageTitle);
    ((JavascriptExecutor) getContext().getDriver())
        .executeScript("window.scrollTo(0, -document.body.scrollHeight)");
    MyResourcesPage myResourcesPage = editor.cancel();

    // Verify the old page name hasn't changed.
    if (testConfig.isNewUI()) {
      myResourcesPage.expandAttachmentsForScrapbookItem(scrapbookItem);
    }
    assertTrue(myResourcesPage.isIndividualPagePresent(scrapbookItem, pageName));

    // Now make an edit and save the changes.
    myResourcesPage.editWebPage(scrapbookItem);
    scrapbookItem = context.getFullName("Tyger, tyger burning bright");
    pageName = "Chapter 3";
    pageTitle = "What the anvil, what the flame?";

    editor.setDescription(scrapbookItem);
    editor.editPage(pageName);
    editor.setTitle(pageTitle);
    ((JavascriptExecutor) getContext().getDriver())
        .executeScript("window.scrollTo(0, -document.body.scrollHeight)");
    editor.save().exactQuery(scrapbookItem);

    // Verify the page name has changed.
    if (testConfig.isNewUI()) {
      myResourcesPage.expandAttachmentsForScrapbookItem(scrapbookItem);
    }
    assertTrue(myResourcesPage.isIndividualPagePresent(scrapbookItem, pageTitle));
  }

  @Test
  public void addAndEditWebPageItemContent() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("Blake's Zoology");
    String pageName = "Chapter 2";
    String[] pageTitles = {"Chapter 1", "Chapter 2", "Chapter 3"};
    String[] pageBodies = {
      "And did those feet in ancient times",
      "Walk upon England's mountains green?",
      "And was the holy lamb of god"
    };

    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .authorWebPages(scrapbookItem, pageTitles, pageBodies);

    assertNewPageCreated(myResourcesPage, scrapbookItem);

    // Edit the webpage editor and save the changes.
    MyResourcesAuthorWebPage editor = myResourcesPage.editWebPage(scrapbookItem);

    String[] newHtmlContent = {
      "Tyger, tyger, burning bright In the forests of the night,",
      "What immortal hand or eye Could frame thy fearful symmetry?",
      "In what distant deeps or skies Burnt the fire of thine eyes?",
      "On what wings dare he aspire? What the hand dare seize the fire?",
      "And what shoulder and what art Could twist the sinews of thy heart?",
      "And when thy heart began to beat, What dread hand and what dread feet?",
      "What the hammer? what the chain? In what furnace was thy brain?",
      "What the anvil? What dread grasp Dare its deadly terrors clasp?",
      "When the stars threw down their spears, And water'd heaven with their tears,",
      "Did He smile His work to see? Did He who made the lamb make thee?",
      "Tyger, tyger, burning bright In the forests of the night,",
      "What immortal hand or eye Dare frame thy fearful symmetry?",
      "William Blake, 1794."
    };

    editor.editPage(pageName);
    editor.setBodyHtml(String.join("<br>", newHtmlContent));
    editor.save();

    // Open again but get the page content only.
    myResourcesPage.editWebPage(scrapbookItem);
    editor.editPage(pageName);
    String htmlAreaContent = editor.getBodyText();
    editor.cancel();

    // check that it's all there - not caring about newlines here.
    for (String content : newHtmlContent) {
      assertTrue(
          htmlAreaContent.contains(content),
          "Failed to find '" + content + "' in edit area content");
    }
  }

  @Test
  public void addAndDeleteAuthoredWebPageItem() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

    String scrapbookItem = "An authored page";

    MyResourcesPage myResourcesPage =
        authorWebPageAndVerify(scrapbookItem, "A Page", "This is a verifiable attachment")
            .deleteScrapbook(scrapbookItem);

    assertTrue(myResourcesPage.isScrapbookDeleted(scrapbookItem));
  }

  @Test
  public void addAndAlternateOverWebPagesInItem() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    String scrapbookItem = context.getFullName("Blake's Zoology");

    String[] pageTitles = {"Chapter 1", "Chapter 2"};
    String[] pageBodies = {
      "And Gilgamesh said to Enkidu: 'Let us journey to the land of the scorpion men.'",
      "And Enkidu said unto Gilgamesh 'One of us shall lead and the other follow: let our strength"
          + " of arm decide!'"
    };

    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .authorWebPages(scrapbookItem, pageTitles, pageBodies);

    assertNewPageCreated(myResourcesPage, scrapbookItem);

    String[] miscAddsForFirstPage = {
      "One man went to mow he",
      "went to mow a meadow",
      "two men went to mow they",
      "both went to mow the meadow"
    };
    String[] miscAddsForSecondPage = {
      "We three kings of orient are",
      "travelling round in an old bomb car",
      "CRASH! BANG!",
      "We two kings of orient are"
    };

    // Load for edit and change the HTML content of the page
    MyResourcesAuthorWebPage editor = myResourcesPage.editWebPage(scrapbookItem);

    // swap between the two pages, adding a bit more distinct content each
    // time
    for (int i = 0; i < miscAddsForFirstPage.length; ++i) {
      editor.editPage("Chapter 1");
      editor.appendBodyHtml(miscAddsForFirstPage[i]);
      editor.editPage("Chapter 2");
      editor.appendBodyHtml(miscAddsForSecondPage[i]);
    }
    // Save and return to My resources page
    myResourcesPage = editor.save();

    // reload each page and verify they have the correct content
    editor = myResourcesPage.editWebPage(scrapbookItem);

    editor.editPage("Chapter 1");
    String htmlAreaContentOfFirst = editor.getBodyText();
    editor.editPage("Chapter 2");
    String htmlAreaContentOfSecond = editor.getBodyText();

    editor.cancel();

    for (int i = 0; i < miscAddsForFirstPage.length; ++i) {
      if (i % 2 == 0) {
        assertTrue(htmlAreaContentOfFirst.contains(miscAddsForFirstPage[i]));
        assertFalse(htmlAreaContentOfFirst.contains(miscAddsForSecondPage[i]));
      } else {
        assertTrue(htmlAreaContentOfSecond.contains(miscAddsForSecondPage[i]));
        assertFalse(htmlAreaContentOfSecond.contains(miscAddsForFirstPage[i]));
      }
    }
  }

  @Test
  public void uploadAttachmentToWebPageTest() {
    final String WEBPAGE_NAME = namePrefix + " Attachment Web Page";
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);
    MyResourcesPage myResources = new MyResourcesPage(context, "scrapbook").load();
    MyResourcesAuthorWebPage authorPage = myResources.authorWebPage();
    authorPage.setDescription(WEBPAGE_NAME);
    authorPage.addPage("attachment", "");
    URL file = Attachments.get("courses.csv");
    authorPage.equellaFileUploader(file, "erryday i'm uploadin'");

    myResources = authorPage.save();

    assertNewPageCreated(myResources, WEBPAGE_NAME);
  }

  private void assertNewPageCreated(MyResourcesPage myResourcesPage, String webPageName) {
    assertTrue(myResourcesPage.isScrapbookCreated(webPageName));
  }

  // convenience method to create and open editor to a multi-page item.
  public MyResourcesAuthorWebPage openEditorToMultiWebPageItem(
      String itemName, String[] pageTitles) {
    String[] pageBodies = {
      "And did those feet in ancient times",
      "Walk upon England's mountains green?",
      "And was the holy lamb of god",
      "On England's pastures seen?",
      "And did the countenance divine shine forth upon these clouded hills?"
    };

    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .authorWebPages(itemName, pageTitles, pageBodies);

    myResourcesPage.exactQuery(itemName);

    return myResourcesPage.editWebPage(itemName);
  }

  // convenience local method to concoct a webpage into scrapbook.
  private MyResourcesPage authorWebPageAndVerify(
      String scrapbookItem, String pageTitle, String pageContent) {
    MyResourcesPage myResourcesPage =
        new MyResourcesPage(context, "scrapbook")
            .load()
            .authorWebPage(scrapbookItem, pageTitle, pageContent);

    assertNewPageCreated(myResourcesPage, scrapbookItem);

    return myResourcesPage;
  }
}
