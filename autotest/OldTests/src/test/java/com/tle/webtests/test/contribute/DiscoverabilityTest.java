package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.generic.HarvestPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import org.testng.annotations.Test;

@TestInstitution("contribute")
public class DiscoverabilityTest extends AbstractCleanupTest {

  private static final String COLLECTION = "Discoverability";

  @Test
  public void metaTest() {
    String fullName = context.getFullName("an item");
    String description = "A description";

    logon("AutoTest", "automated");
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1).setText(fullName);
    wizard.editbox(2).setText(description);

    SummaryPage summary = wizard.save().publish();
    // Skip this check for new UI until Github issue #1148 is fixed
    if (!summary.usingNewUI()) {
      assertTrue(summary.hasMeta("citation_title", fullName));
      assertTrue(summary.hasMeta("citation_description", description));
    }
  }

  @Test
  public void validationTest() {
    String fullName = context.getFullName("an item");
    String description = "A description";

    logon("AutoTest", "automated");
    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1).setText(fullName);
    wizard.editbox(2).setText(description);

    SummaryPage summary = wizard.save().publish();
    assertEquals(summary.getItemTitle(), fullName);
    // New UI implements the badge and banner very differently,
    // so it's pointless to check these two sections.
    // This is NOT a bug.
    if (!summary.usingNewUI()) {
      assertTrue(summary.badgeIsADiv());
      assertTrue(summary.bannerIsADiv());
    }
  }

  @Test
  public void harvestTest() {
    String fullName = context.getFullName("an item");
    String description = "A description";

    logon("AutoTest", "automated");

    WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
    wizard.editbox(1).setText(fullName);
    wizard.editbox(2).setText(description);
    wizard.save().publish();

    HarvestPage harvestPage = new HarvestPage(context).load();
    // Skip this check for new UI until Github issue #1148 is fixed
    if (!harvestPage.usingNewUI()) {
      assertTrue(harvestPage.hasMeta("robots", "noindex, follow"));
    }

    // might not be on the first page so loop through
    int pages = harvestPage.getPageCount();
    boolean found = false;
    for (int i = 0; i < pages; i++) {
      harvestPage.clickPage(i);
      if (harvestPage.hasItem(fullName)) {
        found = true;
        break;
      }
    }
    assertTrue(found);

    SummaryPage summary = harvestPage.clickItem(fullName);
    assertEquals(fullName, summary.getItemTitle());
    assertEquals(description, summary.getItemDescription());
  }
}
