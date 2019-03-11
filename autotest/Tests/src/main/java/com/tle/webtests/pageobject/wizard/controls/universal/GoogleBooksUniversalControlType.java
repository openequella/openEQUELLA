package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class GoogleBooksUniversalControlType
    extends AbstractUniversalControlType<GoogleBooksUniversalControlType> {

  private WebElement getSearchField() {
    return byWizId("_dialog_gbh_query");
  }

  private WebElement getSearchButton() {
    return byWizId("_dialog_gbh_search");
  }

  private WebElement getMainDiv() {
    return byDialogXPath("//div[contains(@class,'googleBookHandler')]");
  }

  public WebElement getNameField() {
    return byWizId("_dialog_gbh_displayName");
  }

  public GoogleBooksUniversalControlType(UniversalControl control) {
    super(control);
  }

  @Override
  public WebElement getFindElement() {
    return getMainDiv();
  }

  @Override
  public String getType() {
    return "Google Books";
  }

  public GoogleBooksUniversalControlType search(String searchTerm) {
    getSearchField().clear();
    getSearchField().sendKeys(searchTerm);
    WaitingPageObject<GoogleBooksUniversalControlType> submitWaiter = submitWaiter();
    getSearchButton().click();
    return submitWaiter.get();
  }

  public GenericAttachmentEditPage selectBook(int index) {
    return selectBooks(editPage(), index).get();
  }

  private <T extends PageObject> T selectBooks(WaitingPageObject<T> returnTo, int... indexes) {
    for (int i = 0; i < indexes.length; i++) {
      WaitingPageObject<GoogleBooksUniversalControlType> submitWaiter = submitWaiter();
      driver
          .findElement(
              By.id(page.subComponentId(ctrlnum, "dialog_gbh_results_" + (indexes[i] - 1))))
          .click();
      submitWaiter.get();
    }

    WaitingPageObject<T> disappearWaiter =
        ExpectWaiter.waiter(removalCondition(getAddButton()), returnTo);
    getAddButton().click();
    return disappearWaiter.get();
  }

  public UniversalControl addBooks(int... indexes) {
    return selectBooks(control.attachmentCountExpectation(indexes.length), indexes);
  }
}
