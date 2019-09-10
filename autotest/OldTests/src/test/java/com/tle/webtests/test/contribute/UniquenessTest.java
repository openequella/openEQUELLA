package com.tle.webtests.test.contribute;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class UniquenessTest extends AbstractWizardControlsTest {

  private static final String ITEM_NAME = "Simple Control Test";
  private static final String COLLECTION_NAME = "Uniqueness Collection";

  @Override
  protected void prepareBrowserSession() {
    logon();
  }

  @Test
  public void contributeFirst() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_NAME));
    wizardPage.editbox(2, "Will be warned - Edit Box");
    wizardPage.editbox(3, "Will be forced - Edit Box");
    wizardPage.addToShuffleList(4, "Will be warned - Shuffle List");
    wizardPage.addToShuffleList(6, "Will be forced - Shuffle List");
    wizardPage.save().publish();
  }

  @Test(dependsOnMethods = "contributeFirst")
  public void contributeSecond() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(COLLECTION_NAME);
    wizardPage.editbox(1, context.getFullName(ITEM_NAME));
    wizardPage.editbox(2, "Will be warned - Edit Box");
    wizardPage.editbox(3, "Will be forced - Edit Box");
    wizardPage.addToShuffleList(4, "Will be warned - Shuffle List");
    wizardPage.addToShuffleList(6, "Will be forced - Shuffle List");
    wizardPage.save().finishInvalid(wizardPage.updateWaiter());
    Assert.assertEquals(
        wizardPage.getErrorMessage(3), "The value in this field must be unique across items");
    Assert.assertEquals(
        wizardPage.getErrorMessage(6), "The value in this field must be unique across items");
    wizardPage.editbox(3, "No longer the same - Edit Box");
    ShuffleListControl shuf = wizardPage.shuffleList(6);
    shuf.remove("Will be forced - Shuffle List");
    wizardPage.addToShuffleList(6, "No longer the same - Shuffle List");
    wizardPage.save().publish();
  }
}
