package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.ItemDetailsPage;
import com.tle.webtests.pageobject.viewitem.PackageViewer;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import org.openqa.selenium.By;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class ItemSummaryTest extends AbstractCleanupTest {

  private static String COLLECTION = "Basic Items";
  private static String COLLECTION2 = "Metadata Mapping";
  private static String COLLECTION3 = "Basic Attachments";

  private static String USERNAME = "AutoTest";
  private static String SHAREDID = "contribute";
  private static String SECRET = "contribute";
  private static String ACTION = "selectOrAdd";
  private static String LAST_KNOWN_OWNER_NAME = "LastKnown Owner (removed)";
  private static String LAST_KNOWN_OWNER_ITEM_NAME = "LastKnownOwner Test";

  public ItemSummaryTest() {
    setDeleteCredentials(AUTOTEST_LOGON, AUTOTEST_PASSWD);
  }

  @Test
  public void aboutLinkTest() {
    String summaryItem = context.getFullName("an item");
    String dontFindItem = context.getFullName("do not find");

    logon();
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION2);

    wizard.editbox(1, dontFindItem);
    wizard.save().publish();

    wizard = new ContributePage(context).load().openWizard(COLLECTION);

    wizard.editbox(1, summaryItem);
    SummaryPage summary = wizard.save().publish();
    assertEquals(summary.getCollection(), COLLECTION);
    SearchPage search = summary.clickCollection();
    assertEquals(search.getSelectedWithin(), COLLECTION);
    assertFalse(search.exactQuery(dontFindItem).isResultsAvailable());
    assertTrue(search.exactQuery(summaryItem).isResultsAvailable());
    logout(context);
  }

  @Test
  public void lastOwnerTest() {
    logon();
    SearchPage searchPage = new SearchPage(context).load();
    ItemListPage itemList = searchPage.resultsPageObject();
    SummaryPage summaryPage = itemList.getResultForTitle(LAST_KNOWN_OWNER_ITEM_NAME).viewSummary();

    // Check if LAST_KNOWN_OWNER_ITEM_NAME is displayed instead of 'unknown user'
    By lastKnownOwnerLabel = By.xpath("//span[@title='LastKnownOwner']");
    String lastOwnerName = context.getDriver().findElement(lastKnownOwnerLabel).getText();
    assertEquals(lastOwnerName, LAST_KNOWN_OWNER_NAME);

    // Open Moderation History page and check if the user of each event is LAST_KNOWN_OWNER_NAME
    summaryPage.viewItemModerationHistory();
    int numberOfLastKnownOwnerLabel = context.getDriver().findElements(lastKnownOwnerLabel).size();
    assertEquals(numberOfLastKnownOwnerLabel, 3);
  }

  @Test
  public void returnFromFullScreen() {
    final String attachment = "package.zip";

    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION2);
    String fullName = context.getFullName("full screen");
    wizard.editbox(1, fullName);
    wizard = wizard.next();
    UniversalControl control = wizard.universalControl(1);
    FileUniversalControlType file =
        control.addDefaultResource(new FileUniversalControlType(control));
    file.uploadPackageOption(Attachments.get(attachment)).showStructure().save();
    SummaryPage summary = wizard.save().publish();
    PackageViewer pv = summary.attachments().viewFullscreen();
    assertTrue(pv.hasTitle());
    summary = pv.clickTitle();
    assertEquals(summary.attachments().attachmentCount(), 1);

    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.getSignonUrl(ACTION, USERNAME, "", "", true);

    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));

    summary = session.homeExactSearch(fullName).viewFromTitle(fullName);
    pv = summary.attachments().viewFullscreen();
    assertFalse(pv.hasTitle());
  }

  @Test
  public void testViewCount() {
    logon(AUTOTEST_LOGON, AUTOTEST_PASSWD);

    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION2);
    String fullName = context.getFullName("view count");
    wizard.editbox(1, fullName);
    wizard = wizard.next();
    SummaryPage summary = wizard.save().publish();

    ItemDetailsPage details = summary.itemDetails();
    assertEquals(details.getViewCount(), 1, "Incorrect view count");

    // reload page...
    context.getDriver().navigate().refresh();
    details = new SummaryPage(context).get().itemDetails();
    assertEquals(details.getViewCount(), 2, "Incorrect view count");
  }
}
