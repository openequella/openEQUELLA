package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ShuffleListControl extends AbstractWizardControl<ShuffleListControl> {
  private WebElement getAddButton() {
    return byWizId("_addButton");
  }

  private WebElement getTextField() {
    return byWizId("_text");
  }

  private WebElement getDiv() {
    return byWizId("_div");
  }

  public ShuffleListControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page) {
    super(context, ctrlnum, page);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getTextField();
  }

  protected By getRowForValue(String value) {
    return By.xpath("//tr/td[@class='shuffle-text' and text()=" + quoteXPath(value) + "]/..");
  }

  public void add(String value) {
    WebElement textField = getTextField();
    textField.clear();
    textField.sendKeys(value);
    getAddButton().click();
    waitForElement(getRowForValue(value));
  }

  public void remove(String value) {
    WebElement removeBtn =
        getDiv().findElement(getRowForValue(value)).findElement(By.className("shuffle-remove"));
    forceButtonClickWithJS(removeBtn);
  }

  public void edit(String value) {
    getDiv().findElement(getRowForValue(value)).findElement(By.className("shuffle-edit")).click();
  }

  public boolean isDisabled() {
    return (" " + getDiv().getAttribute("class").trim()).contains(" disabled");
  }
}
