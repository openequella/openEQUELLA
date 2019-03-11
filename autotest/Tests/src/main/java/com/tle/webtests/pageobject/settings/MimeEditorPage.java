package com.tle.webtests.pageobject.settings;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.net.URL;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class MimeEditorPage extends AbstractPage<MimeEditorPage> {
  @FindBy(id = "det_description")
  private WebElement descField;

  @FindBy(id = "det_type")
  private WebElement mimeTypeField;

  @FindBy(id = "det_newExtension")
  private WebElement extField;

  @FindBy(id = "det_addExtensionButton")
  private WebElement addExtButton;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  @FindBy(id = "mei_iu")
  private WebElement fileUpload;

  @FindBy(id = "mei_ri")
  private WebElement restoreIcon;

  @FindBy(id = "viewers")
  private WebElement viewersDiv;

  @FindBy(id = "dv_dvt")
  private WebElement viewersTable;

  public MimeEditorPage(PageContext context) {
    super(context, By.xpath("//h3[text()='Viewers']"));
  }

  public MimeEditorPage setDetails(String desc, String mimeType, String[] ext) {
    if (!Check.isEmpty(desc)) {
      descField.clear();
      descField.sendKeys(desc);
    }

    if (!Check.isEmpty(mimeType)) {
      mimeTypeField.click();
      mimeTypeField.clear();
      mimeTypeField.sendKeys(mimeType);
      descField.click();
      WaitingPageObject<MimeEditorPage> ajaxUpdate =
          ajaxUpdateExpect(waitForElement(viewersDiv), waitForElement(viewersTable));
      ((JavascriptExecutor) driver)
          .executeScript("$(arguments[0]).blur();", waitForElement(mimeTypeField));
      ajaxUpdate.get();
    }

    if (!Check.isEmpty(ext)) {
      for (int i = 0; i < ext.length; i++) {
        extField.clear();
        extField.sendKeys(ext[i]);
        addExtButton.click();
      }
    }
    return get();
  }

  public MimeSearchPage save() {
    saveButton.click();
    return new MimeSearchPage(context).get();
  }

  public MimeEditorPage saveWithError() {
    saveButton.click();
    return visibilityWaiter(driver, By.className("ctrlinvalidmessage")).get();
  }

  public MimeEditorPage selectTextExtractor(String extractor, boolean check) {
    WebElement element =
        driver.findElement(
            By.xpath(
                "//div[@id='extractors']//td[text()=" + quoteXPath(extractor) + "]/..//input"));
    if (element.isSelected() != check) {
      element.click();
    }
    return get();
  }

  private WebElement getIcon() {
    return driver.findElement(By.xpath("//div[@class='control']/img"));
  }

  public MimeEditorPage uploadIcon(URL icon) {
    ExpectedCondition<Boolean> iconUpdated = ExpectedConditions2.updateOfElement(getIcon());
    waitForHiddenElement(fileUpload);
    fileUpload.sendKeys(getPathFromUrl(icon));
    waiter.until(iconUpdated);
    return get();
  }

  public MimeEditorPage restoreIcon() {
    ExpectedCondition<Boolean> iconUpdated = ExpectedConditions2.updateOfElement(getIcon());
    restoreIcon.click();
    waiter.until(iconUpdated);
    return get();
  }
}
