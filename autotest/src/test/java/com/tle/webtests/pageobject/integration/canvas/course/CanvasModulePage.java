package com.tle.webtests.pageobject.integration.canvas.course;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CanvasModulePage extends AbstractCanvasCoursePage<CanvasModulePage> {
  @FindBy(xpath = "//button[contains(@class, 'add_module_link')]")
  private WebElement createModuleButton;

  @FindBy(id = "context_module_name")
  private WebElement moduleNameField;

  public CanvasModulePage(PageContext context) {
    super(context, By.id("context_modules"));
  }

  public CanvasAddModuleItemDialog addModuleItem(String moduleName) {
    WebElement moduleDiv = driver.findElement(getModuleBy(moduleName));
    WebElement addContentButton =
        moduleDiv.findElement(By.xpath(".//button[contains(@class,'add_module_item_link')]"));
    dumbCanvasClickHack(addContentButton);
    return new CanvasAddModuleItemDialog(context).get();
  }

  public boolean moduleItemExists(String moduleName, String moduleItemName) {
    return isPresent(driver.findElement(getModuleBy(moduleName)), By.linkText(moduleItemName));
  }

  private By getModuleBy(String moduleName) {
    return By.xpath("//div[@aria-label='" + moduleName + "']");
  }

  public void deleteModuleItemsNamed(String moduleName, String itemName) {
    WebElement moduleDiv = driver.findElement(getModuleBy(moduleName));
    while (moduleItemExists(moduleName, itemName)) {
      WebElement adminOpener =
          moduleDiv.findElement(
              By.xpath(
                  ".//span[text() = "
                      + quoteXPath(itemName)
                      + "]/../../..//div[@class='ig-admin']/div/a[contains(@class,"
                      + " 'al-trigger')]"));
      dumbCanvasClickHack(adminOpener);
      WebElement activeRemoveButton =
          moduleDiv.findElement(
              By.xpath(".//ul[@aria-expanded='true']/li/a[contains(@class,'delete_item_link')]"));
      waiter.until(ExpectedConditions.elementToBeClickable(activeRemoveButton));
      activeRemoveButton.click();
      acceptConfirmation();
      removalWaiter(adminOpener).get();
    }
  }

  public void waitForModuleItem(String moduleName, String moduleItem) {
    waitForElement(getModuleBy(moduleName));
    waitForElement(driver.findElement(getModuleBy(moduleName)), By.linkText(moduleItem));
  }

  public void deleteModule(String moduleName) {
    if (isPresent(getModuleBy(moduleName))) {
      WebElement moduleDiv = driver.findElement(getModuleBy(moduleName));
      WebElement adminOpener =
          moduleDiv.findElement(
              By.xpath(".//div[@class='ig-header-admin']/button[@aria-label='Manage module']"));
      dumbCanvasClickHack(adminOpener);
      WebElement activeDeleteButton =
          moduleDiv.findElement(
              By.xpath(".//ul[@aria-expanded='true']/li/a[contains(@class,'delete_module_link')]"));
      waiter.until(ExpectedConditions.elementToBeClickable(activeDeleteButton));
      activeDeleteButton.click();
      acceptConfirmation();
      removalWaiter(moduleDiv).get();
    }
  }

  public void addModule(String moduleName) {
    waitForElement(createModuleButton);
    dumbCanvasClickHack(createModuleButton);
    waitForElement(moduleNameField);
    moduleNameField.sendKeys(moduleName);
    dumbCanvasClickHack(
        driver.findElement(
            By.xpath(
                "//div[@class='ui-dialog-buttonset']/button[contains(@class,'button_type_submit')]")));
    waitForElement(getModuleBy(moduleName));
  }
}
