package com.tle.webtests.test.contribute.bugs;

import static com.tle.webtests.framework.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.testng.annotations.Test;

/**
 * This is a test to ensure that GH issue #1678 has not regressed.
 *
 * @see <a href="https://github.com/openequella/openEQUELLA/issues/1678">GitHub Issue 1678</a>
 */
@TestInstitution("vanilla")
public class VisibilityScriptingBugTest extends AbstractCleanupTest {
  private static final String METADATA_INPUT_USER = "cpddm";
  private static final String TOGGLE_USER = "csme";
  private static final String PASS = "tle010";

  private static final String COLLECTION = "Test Wizard Issue";
  private static final String ITEM_NAME = "VisibilityScriptingBugTest - Lost Metadata Test Item 1";

  private static final Date DATE = new Date(1589256742342L);
  private static final String DATE_AS_STRING = new SimpleDateFormat("yyyy-MM-dd").format(DATE);

  private static final String CHECKBOX = "one";
  private static final String DROPDOWN = "List 2";
  private static final String EMAIL = "test@test.com";
  private static final String ROLE = "System Administrator";

  private static final String RADIO1 = "Radio 1";
  private static final String RADIO2 = "Yes";
  private static final String SHUFFLE1 = "Shuffle 1";
  private static final String SHUFFLE2 = "Shuffle 2";

  private static final String XML_PATH = "item/itembody/";
  private static final String ROLE_ID = "ROLE_SYSTEM_ADMINISTRATOR";
  private static final String USER_ID = "4026b550-9a1f-4521-9b56-9ec737c02526";

  @Test
  public void testLostMetadataBug() {
    // Login as cpddm and begin contribution to 'Test Wizard Issue' collection.
    logon(METADATA_INPUT_USER, PASS);
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);

    // Insert a value into each control and publish the item.
    wizard.getControl(1).sendKeys(ITEM_NAME);
    wizard.calendar(2).setDate(DATE);
    wizard.setCheck(3, CHECKBOX, true);
    wizard.emailSelector(4).addEmail(EMAIL);
    wizard.selectDropDown(5, DROPDOWN);
    wizard.setCheck(6, RADIO1, true);
    wizard.selectRole(7).openDialog().search(ROLE).selectAndFinish(ROLE, wizard);
    wizard.shuffleBox(8).moveAllRight();
    wizard
        .selectUser(10)
        .openDialog()
        .search(TOGGLE_USER)
        .selectAndFinish(TOGGLE_USER, wizard)
        .checkLoaded();
    wizard.save().publish();

    // Login as csme and find the same item.
    logon(TOGGLE_USER, PASS);
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.search(ITEM_NAME);
    SummaryPage summaryPage = searchPage.resultsPageObject().viewFromTitle(ITEM_NAME);

    // If the item is in a locked state, unlock it
    // (this is mainly so that rerunning the test before it cleans up doesn't wrongly fail the test)
    if (summaryPage.isItemLocked()) {
      summaryPage.unlockAfterLogout();
    }

    // Edit the item as csme.
    // This will open a different wizard page to the one cpddm sees, because of visibility
    // scripting.
    WizardPageTab newWizard = summaryPage.edit(2);

    // Make a change (this is where the metadata would become lost because of the bug) and save.
    newWizard.setCheckReload(1, RADIO2, true).saveNoConfirm().checkLoaded();
    // Open the item's xml page to view the metadata.
    ItemXmlPage xml = summaryPage.itemXml();

    // Assert that all of the metadata has been retained.
    assertTrue(xml.nodeHasValue(XML_PATH + "name", ITEM_NAME));
    assertTrue(xml.nodeHasValue(XML_PATH + "calendar", DATE_AS_STRING));
    assertTrue(xml.nodeHasValue(XML_PATH + "checkbox", CHECKBOX));
    assertTrue(xml.nodeHasValue(XML_PATH + "email", EMAIL));
    assertTrue(xml.nodeHasValue(XML_PATH + "radiobutton", RADIO1));
    assertTrue(xml.nodeHasValue(XML_PATH + "role", ROLE_ID));
    assertTrue(xml.nodeHasValue(XML_PATH + "shuffle", SHUFFLE1));
    assertTrue(xml.nodeHasValue(XML_PATH + "shuffle", SHUFFLE2));
    assertTrue(xml.nodeHasValue(XML_PATH + "user", USER_ID));
  }
}
