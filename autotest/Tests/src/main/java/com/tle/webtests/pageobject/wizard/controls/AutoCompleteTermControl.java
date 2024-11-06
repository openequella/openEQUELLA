package com.tle.webtests.pageobject.wizard.controls;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.StringSelectedStuff;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AutoCompleteTermControl extends NewAbstractWizardControl<AutoCompleteTermControl> {
  private WebElement getControlElem() {
    return byWizId("dautocompleteControl");
  }

  private By getControlBy() {
    return By.id(getWizid() + "dautocompleteControl");
  }

  private WebElement getSelectButton() {
    return byWizId("d_s");
  }

  private WebElement getTermField() {
    return byWizId("d_e");
  }

  private WebElement getSelectionsTable() {
    return byWizId("d_t");
  }

  private WebDriverWait acWaiter;

  public AutoCompleteTermControl(
      PageContext context, int ctrlnum, AbstractWizardControlPage<?> page) {
    super(context, ctrlnum, page);
    acWaiter =
        new WebDriverWait(
            driver, context.getTestConfig().getStandardTimeout(), Duration.ofMillis(600));
  }

  @Override
  protected WebElement findLoadedElement() {
    return getControlElem();
  }

  public void addNewTerm(String term) {
    getTermField().sendKeys(term);
    WaitingPageObject<StringSelectedStuff> waiter = getSelections().selectionWaiter(term);
    getSelectButton().click();
    waiter.get();
  }

  public WizardPageTab selectExistingTerm(String prefix, WizardPageTab wizardPage) {
    return selectExistingTerm(
        prefix,
        ExpectWaiter.waiter(ExpectedConditions2.ajaxUpdate(getControlElem()), wizardPage),
        1);
  }

  public WizardPageTab selectExistingTerm(
      String prefix, WaitingPageObject<WizardPageTab> nextPage, int number) {
    waiter.until(
        ExpectedConditions2.elementAttributeToContain(
            getTermField(), "class", "ui-autocomplete-input"));
    getTermField().clear();
    getTermField().sendKeys(prefix);

    AutoCompleteTermResults results = listWait();
    results.selectByIndex(number);
    return nextPage.get();
  }

  public String getAddedTermByIndex(int index) {
    return getSelectionsTable().findElement(By.xpath("//tbody/tr[" + index + "]/td[1]")).getText();
  }

  public AutoCompleteTermControl selectNothing() {
    WaitingPageObject<AutoCompleteTermControl> waiter = updateWaiter(getSelectionsTable());
    getSelectButton().click();
    return waiter.get();
  }

  public StringSelectedStuff getSelections() {
    return new StringSelectedStuff(context, getControlBy());
  }

  public void removeTerm(String term) {
    StringSelectedStuff selections = getSelections();
    selections.clickActionWithConfirm(term, "Remove", Boolean.TRUE, selections.removalWaiter(term));
  }

  private AutoCompleteTermResults listWait() {
    acWaiter.until(
        new ExpectedCondition<Boolean>() {
          private String lastQuery;

          @Override
          public Boolean apply(WebDriver driver) {

            boolean loaded = new AutoCompleteTermResults(AutoCompleteTermControl.this).isLoaded();
            if (Check.isEmpty(lastQuery) && loaded) {
              return true;
            }

            if (Check.isEmpty(lastQuery)) {
              lastQuery = getTermField().getAttribute("value");
              getTermField().sendKeys(Keys.ESCAPE);
              getTermField().clear();
              ((JavascriptExecutor) driver)
                  .executeScript("$(arguments[0]).keydown();", getTermField());
            } else {
              getTermField().sendKeys(lastQuery);
              lastQuery = null;
            }

            return false;
          }
        });

    return new AutoCompleteTermResults(this).get();
  }
}
