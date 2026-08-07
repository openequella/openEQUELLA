/** */
package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog.ConfirmButton;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.Test;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=15096 Preconditions: the wizard for
 * COLLECTIONS_NAME is set up with 2 edit boxes and a 3rd control being a checkbox, which enables a
 * second page to exist in the contribution sequence. This test presupposes that a second wizard
 * page exists but is not mandatory, hence if the user has not checked the box indicating that they
 * wish the resource to be added, then the item can be saved and published as is (the only
 * unilaterally mandatory data is the "Name" field in the first editBox).
 *
 * @author larry
 */
@TestInstitution("contribute")
public class VaryingContribPageNmbrSaveTest extends AbstractCleanupAutoTest {
  private static String COLLECTIONS_NAME = "Basic Items with variable wizard pages";

  /**
   * Preserved in the exported contributions - tests/contribute/institution.tar.gz being the uuid
   * for the COLLECTIONS_NAME collection.
   */
  private static String COLLECTIONS_ID_FROM_TEST_INSTITUTION =
      "cfbde79c-dfee-4f44-b5a8-70d11055914b";

  /**
   * Not having checked the "Resource" to enable a second page for the wizard we expect a dialogue
   * box with the options to either Publish, Draft or Cancel, and we don't expect Submit (because
   * there is no workflow moderation applicable, nor Complete Wizard (incorrect).
   */
  @Test
  public void performSaveTestSecondPageNotEnabled() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
    wizard.editbox(1, COLLECTIONS_NAME + "_item");

    ConfirmationDialog confirmationDialog = wizard.save();
    askedToFinishCannotPublish(false, confirmationDialog);
    // close the dialog box lest it interfere with any further tests.
    confirmationDialog.cancel(wizard);
  }

  /**
   * Same as above except for the step to check the "Resource" to enable a second page for the
   * wizard. Accordingly on attempting "Save" we expect a dialogue box with the options to either
   * Complete Wizard, Save as Draft or Cancel, and we don't expect Submit (because there is no
   * workflow moderation applicable, nor Publish (incorrect).
   */
  @Test
  public void performSaveTestSecondPageEnabled() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
    wizard.editbox(1, COLLECTIONS_NAME + "_item");

    wizard.setCheckNextAppear(3, "navtree", true);
    ConfirmationDialog confirmationDialog = wizard.save();
    askedToFinishCannotPublish(true, confirmationDialog);
    // close the dialog box and clear the checkbox lest they interfere with
    // any further tests.
    confirmationDialog.cancel(wizard);
    wizard.setCheckNextDisappear(3, "navtree", false);
    wizard.cancel(contributePage);
  }

  @Test
  public void performSaveAfterToggleSecondPageEnable() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
    wizard.editbox(1, COLLECTIONS_NAME + "_item");
    // Click once, 2nd page now enabled, user can Complete Wizard but cannot
    // publish
    wizard.setCheckNextAppear(3, "navtree", true);
    ConfirmationDialog confirmationDialog = wizard.save();
    askedToFinishCannotPublish(true, confirmationDialog);

    // having verified the first dialog presentation is correct, close it
    // (by clicking cancel) and continue the test.
    confirmationDialog.cancel(wizard);
    // Click again (and clear checkbox), 2nd page now disabled, user can
    // Publish
    wizard.setCheckNextDisappear(3, "navtree", false);
    confirmationDialog = wizard.save();
    askedToFinishCannotPublish(false, confirmationDialog);
    // close the dialog box lest it interfere with any further tests
    confirmationDialog.cancel(wizard);
    // checkbox already cleared
  }

  @Test
  public void performSaveAfterNavigateToSecondPage() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
    wizard.editbox(1, COLLECTIONS_NAME + "_item");
    // Click once, 2nd page now enabled, user can Complete Wizard but cannot
    // publish
    wizard.setCheckNextAppear(3, "navtree", true);
    ConfirmationDialog confirmationDialog = wizard.save();
    askedToFinishCannotPublish(true, confirmationDialog);

    // close the dialog box to continue the test
    confirmationDialog.cancel(wizard);

    // Click again (and clear checkbox), 2nd page now disabled, user can
    // Publish
    wizard.setCheckNextDisappear(3, "navtree", false);
    confirmationDialog = wizard.save();
    askedToFinishCannotPublish(false, confirmationDialog);

    // close the dialog box to continue the test
    confirmationDialog.cancel(wizard);

    // now enable the 2nd page again, and travel to it
    wizard.setCheckNextAppear(3, "navtree", true);
    WizardPageTab nextWizardPageTab = wizard.next();

    // Assumed that default settings are sufficient to allow a save

    ConfirmationDialog saveDialog = nextWizardPageTab.save();

    askedToFinishCannotPublish(false, saveDialog);
    // close the dialog box lest it interfere with any further tests.
    saveDialog.cancel(wizard);
  }

  /**
   * Populate lists of expected and unexpected strings, verify content of Confirmation Dialogue's
   * buttons.
   *
   * @param askedToFinish
   * @param confirmationDialog
   */
  private void askedToFinishCannotPublish(
      boolean askedToFinish, ConfirmationDialog confirmationDialog) {
    // we can always either save as Draft, or Cancel
    List<ConfirmButton> expectations = new ArrayList<ConfirmButton>();
    expectations.add(ConfirmButton.DRAFT);
    expectations.add(ConfirmButton.CANCEL);

    // No moderation involved, Submit for Moderation never applicable.
    List<ConfirmButton> unexpectations = new ArrayList<ConfirmButton>();
    unexpectations.add(ConfirmButton.SUBMIT_FOR_MOD);

    // Publish and Finish are mutually exclusive
    if (askedToFinish) {
      expectations.add(ConfirmButton.COMPLETE);
      unexpectations.add(ConfirmButton.PUBLISH);
    } else {
      expectations.add(ConfirmButton.PUBLISH);
      unexpectations.add(ConfirmButton.COMPLETE);
    }

    for (ConfirmButton expectMe : expectations) {
      assertTrue(confirmationDialog.containsButton(expectMe));
    }

    for (ConfirmButton dontExpectMe : unexpectations) {
      assertFalse(confirmationDialog.containsButton(dontExpectMe));
    }
  }
}
