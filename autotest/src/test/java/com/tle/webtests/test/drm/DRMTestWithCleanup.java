package com.tle.webtests.test.drm;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.DRMAgreementPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.viewitem.TermsOfUsePage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.util.Arrays;
import java.util.Collections;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class DRMTestWithCleanup extends AbstractCleanupTest {
  @Test
  public void termsOfUse() {
    // DTEC 14603
    final String terms = "You must view this item with your eyes closed";
    final String COLLECTION_ALL_NAME = "DRM All Owner Accept";
    final String ITEM_TERMSTEST_NAME = "Terms test";

    // Logon
    logon("AutoTest", "automated");

    // Contribute and view summary
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_ALL_NAME);
    String fullitemname = context.getFullName(ITEM_TERMSTEST_NAME);
    wizardPage.editbox(1, fullitemname);
    wizardPage.next();
    DRMAgreementPage drmPage = wizardPage.save().publishInvalid(new DRMAgreementPage(context));
    SummaryPage summaryPage =
        drmPage.acceptThisIfYouAreVerySureYouNeedToOtherwiseUsePreview(new SummaryPage(context));
    waitForTermsOfUse(summaryPage);
    TermsOfUsePage termsOfUsePage = summaryPage.termsOfUsePage();

    // Ensure data
    Assert.assertTrue(termsOfUsePage.hasTerms(terms));
    Assert.assertEquals(termsOfUsePage.getNumberOfAcceptances(), 1);
    Assert.assertEquals(termsOfUsePage.getUsersAccepted(), Collections.singletonList("Auto Test"));

    // Logout
    logout();

    // Logon as different user
    logon("DRMTest", "automated");

    // Search
    SearchPage searchPage = new SearchPage(context).load();

    // View and accept
    searchPage.exactQuery(fullitemname);
    searchPage.results().getResult(1).clickTitle();
    drmPage = new DRMAgreementPage(context).get();
    summaryPage = drmPage.acceptThisIfYouAreVerySureYouNeedToOtherwiseUsePreview(summaryPage);
    waitForTermsOfUse(summaryPage);
    termsOfUsePage = summaryPage.termsOfUsePage();

    // Ensure data
    Assert.assertTrue(termsOfUsePage.hasTerms(terms));
    Assert.assertEquals(termsOfUsePage.getNumberOfAcceptances(), 2);
    Assert.assertEquals(termsOfUsePage.getUsersAccepted(), Arrays.asList("DRM Test", "Auto Test"));
  }

  private void waitForTermsOfUse(SummaryPage summaryPage) {
    summaryPage
        .getWaiter()
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space(text())='Terms of use']")));
  }
}
