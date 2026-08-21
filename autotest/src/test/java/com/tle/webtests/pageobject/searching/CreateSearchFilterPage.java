package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CreateSearchFilterPage extends AbstractPage<CreateSearchFilterPage> {

  @FindBy(id = "esf_name")
  private WebElement nameBox;

  @FindBy(id = "esf_saveButton")
  private WebElement save;

  @FindBy(id = "esf_cancelButton")
  private WebElement cancel;

  public CreateSearchFilterPage(PageContext context) {
    super(context, By.xpath("//h2[text()='Create new search filter']"));
  }

  public CreateSearchFilterPage(PageContext context, boolean edit) {
    super(context, By.xpath("//h2[text()='Edit search filter']"));
  }

  public CreateSearchFilterPage setName(String name) {
    nameBox.clear();
    nameBox.sendKeys(name);
    return get();
  }

  public CreateSearchFilterPage selectMimeType(String type, boolean select) {
    WebElement check = driver.findElement(By.xpath(".//input[@value=" + quoteXPath(type) + "]"));
    if (check.isSelected() != select) {
      check.click();
    }
    return get();
  }

  public CreateSearchFilterPage selectMimeTypes(List<String> types) {
    for (String type : types) {
      selectMimeType(type, true);
    }
    return get();
  }

  public void saveWithErrors() {
    save.click();
    waiter.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class, 'ctrlinvalid')]")));
  }

  public SearchSettingsPage save() {
    save.click();
    return new SearchSettingsPage(context).get();
  }

  public SearchSettingsPage cancel() {
    cancel.click();
    return new SearchSettingsPage(context).get();
  }

  // These xpaths suck but are non-message text specific.
  public boolean nameValidationExists() {
    return isPresent(By.xpath("//div[contains(@class, 'ctrlinvalid')]/input[@id='esf_name']"));
  }

  public boolean mimeTypeValidationExists() {
    return isPresent(
        By.xpath(
            "//div[contains(@class, 'ctrlinvalid')]/div[contains(@class, 'mimeTypesContainer')]"));
  }
}
