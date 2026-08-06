package com.tle.webtests.pageobject.generic.component;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.text.MessageFormat;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class SelectGroupDialog extends AbstractPage<SelectGroupDialog> {
  private final String baseId;

  private WebElement getQueryField() {
    return byBaseId("_sg_q");
  }

  private WebElement getSearchButton() {
    return byBaseId("_sg_s");
  }

  private WebElement getOkButton() {
    return byBaseId("_ok");
  }

  private WebElement getCancelButton() {
    return byBaseId("_cancel");
  }

  @FindBy(id = "results")
  private WebElement resultsDiv;

  @FindBy(className = "resultlist")
  private WebElement resultsList;

  public SelectGroupDialog(PageContext context, String baseId) {
    super(context);
    this.baseId = baseId;
  }

  public WebElement byBaseId(String postfix) {
    return driver.findElement(By.id(baseId + postfix));
  }

  @Override
  protected void checkLoadedElement() {
    ensureVisible(getQueryField(), getSearchButton(), getOkButton());
  }

  public SelectGroupDialog search(String query) {
    getQueryField().clear();
    getQueryField().sendKeys(query);
    WaitingPageObject<SelectGroupDialog> ajaxUpdateExpect =
        ajaxUpdateExpect(resultsDiv, resultsList);
    getSearchButton().click();
    ajaxUpdateExpect.get();
    waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//ul/li"));
    return get();
  }

  public boolean searchWithoutMatch(String query) {
    getQueryField().clear();
    getQueryField().sendKeys(query);
    WaitingPageObject<SelectGroupDialog> ajaxUpdateExpect =
        ajaxUpdateExpect(resultsDiv, resultsList);
    getSearchButton().click();
    ajaxUpdateExpect.get();
    waitForElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"));
    String text =
        driver
            .findElement(By.xpath("id('" + baseId + "')//div[@id='results']//h4[text()]"))
            .getText();
    if (text.equals("Your search did not match any groups.")) {
      return true;
    }
    return false;
  }

  public boolean containsGroupname(String groupname) {
    return !driver.findElements(getByForGroupname(groupname)).isEmpty();
  }

  public void select(String groupname) {
    driver.findElement(getByForGroupname(groupname)).click();
  }

  private By getByForGroupname(String groupname) {
    String xpath =
        MessageFormat.format(
            "id({0})//div[@id={1}]//ul/li[div[text()={2}]]/input",
            quoteXPath(baseId), quoteXPath("results"), quoteXPath(groupname));
    return By.xpath(xpath);
  }

  public <T extends AbstractPage<T>> T selectAndFinish(
      String groupname, WaitingPageObject<T> page) {
    select(groupname);
    return finish(page);
  }

  public <T extends AbstractPage<T>> T finish(WaitingPageObject<T> page) {
    getOkButton().click();
    return page.get();
  }

  public <T extends AbstractPage<T>> T cancel(WaitingPageObject<T> page) {
    ExpectedCondition<Boolean> removalCondition = removalCondition(getCancelButton());
    getCancelButton().click();
    waiter.until(removalCondition);
    return page.get();
  }
}
