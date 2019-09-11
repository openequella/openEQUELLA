package com.tle.webtests.test.contribute;

import static com.tle.webtests.pageobject.AbstractPage.quoteXPath;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog.ConfirmButton;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.DuplicatesTab;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.AbstractWizardControlsTest;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class UniquenessTest extends AbstractWizardControlsTest {

  private static final String ITEM_NAME = "Simple Control Test";
  private static final String COLLECTION_NAME = "Uniqueness Collection";
  private final String DUPLICATE_CHECK_COLLECTION = "Duplicate Check Test";
  private final String DUPLICATE_CHECK_ITEM_NAME = "uniquenessTest - Duplicate Check";
  private final String DUPLICATE_CHECK_ITEM_DIFFERENT_NAME =
      "uniquenessTest - Duplicate Check different ";
  private final String DUPLICATE_WARNING_LINK_TEXT =
      "Click here to view existing items that contain duplicates";
  private final String DUPLICATE_CHECK_ITEM_ID = "Unique ID";
  private final String DUPLICATE_CHECK_ITEM_DIFFERENT_ID = "Different ID";
  private final String FIRST_FILE_NAME = "A.txt";
  private final String SECOND_FILE_NAME = "B.txt";
  private final String FIRST_LINK_NAME = "http://www.google.com";
  private final String SECOND_LINK_NAME = "https://www.google.com";

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

  @Test
  public void createDuplicateCheckTestData() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);
    wizardPage.editbox(1, DUPLICATE_CHECK_ITEM_NAME);
    wizardPage.editbox(2, DUPLICATE_CHECK_ITEM_ID);
    wizardPage.addFile(3, FIRST_FILE_NAME);
    wizardPage.addUrl(3, FIRST_LINK_NAME, FIRST_LINK_NAME);
    wizardPage.save().publish();
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void editboxDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);

    // Test an editbox which gives warning when duplicates found and allows duplicates
    String controlDivId = "p0c1";
    wizardPage.editbox(1, DUPLICATE_CHECK_ITEM_NAME);
    ConfirmationDialog confirmationDialog = wizardPage.save();
    assertTrue(confirmationDialog.containsButton(ConfirmButton.PUBLISH));
    confirmationDialog.cancel(wizardPage);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, DUPLICATE_CHECK_ITEM_NAME);
    wizardPage.get();
    wizardPage.editbox(1, DUPLICATE_CHECK_ITEM_DIFFERENT_NAME);
    wizardPage.save().cancel(wizardPage);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);

    // Test an editbox which doesn't allow duplicates
    controlDivId = "p0c2";
    wizardPage.editbox(2, DUPLICATE_CHECK_ITEM_ID);
    confirmationDialog = wizardPage.save();
    assertTrue(confirmationDialog.containsButton(ConfirmButton.COMPLETE));
    confirmationDialog.cancel(wizardPage);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(true, DUPLICATE_CHECK_ITEM_ID);
    wizardPage.get();
    wizardPage.editbox(2, DUPLICATE_CHECK_ITEM_DIFFERENT_ID);
    wizardPage.save().cancel(wizardPage);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void singleFileDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);

    String controlDivId = "p0c3";
    wizardPage.addFile(3, FIRST_FILE_NAME);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void multipleFilesDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);

    String controlDivId = "p0c3";
    // Add two files. One has duplicates and the other doesn't.
    wizardPage.addFiles(3, false, FIRST_FILE_NAME, SECOND_FILE_NAME);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME);

    // Delete the file not having duplicates
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(SECOND_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME);

    // Ddelete the other one having duplicates
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void singleLinkDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);

    String controlDivId = "p0c3";
    wizardPage.addUrl(3, FIRST_LINK_NAME, FIRST_LINK_NAME);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void multipleLinksDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);

    String controlDivId = "p0c3";
    // Add two links. One has duplicates and the other doesn't.
    wizardPage.addUrl(3, FIRST_LINK_NAME, FIRST_LINK_NAME);
    wizardPage.addUrl(3, SECOND_LINK_NAME, SECOND_LINK_NAME);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);

    // Delete the link not having duplicates
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(SECOND_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);

    // Re-add the deleted link and delete the other one having duplicates
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  @Test(dependsOnMethods = "createDuplicateCheckTestData")
  public void mixedAttchmentsDuplicateCheck() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizardPage = contributePage.openWizard(DUPLICATE_CHECK_COLLECTION);
    String controlDivId = "p0c3";

    // Add one file having duplicates and one link not having duplicate
    wizardPage.addFile(3, FIRST_FILE_NAME);
    wizardPage.addUrl(3, SECOND_LINK_NAME, SECOND_LINK_NAME);
    boolean warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(SECOND_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);

    // Add one file not having duplicates and one link having duplicate
    wizardPage.addFile(3, SECOND_FILE_NAME);
    wizardPage.addUrl(3, FIRST_LINK_NAME, FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(SECOND_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);

    // Add one file having duplicate and one link having too
    wizardPage.addFile(3, FIRST_FILE_NAME);
    wizardPage.addUrl(3, FIRST_LINK_NAME, FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_FILE_NAME, FIRST_LINK_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_FILE_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, true);
    assertTrue(warningDisplayed);
    checkDuplicateItem(false, FIRST_LINK_NAME);
    wizardPage.get();
    wizardPage.universalControl(3).deleteResource(FIRST_LINK_NAME);
    warningDisplayed = findWarningLink(wizardPage, controlDivId, false);
    assertFalse(warningDisplayed);
  }

  private boolean findWarningLink(
      WizardPageTab wizardPage, String controlDivId, boolean isWarningDisplayed) {
    By linkTextXpath =
        By.xpath(
            "//div[@id='"
                + controlDivId
                + "']//a[text()="
                + quoteXPath(DUPLICATE_WARNING_LINK_TEXT)
                + "]");
    if (isWarningDisplayed) {
      WebElement warningLink =
          wizardPage.getWaiter().until(ExpectedConditions.elementToBeClickable(linkTextXpath));
      // Check if the warning link text is correct
      Assert.assertEquals(
          warningLink.getText(), "Click here to view existing items that contain duplicates");
      warningLink.click();
      return true;
    } else {
      wizardPage.getWaiter().until(ExpectedConditions.invisibilityOfElementLocated(linkTextXpath));
    }
    return false;
  }

  private void checkDuplicateItem(boolean isUniqueEnforced, String... duplicateContentTexts) {
    DuplicatesTab duplicateDataPage = new DuplicatesTab(context).get();

    for (String duplicateContentText : duplicateContentTexts) {
      By duplicateContentXpath =
          By.xpath(
              "//div[starts-with(@class,'duplicate')]//*[contains(text(),"
                  + quoteXPath(duplicateContentText)
                  + ")]");
      WebElement duplicateContent =
          duplicateDataPage
              .getWaiter()
              .until(ExpectedConditions.visibilityOfElementLocated(duplicateContentXpath));
      Assert.assertEquals(
          duplicateContent.getText(), "'" + duplicateContentText + "' is also used by:");
    }

    By duplicatesFoundXpath = By.xpath("//a[text()='" + DUPLICATE_CHECK_ITEM_NAME + "']");
    WebElement duplicateItem =
        duplicateDataPage
            .getWaiter()
            .until(ExpectedConditions.visibilityOfElementLocated(duplicatesFoundXpath));
    // Check if the correct duplicate item is found
    Assert.assertEquals(duplicateItem.getText(), "uniquenessTest - Duplicate Check");

    if (isUniqueEnforced) {
      By uniquenessMarkXpath = By.className("mandatory");
      WebElement uniquenessMark = context.getDriver().findElement(uniquenessMarkXpath);
      Assert.assertEquals(uniquenessMark.getText(), "*");
    }

    WebElement prevButton = context.getDriver().findElement(By.name("nav_previousButton"));
    prevButton.click();
  }
}
