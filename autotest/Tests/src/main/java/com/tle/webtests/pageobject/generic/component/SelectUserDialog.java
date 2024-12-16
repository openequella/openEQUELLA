package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.text.MessageFormat;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class SelectUserDialog extends AbstractPage<SelectUserDialog> {
  private final String baseId;

  private WebElement getQueryField() {
    return byBaseId("_su_q");
  }

  private WebElement getSearchButton() {
    return byBaseId("_su_s");
  }

  private WebElement getOkButton() {
    return byBaseId("_ok");
  }

  private WebElement getCloseButton() {
    return byBaseId("_close");
  }

  @FindBy(id = "results")
  private WebElement resultsDiv;

  @FindBy(className = "resultlist")
  private WebElement resultsList;

  public WebElement byBaseId(String postfix) {
    return driver.findElement(By.id(baseId + postfix));
  }

  public SelectUserDialog(PageContext context, String baseId) {
    super(context);
    this.baseId = baseId;
  }

  @Override
  protected void checkLoadedElement() {
    ensureVisible(getQueryField(), getSearchButton(), getOkButton());
  }

  public SelectUserDialog search(String query) {
    getQueryField().clear();
    getQueryField().sendKeys(query);
    WaitingPageObject<SelectUserDialog> ajaxUpdateExpect =
        ajaxUpdateExpect(resultsDiv, resultsList);
    getSearchButton().click();
    ajaxUpdateExpect.get();
    waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//ul/li"));
    return get();
  }

  public boolean searchWithoutMatch(String query) {
    getQueryField().clear();
    getQueryField().sendKeys(query);
    WaitingPageObject<SelectUserDialog> ajaxUpdateExpect =
        ajaxUpdateExpect(resultsDiv, resultsList);
    getSearchButton().click();
    ajaxUpdateExpect.get();
    waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"));
    String text =
        driver
            .findElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"))
            .getText();
    if (text.equals("Your search did not match any users.")) {
      return true;
    }
    return false;
  }

  public boolean containsUsername(String username) {
    return !driver.findElements(getByForUsername(username)).isEmpty();
  }

  public SelectUserDialog select(String username) {
    WebElement userElem = driver.findElement(getByForUsername(username));
    scrollToElement(userElem);
    userElem.click();
    return get();
  }

  private By getByForUsername(String username) {
    String xpath =
        MessageFormat.format(
            "id({0})//div[@id={1}]//ul/li[div[text() = {2}]]/input",
            quoteXPath(baseId), quoteXPath("results"), quoteXPath(username));
    return By.xpath(xpath);
  }

  public <T extends AbstractPage<T>> T selectAndFinish(String username, WaitingPageObject<T> page) {
    select(username);
    return finish(page);
  }

  public <T extends AbstractPage<T>> T finish(WaitingPageObject<T> page) {
    getOkButton().click();
    return page.get();
  }

  public <T extends AbstractPage<T>> T cancel(WaitingPageObject<T> page) {
    ExpectedCondition<Boolean> removalCondition = removalCondition(getCloseButton());
    getCloseButton().click();
    waiter.until(removalCondition);
    return page.get();
  }
}
