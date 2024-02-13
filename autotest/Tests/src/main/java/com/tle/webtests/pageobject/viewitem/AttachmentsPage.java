package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionStatusPage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AttachmentsPage extends AbstractPage<AttachmentsPage> {
  @FindBy(id = "sc_attachments_browse")
  private WebElement attachmentsList;

  @FindBy(id = "sc_attachments_reorderAttachments")
  private WebElement reorderLink;

  @FindBy(className = "overlay")
  private WebElement reorderOverlay;

  @FindBy(className = "modal-save")
  private WebElement modalSave;

  @FindBy(id = "sc_attachments_browse")
  private WebElement firstAttachmentsList;

  @FindBy(id = "sc_attachments2_browse")
  private WebElement secondAttachmentsList;

  @FindBy(id = "sc_attachments3_browse")
  private WebElement thirdAttachmentsList;

  public AttachmentsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return attachmentsList;
  }

  public boolean attachmentExists(String description) {
    return isPresent(attachmentsList, attachmentBy(description));
  }

  public boolean existInFirstAttachmentList(String description) {
    return isPresent(firstAttachmentsList, attachmentBy(description));
  }

  public boolean existInSecondAttachmentList(String description) {
    return isPresent(secondAttachmentsList, attachmentBy(description));
  }

  public boolean existInThirdAttachmentsList(String description) {
    return isPresent(thirdAttachmentsList, attachmentBy(description));
  }

  public boolean attachmentSelectExists(String description) {
    return isPresent(
        attachmentsList, new ByChained(attachmentBy(description), By.tagName("button")));
  }

  public boolean folderExists(String description) {
    return isPresent(attachmentsList, folderBy(description));
  }

  public <T extends PageObject> T viewAttachment(String description, WaitingPageObject<T> page) {
    attachmentRow(description).findElement(By.tagName("a")).click();
    return page.get();
  }

  /**
   * @param description The text of the link attachment to click on
   * @param elementToFind The element to find on the external page
   */
  public void viewLinkAttachment(String description, By elementToFind) {
    attachmentRow(description).findElement(By.tagName("a")).click();
    ExpectWaiter.waiter(
        ExpectedConditions.visibilityOf(elementToFind.findElement(context.getDriver())), this);
  }

  public int attachmentCount() {
    return attachmentsList.findElements(By.xpath("//li[contains(@class,'attachmentrow')]")).size();
  }

  public List<String> attachmentOrder() {
    List<String> rv = new ArrayList<String>();
    for (WebElement row : attachmentsList.findElements(By.xpath("li"))) {
      WebElement link = row.findElement(By.xpath(".//a[1]"));
      // FIXME: what if it's a folder? or dead link?
      rv.add(link.getText());
    }
    return rv;
  }

  private WebElement attachmentRow(String description) {
    return attachmentsList.findElement(attachmentBy(description));
  }

  private By attachmentBy(String description) {
    return By.xpath("li[div/a[normalize-space(text())=" + quoteXPath(description) + "]]");
  }

  private WebElement attachmentRowClone(String description) {
    return attachmentsList.findElement(attachmentCloneBy(description));
  }

  private By attachmentCloneBy(String description) {
    return By.xpath(
        "li[div/a[normalize-space(text())="
            + quoteXPath(description)
            + "] and contains(@class,'clone')]");
  }

  private By activeAttachmentBy(String description) {
    return By.xpath(
        "li[not(contains(@class, 'inactive')) and .//a[normalize-space(text())="
            + quoteXPath(description)
            + "]]");
  }

  private By folderBy(String description) {
    return By.xpath("li[.//span[normalize-space(text())=" + quoteXPath(description) + "]]");
  }

  public String attachmentDetails(String description) {
    WebElement row = attachmentRow(description);
    if (row.getAttribute("class").contains("inactive")) {
      // You cannot just call click on the row, as it may be clicking on the actual attachment link
      // instead.
      // Note the yOffset is negative, as despite their javadoc saying the coord are top-left based,
      // it's actually a bald-faced lie, and the position is relative to the center of the element.
      new Actions(driver)
          .moveToElement(((WrapsElement) row).getWrappedElement(), 0, -13)
          .click()
          .build()
          .perform();
      waiter.until(
          ExpectedConditions2.visibilityOfElementLocated(
              attachmentsList, activeAttachmentBy(description)));
    }

    List<WebElement> meta =
        row.findElements(By.xpath(".//div[normalize-space(@class)='attachments-meta']"));
    for (WebElement ele : meta) {
      if ((ele).isDisplayed()) {
        return ele.getText();
      }
    }
    return "";
  }

  public AttachmentsPage selectAttachment(String description) {
    return selectAttachment(description, updateWaiter());
  }

  public <T extends PageObject> T selectAttachment(
      String description, WaitingPageObject<T> returnTo) {
    WebElement row = attachmentRow(description);
    row.findElement(By.tagName("button")).click();
    return returnTo.get();
  }

  public void selectAttachmentStructured(String description) {
    WebElement row = attachmentRow(description);
    // TODO add waiter
    row.findElement(By.tagName("button")).click();
  }

  public SelectionCheckoutPage selectSingleAttachment(String description) {
    WebElement row = attachmentRow(description);
    row.findElement(By.tagName("button")).click();
    return new SelectionCheckoutPage(context).get();
  }

  public <T extends PageObject> T selectAttachmentMultiple(
      String description, WaitingPageObject<T> returnTo) {
    SelectionStatusPage status = new SelectionStatusPage(context).get();
    return selectAttachment(description, status.updateWaiter())
        .finishSelections()
        .returnSelection(returnTo);
  }

  public <T extends PageObject> T selectPackage(WaitingPageObject<T> returnTo) {
    SelectionStatusPage status = new SelectionStatusPage(context).get();
    WaitingPageObject<SelectionStatusPage> waiter = status.updateWaiter();
    driver.findElement(By.id("sc_attachments_selectPackageButton")).click();
    return waiter.get().finishSelections().returnSelection(returnTo);
  }

  public <T extends PageObject> T viewFullscreen(WaitingPageObject<T> targetPage) {
    driver.findElement(By.id("sc_attachments_fullScreenLink")).click();
    return targetPage.get();
  }

  public PackageViewer viewFullscreen() {
    driver.findElement(By.id("sc_attachments_fullScreenLink")).click();
    return new PackageViewer(context).get();
  }

  /**
   * @param index the first attachment section index start with an empty string, second section is
   *     "2", third is "3" and so forth
   * @return
   */
  public PackageViewer chooseFullscreenToView(String index) {
    WebElement link = driver.findElement(By.id("sc_attachments" + index + "_fullScreenLink"));
    // When there is resource crunch, the selenium `click` function become unstable, thus use JS to
    // trigger the action.
    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link);
    return new PackageViewer(context).get();
  }

  public boolean attachmentIsPreview(String description) {
    return isPresent(
        attachmentsList, new ByChained(attachmentBy(description), By.className("preview-tag")));
  }

  public String getAttachmentURL(String description) {
    return attachmentRow(description).findElement(By.xpath("./div[1]/a")).getAttribute("href");
  }

  public void startReorder() {
    WaitingPageObject<AttachmentsPage> waiter =
        ExpectWaiter.waiter(ExpectedConditions2.presenceOfElement(reorderOverlay), this);
    reorderLink.click();
    waiter.get();
  }

  public void reorder(String description, boolean up) {
    WebElement row = attachmentRowClone(description);
    if (up) {
      row.findElement(By.className("moveup")).click();
    } else {
      row.findElement(By.className("movedown")).click();
    }
  }

  public void finishReorder() {
    modalSave.click();
  }
}
