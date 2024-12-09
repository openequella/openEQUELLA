package com.tle.webtests.pageobject.integration.canvas.course.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class CanvasAddNewAppDialog extends AbstractPage<CanvasAddNewAppDialog> {
  @FindBy(xpath = "//div[@class ='ConfigurationsTypeSelector']/div/select")
  private WebElement toolType;

  @FindBy(xpath = "//input[@placeholder='Name']")
  private WebElement nameField;

  @FindBy(xpath = "//input[@placeholder='Consumer key']")
  private WebElement keyField;

  @FindBy(xpath = "//input[@placeholder='Shared Secret']")
  private WebElement secretField;

  @FindBy(id = "xml")
  private WebElement configXMLField;

  @FindBy(xpath = "//button[text()='Submit']")
  private WebElement saveButton;

  private Select toolTypeDropdown;

  public CanvasAddNewAppDialog(PageContext context) {
    super(context, By.className("ConfigurationFormManual"));
    toolTypeDropdown = new Select(toolType);
  }

  public WaitingPageObject<CanvasAppsTab> addByPasting(
      String name,
      String consumerKey,
      String consumerSecret,
      String configXML,
      WaitingPageObject<CanvasAppsTab> returnTo) {

    toolTypeDropdown.selectByValue("xml");
    nameField.sendKeys(name);
    keyField.sendKeys(consumerKey);
    secretField.sendKeys(consumerSecret);
    configXMLField.sendKeys(configXML);
    saveButton.click();

    return returnTo.get();
  }
}
