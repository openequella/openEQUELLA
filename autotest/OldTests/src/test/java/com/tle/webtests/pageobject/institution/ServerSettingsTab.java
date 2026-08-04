package com.tle.webtests.pageobject.institution;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ServerSettingsTab extends InstitutionTab<ServerSettingsTab> {
  @FindBy(id = "isserver_message_message")
  private WebElement serverMessage;

  @FindBy(id = "isserver_message_enabled")
  private WebElement serverMessageEnabled;

  @FindBy(id = "isserver_message_save")
  private WebElement serverMessageSave;

  /**
   * The id of the server message element which displays the message to users. (Different from the
   * `serverMessage` WebElement above which is the input field for setting the message.)
   */
  private static final String SERVER_MESSAGE_ID = "servermessage";

  public ServerSettingsTab(PageContext context) {
    super(context, "Settings", "Server message");
  }

  public void setServerMessage(String message) {
    serverMessage.clear();
    serverMessage.sendKeys(message);
    setServerMessageEnabled(true);

    serverMessageSave.click();
    // The only option we have to validate the save completed is to check the displayed message.
    waiter.until(driver -> driver.findElement(By.id(SERVER_MESSAGE_ID)).getText().equals(message));
  }

  public void disableServerMessage() {
    serverMessage.clear();
    setServerMessageEnabled(false);

    serverMessageSave.click();
    // Wait for the server message to disappear, meaning the save has completed.
    waiter.until(driver -> driver.findElements(By.id(SERVER_MESSAGE_ID)).isEmpty());
  }

  private void setServerMessageEnabled(boolean enabled) {
    if (isServerMessageEnabled() != enabled) {
      serverMessageEnabled.click();

      // Wait for the checkbox state to change - to avoid subsequent race conditions.
      waiter.until(driver -> isServerMessageEnabled() == enabled);
    }
  }

  private boolean isServerMessageEnabled() {
    return !Check.isEmpty(serverMessageEnabled.getAttribute("checked"));
  }
}
