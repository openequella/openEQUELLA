package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GoogleSettingsPage extends AbstractPage<GoogleSettingsPage> {

  @FindBy(id = "_g")
  private WebElement accountIdField;

  @FindBy(id = "_save")
  private WebElement saveButton;

  public GoogleSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()='Google Analytics']"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/googleAnalyticsPage.do");
  }

  public void setAccountId(String accountId) {
    clearAccountId();
    accountIdField.sendKeys(accountId);
  }

  public void clearAccountId() {
    accountIdField.clear();
  }

  public GoogleSettingsPage save() {
    saveButton.click();
    return ReceiptPage.waiter("Successfully saved settings", this).get();
  }

  public boolean isTrackingEnabled() {
    WebElement trackerStatus =
        driver.findElement(
            By.xpath(
                "//div[@class='settingRow' and ./div[@class='settingLabel' and "
                    + "normalize-space(label/text())='Tracking status:']]//div[@class='control']/span"));
    return trackerStatus.getText().trim().equals("Enabled");
  }

  public boolean isTrackingTagPresent() {
    if (!usingNewUI()) {
      // Old UI puts this directly in the head
      List<WebElement> tagPresent =
          driver.findElements(
              By.xpath(
                  "/html/head/script[@src='https://www.googletagmanager.com/gtag/js?id=test']"));
      return tagPresent.size() > 0;
    } else {
      // New UI puts this script into the body via React GA package.
      // Then if it returns valid info from Google, then it goes into the head.
      // We can't use a valid Google Analytics ID for this,
      // so just check for the script in the body.
      List<WebElement> tagPresent =
          driver.findElements(
              By.xpath(
                  "/html/body/script[@src='https://www.googletagmanager.com/gtag/js?id=test']"));
      return tagPresent.size() > 0;
    }
  }
}
