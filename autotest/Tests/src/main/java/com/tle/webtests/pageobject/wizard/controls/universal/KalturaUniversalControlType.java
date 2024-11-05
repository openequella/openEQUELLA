package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KalturaUniversalControlType
    extends AbstractUniversalControlType<KalturaUniversalControlType> {
  private WebElement getAddExisting() {
    return byWizId("_dialog_kh_choice_0");
  }

  private WebElement getAddNew() {
    return byWizId("_dialog_kh_choice_1");
  }

  private WebElement getSearchField() {
    return byWizId("_dialog_kh_query");
  }

  private WebElement getSearchButton() {
    return byWizId("_dialog_kh_search");
  }

  private WebElement getMainDiv() {
    return byDialogXPath("//div[contains(@class,'kalturaHandler')]");
  }

  public WebElement getNameField() {
    return byWizId("_dialog_kh_displayName");
  }

  private WebElement getKcwFlashWidget() {
    return byWizId("_dialog_kh_divKcw");
  }

  public enum KalturaOption {
    EXISTING,
    NEW
  }

  public KalturaUniversalControlType(UniversalControl control) {
    super(control);
    this.waiter = new WebDriverWait(driver, Duration.ofMinutes(1), Duration.ofMillis(50));
  }

  public KalturaUniversalControlType search(String query) {
    clickChoice("Add existing Kaltura media");
    waitForElement(getSearchField());
    getSearchField().clear();
    getSearchField().sendKeys(query);
    WaitingPageObject<KalturaUniversalControlType> submitWaiter = submitWaiter();
    getSearchButton().click();
    return submitWaiter.get();
  }

  public GenericAttachmentEditPage selectExistingVideo(int index) {
    return selectExistingVideos(editPage(), index);
  }

  public UniversalControl addExistingVideos(int... indexes) {
    return selectExistingVideos(control.attachmentCountExpectation(indexes.length), indexes);
  }

  private <T extends PageObject> T selectExistingVideos(
      WaitingPageObject<T> returnTo, int... index) {
    for (int i = 0; i < index.length; i++) {
      WaitingPageObject<KalturaUniversalControlType> submitWaiter = submitWaiter();
      driver
          .findElement(By.id(page.subComponentId(ctrlnum, "dialog_kh_results_" + (index[i] - 1))))
          .click();
      submitWaiter.get();
    }

    WaitingPageObject<T> disappearWaiter =
        ExpectWaiter.waiter(removalCondition(getAddButton()), returnTo);
    getAddButton().click();
    return disappearWaiter.get();
  }

  public void clickChoice(String choice) {
    driver.findElement(By.xpath("//h4/label[text()=" + quoteXPath(choice) + "]")).click();
    getNextButton().click();
  }

  public String addNewVideo(String displayName, String... tags) {
    getAddNew().click();
    getNextButton().click();
    waitForElement(getKcwFlashWidget());

    kcwNext();

    return "";
  }

  @Override
  public String getType() {
    return "Kaltura";
  }

  @Override
  public WebElement getFindElement() {
    return getMainDiv();
  }

  public boolean isDisabled() {
    return isPresent(By.xpath("//h3[text()='Kaltura media server is disabled']"));
  }

  public void kcwNext() {
    ((JavascriptExecutor) driver)
        .executeScript(
            "nextStep = function(){document.getElementById("
                + getKcwFlashWidget().getAttribute("id")
                + ").goNextStep();};");
  }

  public void kcwPrev() {
    ((JavascriptExecutor) driver)
        .executeScript(
            "prevStep = function(){document.getElementById("
                + getKcwFlashWidget().getAttribute("id")
                + ").goPrevStep();};");
  }
}
