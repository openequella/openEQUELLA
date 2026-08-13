package com.tle.webtests.test.drm;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.dytech.devlib.PropBagEx;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.PowerSearchPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.DRMAccessWizardPage;
import com.tle.webtests.pageobject.wizard.DRMUsageWizardPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.WizardUrlPage;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class WizardDRMTest extends AbstractWizardControlsTest {
  private static final String AUTOTEST_USERID = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
  private static final String TERM_MSG = "These are the DRM terms";
  private static final String ITEM_ALL_NAME = "All Options";
  private static final String ITEM_CUSTOM_NAME = "Custom Options";
  private static final String ITEM_NO_NAME = "No Options";
  private static final String ITEM_CONTROL_NAME = "Control Test";
  private static final String COLLECTION_ALL_NAME = "DRM All Contributor Options";
  private static final String COLLECTION_CUSTOM_NAME = "DRM Custom Contributor Options";
  private static final String COLLECTION_NO_NAME = "DRM No Contributor Options";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @BeforeClass
  public void setUp() {
    logon();
  }

  @Test
  public void contributeAll() throws Exception {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_ALL_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_ALL_NAME));
    wizardPage.next();
    DRMUsageWizardPage rightsPage = new DRMUsageWizardPage(context, wizardPage);
    rightsPage.setWho("everyone");
    rightsPage.addOther("Jolse", "jolse.maginnis@equella.com");
    rightsPage.setWhat("adapt");
    wizardPage.next();
    DRMAccessWizardPage accessPage = new DRMAccessWizardPage(context, wizardPage);
    accessPage.selectUserId(AUTOTEST_USERID);
    accessPage.selectNetwork("localhost");
    accessPage.setLicenseCount(10);
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2020);
    cal.set(Calendar.DAY_OF_MONTH, 8);
    cal.set(Calendar.MONTH, Calendar.APRIL);
    accessPage.setDateRange(new Date(0), cal.getTime());
    accessPage.setEducationSector(true);
    accessPage.setRequireAttribution(true);
    accessPage.setAcceptanceTerms(TERM_MSG);
    SummaryPage viewing = wizardPage.save().publish();
    ItemId itemId = viewing.getItemId();
    PropBagEx itemXml = getItemXml(itemId);
    checkOdrl(itemXml);
    wizardPage = new WizardUrlPage(context, itemId).edit();
    wizardPage.next();
    wizardPage.next();
    wizardPage.saveNoConfirm();
    itemXml = getItemXml(itemId);
    checkOdrl(itemXml);
  }

  @Test
  public void contributeAllNone() throws Exception {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_ALL_NAME);
    wizardPage.editbox(1, context.getFullName(""));
    wizardPage.next();
    DRMUsageWizardPage rightsPage = new DRMUsageWizardPage(context, wizardPage);
    rightsPage.setWho("myself");
    rightsPage.setWhat("basic");
    wizardPage.next();
    SummaryPage viewing = wizardPage.save().publish();
    ItemId itemId = viewing.getItemId();
    PropBagEx itemXml = getItemXml(itemId);
    checkOdrlCustom(itemXml, true);
    wizardPage = new WizardUrlPage(context, itemId).edit();
    wizardPage.next();
    wizardPage.next();
    wizardPage.saveNoConfirm();
    itemXml = getItemXml(itemId);
    checkOdrlCustom(itemXml, true);
  }

  @Test
  public void contributeAllEnableDisableTest() throws Exception {
    // DTEC tests 14596, 14597, 14897
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_ALL_NAME);
    String fullitemname = context.getFullName(ITEM_CONTROL_NAME);
    wizardPage.editbox(1, fullitemname);
    wizardPage.next();
    DRMUsageWizardPage rightsPage = new DRMUsageWizardPage(context, wizardPage);
    rightsPage.setWho("myself");
    rightsPage.setWhat("basic");
    wizardPage.next();
    DRMAccessWizardPage accessPage = new DRMAccessWizardPage(context, wizardPage);

    // Enable disable Licence acceptance control
    assertFalse(accessPage.isLicenseCountEnabled());
    accessPage.enableLicenseCount(true);
    assertTrue(accessPage.isLicenseCountEnabled());
    accessPage.enableLicenseCount(false);
    assertFalse(accessPage.isLicenseCountEnabled());

    // Enable disable DRM date range control
    assertFalse(accessPage.isDateRangeEnabled());
    accessPage.enableDateRange(true);
    assertTrue(accessPage.isDateRangeEnabled());
    accessPage.enableDateRange(false);
    assertFalse(accessPage.isDateRangeEnabled());

    // Require agreement and attempt save without agreement filled
    assertFalse(accessPage.isRequireTermsAcceptanceEnabled());
    accessPage.enableRequireTermsAcceptance(true);
    assertTrue(accessPage.isRequireTermsAcceptanceEnabled());

    accessPage = wizardPage.save().finishInvalid(accessPage);

    // Check for error and enter
    accessPage.hasError("Please enter a value in this field");
    accessPage.setAcceptanceTerms(TERM_MSG);

    // Fill out correctly and save and get ItemID
    ItemId itemId = wizardPage.save().publish().getItemId();

    // Search page
    SearchPage searchPage = new SearchPage(context).load();

    // Choose power search
    PowerSearchPage powerSearchPage = searchPage.setWithinPowerSearch("DRM Party search");

    // Fill out criteria
    WebElement control = powerSearchPage.getControl(1);
    control.clear();

    control.sendKeys("Auto Test");

    // Get results
    searchPage = powerSearchPage.search();
    searchPage.exactQuery(fullitemname);
    Assert.assertEquals(searchPage.results().getResult(1).getTitle(), fullitemname);

    // Check item xml
    PropBagEx itemXml = getItemXml(itemId);
    assertWizardControlValueEquals(
        itemXml, "/item/rights/offer/party/context/name", "Auto Test [AutoTest]");
  }

  @Test
  public void contributeCustom() throws Exception {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_CUSTOM_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_CUSTOM_NAME));
    wizardPage.next();
    DRMUsageWizardPage rightsPage = new DRMUsageWizardPage(context, wizardPage);
    rightsPage.setWho("myself");
    rightsPage.setWhat("custom");
    rightsPage.setCustomUse(true, "print", "play");
    rightsPage.setCustomUse(false, "display", "execute");
    rightsPage.setCustomReuse(true, "modify", "excerpt");
    rightsPage.setCustomReuse(false, "annotate", "aggregate");
    SummaryPage viewing = wizardPage.save().publish();
    ItemId itemId = viewing.getItemId();
    PropBagEx itemXml = getItemXml(itemId);
    checkOdrlCustom(itemXml, false);
    wizardPage = new WizardUrlPage(context, itemId).edit();
    wizardPage.next();
    rightsPage.setWhat("basic");
    wizardPage.saveNoConfirm();
    itemXml = getItemXml(itemId);
    checkOdrlCustom(itemXml, true);
  }

  private void checkOdrlCustom(PropBagEx itemXml, boolean basic) {
    PropBagEx rightsXml = itemXml.getSubtree("item/rights/offer");
    assertUser(
        rightsXml.getSubtree("party[0]"),
        "Auto Test [AutoTest]",
        "auto@test.com",
        AUTOTEST_USERID,
        true);
    PropBagEx permissionXml = rightsXml.getSubtree("permission");
    if (!basic) {
      assertPermissions(permissionXml, "print", "play", "modify", "excerpt");
      assertNonPermissions(permissionXml, "display", "execute", "annotate", "aggregate");
    } else {
      assertPermissions(permissionXml, "display", "execute", "print", "play");
      assertNonPermissions(permissionXml, "modify", "excerpt", "annotate", "aggregate");
    }
    assertFalse(permissionXml.nodeExists("requirement/accept"));
    assertNoConstraints(permissionXml);
  }

  @Test
  public void contributeNone() throws Exception {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NO_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_NO_NAME));
    wizardPage.next();
    DRMUsageWizardPage rightsPage = new DRMUsageWizardPage(context, wizardPage);
    rightsPage.setWho("others");
    rightsPage.addOther("Jolse", "jolse.maginnis@equella.com");
    SummaryPage viewing = wizardPage.save().publish();
    ItemId itemId = viewing.getItemId();
    PropBagEx itemXml = getItemXml(itemId);
    checkOdrlNone(itemXml);
    wizardPage = new WizardUrlPage(context, itemId).edit();
    wizardPage.next();
    wizardPage.saveNoConfirm();
    itemXml = getItemXml(itemId);
    checkOdrlNone(itemXml);
  }

  private void checkOdrlNone(PropBagEx itemXml) {
    PropBagEx rightsXml = itemXml.getSubtree("item/rights/offer");
    assertUser(
        rightsXml.getSubtree("party[0]"), "Jolse", "jolse.maginnis@equella.com", null, false);
    PropBagEx permissionXml = rightsXml.getSubtree("permission");
    assertNonPermissions(
        permissionXml,
        "display",
        "execute",
        "play",
        "print",
        "modify",
        "excerpt",
        "annotate",
        "aggregate");
    assertFalse(permissionXml.nodeExists("requirement/accept"));
    assertNoConstraints(permissionXml);
  }

  private void assertNoConstraints(PropBagEx permissionXml) {
    PropBagEx constraintXml = permissionXml.getSubtree("container/constraint");
    if (constraintXml != null) {
      assertFalse(constraintXml.nodeExists("purpose"));
      assertFalse(constraintXml.nodeExists("individual"));
      assertFalse(constraintXml.nodeExists("count"));
      assertFalse(constraintXml.nodeExists("network"));
      assertFalse(constraintXml.nodeExists("datetime"));
    }
  }

  private void checkOdrl(PropBagEx itemXml) {
    PropBagEx rightsXml = itemXml.getSubtree("item/rights/offer");
    assertUser(
        rightsXml.getSubtree("party[0]"),
        "Auto Test [AutoTest]",
        "auto@test.com",
        AUTOTEST_USERID,
        true);
    assertUser(
        rightsXml.getSubtree("party[1]"), "Jolse", "jolse.maginnis@equella.com", null, false);
    PropBagEx permissionXml = rightsXml.getSubtree("permission");
    assertPermissions(
        permissionXml,
        "display",
        "execute",
        "play",
        "print",
        "modify",
        "excerpt",
        "annotate",
        "aggregate");

    assertTrue(permissionXml.nodeExists("requirement/attribution"));
    assertWizardControlValueEquals(permissionXml, "requirement/accept/context/remark", TERM_MSG);
    PropBagEx constraintXml = permissionXml.getSubtree("container/constraint");
    assertWizardControlValueEquals(constraintXml, "purpose/@type", "sectors:educational");
    assertUser(constraintXml.getSubtree("individual"), null, null, AUTOTEST_USERID, false);
    assertWizardControlValueEquals(constraintXml, "count", "10");
    assertNetwork(constraintXml.getSubtree("network"), "localhost", "127.0.0.1", "127.0.0.1");
    assertWizardControlValueEquals(constraintXml, "datetime/start", "1970-01-01T00:00:00");
    assertWizardControlValueEquals(constraintXml, "datetime/end", "2020-04-08T00:00:00");
  }

  private void assertNetwork(PropBagEx networkXml, String name, String min, String max) {
    assertWizardControlValueEquals(networkXml, "@name", name);
    assertWizardControlValueEquals(networkXml, "range/min", min);
    assertWizardControlValueEquals(networkXml, "range/max", max);
  }

  private void assertPermissions(PropBagEx permissionXml, String... permissions) {
    for (String permission : permissions) {
      assertTrue(permissionXml.nodeExists(permission), "Missing permission '" + permission + "':");
    }
  }

  private void assertNonPermissions(PropBagEx permissionXml, String... permissions) {
    for (String permission : permissions) {
      assertFalse(
          permissionXml.nodeExists(permission), "Shouldn't have permission '" + permission + "':");
    }
  }

  private void assertUser(PropBagEx party, String name, String email, String uuid, boolean owner) {
    PropBagEx context = party.getSubtree("context");
    Assert.assertEquals(context.isNodeTrue("@owner"), owner);
    if (name != null) {
      assertWizardControlValueEquals(context, "name", name);
    }
    if (email != null) {
      assertWizardControlValueEquals(context, "remark", email);
    }
    if (uuid != null) {
      assertWizardControlValueEquals(context, "uid", "tle:" + uuid);
    }
  }

  /**
   * Since the metadata returned from the standard Item GET API does not include DRM data, a
   * different approach where the metadata including DRM is retrieved from the Item <XML> endpoint
   * is implemented in this test suite.
   */
  @Override
  protected PropBagEx getItemXml(ItemId itemId) throws IOException {
    String token = getToken();

    URI itemXmlUri =
        URI.create(
            context.getBaseUrl()
                + "items/"
                + itemId.getUuid()
                + "/"
                + itemId.getVersion()
                + "/%3CXML%3E");
    HttpResponse response = execute(new HttpGet(itemXmlUri), false, token);

    Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
    return new PropBagEx(response.getEntity().getContent());
  }
}
