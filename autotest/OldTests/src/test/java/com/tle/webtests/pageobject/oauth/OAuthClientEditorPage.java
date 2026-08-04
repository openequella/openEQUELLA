package com.tle.webtests.pageobject.oauth;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OAuthClientEditorPage extends AbstractPage<OAuthClientEditorPage> {
  @FindBy(xpath = "id('oace_n')/div/input")
  private WebElement nameField;

  @FindBy(id = "oace_i")
  private WebElement clientIdField;

  @FindBy(id = "oace_cs")
  private WebElement clientSecretField;

  @FindBy(id = "oace_sf")
  private WebElement selectFlow;

  @FindBy(id = "oace_r")
  private WebElement redirectUrlField;

  @FindBy(id = "oace_su")
  private WebElement selectUserButton;

  @FindBy(id = "oace_sv")
  private WebElement saveButton;

  @FindBy(id = "oace_cl")
  private WebElement cancelButton;

  @FindBy(id = "userAjaxDiv")
  private WebElement userAjaxDiv;

  @FindBy(id = "flowAjaxDiv")
  private WebElement flowAjaxDiv;

  @FindBy(id = "oace_rs")
  private WebElement regenButton;

  @FindBy(className = "ctrlinvalidmessage")
  private WebElement invalidMessage;

  @FindBy(id = "oace_cu2_0")
  private WebElement defaultUrlBox;

  @FindBy(id = "clientSecretDiv")
  private WebElement clientSecretDiv;

  @FindBy(id = "oace_tv")
  private WebElement validityField;

  public OAuthClientEditorPage(PageContext context) {
    super(context, By.xpath("//h2[contains(text(), 'OAuth client')]"));
  }

  /** Options are: acg, ig, ccg */
  public OAuthClientEditorPage setFlow(String flow) {
    WaitingPageObject<OAuthClientEditorPage> ajaxUpdate = ajaxUpdate(flowAjaxDiv);
    new EquellaSelect(context, selectFlow).selectByValue(flow);
    return ajaxUpdate.get();
  }

  public String getSelectedFlow() {
    return new EquellaSelect(context, selectFlow).getSelectedValue();
  }

  public OAuthClientEditorPage setName(String name) {
    nameField.clear();
    nameField.sendKeys(name);
    return this;
  }

  public String getName() {
    return nameField.getAttribute("value");
  }

  public OAuthClientEditorPage setClientId(String clientId) {
    clientIdField.clear();
    clientIdField.sendKeys(clientId);
    return this;
  }

  public String getClientId() {
    return clientIdField.getAttribute("value");
  }

  public OAuthClientEditorPage setRedirectUrl(String redirectUrl) {
    redirectUrlField.clear();
    redirectUrlField.sendKeys(redirectUrl);
    return this;
  }

  public OAuthClientEditorPage setUser(String username) {
    WaitingPageObject<OAuthClientEditorPage> ajaxUpdate = ajaxUpdate(userAjaxDiv);
    selectUserButton.click();
    SelectUserDialog dialog = new SelectUserDialog(context, "oace_sud").get();
    return dialog.search(username).selectAndFinish(username, ajaxUpdate);
  }

  public OAuthSettingsPage save() {
    saveButton.click();
    return new OAuthSettingsPage(context).get();
  }

  public OAuthClientEditorPage saveWithErrors() {
    saveButton.click();
    return visibilityWaiter(invalidMessage).get();
  }

  public OAuthSettingsPage cancel() {
    cancelButton.click();
    return new OAuthSettingsPage(context).get();
  }

  public String getSecret() {
    return clientSecretField.getText();
  }

  public OAuthClientEditorPage regenerateSecret() {
    WaitingPageObject<OAuthClientEditorPage> ajaxUpdateExpect =
        ajaxUpdateExpect(clientSecretDiv, clientSecretField);
    regenButton.click();
    acceptConfirmation();
    return ajaxUpdateExpect.get();
  }

  public boolean canSetChooseUrl() {
    return isPresent(defaultUrlBox);
  }

  public boolean canSetUrl() {
    return isPresent(redirectUrlField);
  }

  public boolean canSetUser() {
    return isPresent(selectUserButton);
  }

  public boolean isDefaultUrl() {
    return !isPresent(redirectUrlField);
  }

  private boolean isControlInvalid(WebElement we) {
    return isControlInvalid(we, null);
  }

  private boolean isControlInvalid(WebElement we, String selector) {
    List<WebElement> invalidControls =
        driver.findElements(By.xpath("//div[@class='control ctrlinvalid']"));
    final String elementId = we.getAttribute("id");
    for (WebElement control : invalidControls) {
      if (StringUtils.isNotEmpty(elementId) && isPresent(control, By.id(elementId))) {
        return true;
      }
      if (selector != null && isPresent(control, By.xpath(selector))) {
        return true;
      }
    }
    return false;
  }

  public boolean isNameError() {
    return isControlInvalid(nameField, ".//div[@id = 'oace_n']/div/input");
  }

  public boolean isFlowError() {
    return isControlInvalid(selectFlow);
  }

  public boolean isUrlError() {
    return isControlInvalid(redirectUrlField);
  }

  public boolean isClientIdError() {
    return isControlInvalid(clientIdField);
  }

  public boolean isUserError() {
    return isControlInvalid(selectUserButton);
  }

  public String getUser() {
    return selectUserButton.findElement(By.xpath("preceding-sibling::span")).getAttribute("title");
  }

  public OAuthClientEditorPage setDefaultUrl() {
    if (!defaultUrlBox.isSelected()) {
      WaitingPageObject<OAuthClientEditorPage> removalWaiter = removalWaiter(redirectUrlField);
      defaultUrlBox.click();
      return removalWaiter.get();
    }
    return this;
  }

  public OAuthClientEditorPage setValidity(int validity) {
    validityField.clear();
    validityField.sendKeys(String.valueOf(validity));
    return this;
  }
}
