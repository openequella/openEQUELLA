package com.tle.webtests.pageobject.userscripts;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.entities.AbstractEditEntityPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EditUserScriptPage
    extends AbstractEditEntityPage<EditUserScriptPage, ShowUserScriptsPage> {
  @FindBy(id = "syntax-div")
  private WebElement syntaxCheckDiv;

  @FindBy(xpath = "//div[@class='editor-container']")
  private WebElement editorContainer;

  @FindBy(xpath = "//*[@id='syntax-div']/pre/span")
  private WebElement syntaxCheckResult;

  protected EditUserScriptPage(ShowUserScriptsPage showUserScriptsPage) {
    super(showUserScriptsPage);
  }

  private WebElement elemWithPrefix(String postfix) {
    return findWithId(getEditorSectionId(), postfix);
  }

  private WebElement getScriptTypeList() {
    return elemWithPrefix("_scriptTypeList");
  }

  private WebElement getModuleNameField() {
    return elemWithPrefix("_moduleNameField");
  }

  private WebElement getCheckSyntaxButton() {
    return elemWithPrefix("_checkSyntaxButton");
  }

  @Override
  protected String getEntityName() {
    return "script";
  }

  @Override
  protected String getContributeSectionId() {
    return "usc";
  }

  @Override
  protected String getEditorSectionId() {
    return "use";
  }

  public void pickScriptType(String type) {
    WaitingPageObject<EditUserScriptPage> ajaxUpdate =
        ExpectWaiter.waiter(
            ExpectedConditions2.ajaxUpdateExpectBy(
                driver.findElement(By.id("script-field")),
                By.id(
                    getEditorSectionId()
                        + (type.equals("executable") ? "_moduleNameField" : "_freemarkerlinkDiv"))),
            this);
    new EquellaSelect(getContext(), getScriptTypeList()).selectByValue(type);
    ajaxUpdate.get();
  }

  public void setModuleName(String moduleName) {
    getModuleNameField().clear();
    getModuleNameField().sendKeys(moduleName);
  }

  /**
   * NOTE: You will need to pick script type before the script editing javascript variables become
   * available
   */
  public void setScript(String script, boolean executable) {
    if (executable) {
      ((JavascriptExecutor) driver).executeScript("cmuse_javascript.setValue('" + script + "');");
    } else {
      ((JavascriptExecutor) driver).executeScript("cmuse_freemarker.setValue('" + script + "');");
    }
  }

  public boolean syntaxOk() {
    WaitingPageObject<EditUserScriptPage> ajaxUpdate =
        ajaxUpdateExpect(syntaxCheckDiv, syntaxCheckResult);
    getCheckSyntaxButton().click();
    ajaxUpdate.get();
    return isPresent(By.className("ok")) && !isPresent(By.className("fail"));
  }
}
