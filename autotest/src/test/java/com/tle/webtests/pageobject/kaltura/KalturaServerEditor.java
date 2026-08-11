package com.tle.webtests.pageobject.kaltura;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class KalturaServerEditor extends AbstractPage<KalturaServerEditor> {
  @FindBy(id = "kse_ep")
  private WebElement endpointField;

  @FindBy(id = "kse_pid")
  private WebElement partnerIdField;

  @FindBy(id = "kse_unlockPartnerIdButton")
  private WebElement unlockPidButton;

  @FindBy(id = "kse_as")
  private WebElement adminSecretField;

  @FindBy(id = "kse_us")
  private WebElement userSecretField;

  @FindBy(id = "kse_testButton")
  private WebElement testButton;

  @FindBy(id = "kse_saveButton")
  private WebElement saveButton;

  @FindBy(id = "connectionstatus")
  private WebElement connectionStatus;

  public KalturaServerEditor(PageContext context, boolean add) {
    super(
        context,
        By.xpath(
            "//h2[text()='" + (add ? "Add new Kaltura server" : "Edit Kaltura server") + "']"));
  }

  public void setEndpoint(String endpoint) {
    endpointField.clear();
    endpointField.sendKeys(endpoint);
  }

  public void setPartnerId(String id) {
    if (isPresent(unlockPidButton) && unlockPidButton.isEnabled()) {
      unlockPidButton.click();
      driver.switchTo().alert().accept();
    }
    partnerIdField.clear();
    partnerIdField.sendKeys(id);
  }

  public void setAdminSecret(String secret) {
    adminSecretField.clear();
    adminSecretField.sendKeys(secret);
  }

  public void setUserSecret(String secret) {
    userSecretField.clear();
    userSecretField.sendKeys(secret);
  }

  public KalturaServerEditor saveWithErrors() {
    saveButton.click();
    return get();
  }

  public KalturaServerList saveSuccessful() {
    saveButton.click();
    return new KalturaServerList(context).get();
  }

  public void test() {
    WaitingPageObject<KalturaServerEditor> ajaxUpdateExpect =
        ajaxUpdateExpect(connectionStatus, connectionStatus.findElement(By.xpath("div")));
    testButton.click();
    ajaxUpdateExpect.get();
  }

  public boolean isWaiting() {
    return isPresent(By.className("waiting"));
  }

  public boolean isFailure() {
    return isPresent(By.xpath("//div[@id='connectionstatus']/div[@class='ctrlinvalid']"));
  }

  public boolean isSuccessful() {
    return isPresent(By.className("success"));
  }

  public String getError() {
    return driver.findElement(By.className("ctrlinvalidmessage")).getText();
  }

  public String getTestStatus() {
    return driver.findElement(By.id("connectionstatus")).getText();
  }
}
