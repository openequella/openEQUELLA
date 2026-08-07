package com.tle.webtests.pageobject.myresources;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class AbstractAuthorWebPage<T extends AbstractAuthorWebPage<T>>
    extends AbstractPage<T> {
  private static final By XPATH_ALLROWS = By.xpath("tbody/tr[td/@class='name']");

  @FindBy(xpath = "//h3[text()='Pages']")
  protected WebElement titleElem;

  @FindBy(xpath = "//legend[text()='Edit page']")
  protected WebElement editPageElem;

  protected WebElement getDescriptionField() {
    return findByBaseId("mypttl_titleField");
  }

  protected WebElement getTitleField() {
    return findByBaseId("mpe_pageNameField");
  }

  protected WebElement getAddButton() {
    return findByBaseId("mpa_a");
  }

  protected WebElement getTable() {
    return findByBaseId("mpa_p");
  }

  @FindBy(xpath = "//body[@id='tinymce']")
  protected WebElement editorBody;

  @FindBy(
      xpath =
          "//table[@id='mpctinyedit_html_toolbar3']//img[@class = 'mceIcon'][contains(@src,"
              + " 'paperclip.gif')]")
  private WebElement attachmentButton;

  @FindBy(id = "_fileName")
  private WebElement fileName;

  @FindBy(id = "_fileUpload")
  private WebElement fileUpload;

  private WebElement getHtmliFrame() {
    return findByBaseId(IFRAME_ID_PREFIX);
  }

  public static final String IFRAME_ID_PREFIX = "tinyedit_html_ifr";

  private final String baseId;
  private final boolean edit;

  public AbstractAuthorWebPage(PageContext context, String baseId) {
    this(context, baseId, false);
  }

  public AbstractAuthorWebPage(PageContext context, String baseId, boolean edit) {
    super(context);
    this.baseId = baseId;
    this.edit = edit;
  }

  private WebElement findByBaseId(String postfix) {
    return driver.findElement(By.id(getBaseId() + postfix));
  }

  @Override
  protected WebElement findLoadedElement() {
    return edit ? editPageElem : titleElem;
  }

  public T editPage(String name) {
    return new PageRow(getPageRowByName(name)).edit();
  }

  public void setDescription(String description) {
    getDescriptionField().clear();
    getDescriptionField().sendKeys(description);
  }

  private By getPageRowByName(String name) {
    return By.xpath("tbody/tr[td[@class='name']/a/text() = " + quoteXPath(name) + "]");
  }

  public int getPageCount() {
    return getTable().findElements(XPATH_ALLROWS).size();
  }

  public T deletePage(String name) {
    return new PageRow(getPageRowByName(name)).delete();
  }

  public void setTitle(String pageTitle) {
    getTitleField().clear();
    getTitleField().sendKeys(pageTitle);
  }

  public void addPage(String pageTitle, String pageBody) {
    if (!edit) {
      ExpectedCondition<?> addCondition = getAddCondition();
      getAddButton().click();
      waiter.until(addCondition);
    }
    setTitle(pageTitle);
    setBodyHtml(pageBody);
  }

  public String getBodyText() {
    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.id(baseId + IFRAME_ID_PREFIX)));
    String editorText = editorBody.getText();
    driver.switchTo().defaultContent();
    return editorText;
  }

  public String getBodyHtml() {
    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.id(baseId + IFRAME_ID_PREFIX)));
    String editorHtml =
        (String)
            ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].innerHTML;", editorBody);
    driver.switchTo().defaultContent();
    return editorHtml;
  }

  public void appendBodyHtml(String htmlText) {
    setBodyHtml(getBodyHtml() + htmlText);
  }

  public void setBodyHtml(String pageBody) {
    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.id(baseId + IFRAME_ID_PREFIX)));
    driver.switchTo().activeElement();
    ((JavascriptExecutor) driver)
        .executeScript("document.body.innerHTML = " + quoteXPath(pageBody));
    driver.switchTo().defaultContent();
  }

  protected abstract ExpectedCondition<?> getAddCondition();

  public String getBaseId() {
    return baseId;
  }

  public void equellaFileUploader(URL file, String description) {
    attachmentButton.click();
    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.xpath("//iframe[contains(@id,'_ifr') and contains(@id,'mce_')]")));

    waitForElement(fileName);
    fileName.clear();
    fileName.sendKeys(description);
    waitForHiddenElement(fileUpload);
    fileUpload.sendKeys(getPathFromUrl(file));
    new SelectionCheckoutPage(context)
        .get()
        .returnSelection(
            ExpectWaiter.waiter(
                ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//iframe[contains(@id,'_ifr') and contains(@id,'mce_')]")),
                this));
    driver.switchTo().frame(getHtmliFrame());
    waiter.until(ExpectedConditions2.textToBePresentInElement(editorBody, description));
    driver.switchTo().defaultContent();
  }

  private class PageRow {
    private By pageBy;

    private WebElement getPageRow() {
      return getTable().findElement(pageBy);
    }

    private WebElement getRowElem(String xpath) {
      return getPageRow().findElement(By.xpath(xpath));
    }

    private WebElement getDeleteButton() {
      return getRowElem("td/a[@title='Delete']");
    }

    private WebElement getEditButton() {
      return getRowElem("td[@class='name']/a");
    }

    public PageRow(By by) {
      this.pageBy = by;
    }

    public T edit() {
      WaitingPageObject<T> waiter = AbstractAuthorWebPage.this.updateWaiter();
      getEditButton().click();
      return waiter.get();
    }

    public T delete() {
      WaitingPageObject<T> waiter = AbstractAuthorWebPage.this.updateWaiter();
      getDeleteButton().click();
      acceptConfirmation();
      return waiter.get();
    }
  }
}
