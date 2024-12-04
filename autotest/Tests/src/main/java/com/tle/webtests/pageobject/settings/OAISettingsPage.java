package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OAISettingsPage extends AbstractPage<OAISettingsPage> {
  public static final String OAI_SETTINGS_HEADER =
      "OAI identifier"; // original properties key: oaiidentifier.title

  @FindBy(id = "_oaiSchemeText")
  private WebElement oaiSchemeTextBox;

  @FindBy(id = "_namespaceText")
  private WebElement namespaceTextBox;

  @FindBy(id = "_emailText")
  private WebElement emailTextBox;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  public OAISettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(OAI_SETTINGS_HEADER) + ']'));
  }

  public String getOAIScheme() {
    return oaiSchemeTextBox.getAttribute("value");
  }

  public OAISettingsPage setOAIScheme(String oaiScheme) {
    oaiSchemeTextBox.clear();
    oaiSchemeTextBox.sendKeys(oaiScheme);
    return get();
  }

  public String getNamespace() {
    return namespaceTextBox.getAttribute("value");
  }

  public OAISettingsPage setNamespace(String namespace) {
    namespaceTextBox.clear();
    namespaceTextBox.sendKeys(namespace);
    return get();
  }

  public String getEmail() {
    return emailTextBox.getAttribute("value");
  }

  public OAISettingsPage setEmail(String email) {
    emailTextBox.clear();
    emailTextBox.sendKeys(email);
    return get();
  }

  public OAISettingsPage save() {
    saveButton.click();
    return new OAISettingsPage(context).get();
  }
}
