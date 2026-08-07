package com.tle.webtests.pageobject.oauth;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.test.webservices.rest.OAuthClient;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class OAuthSettingsPage extends AbstractPage<OAuthSettingsPage> {
  @FindBy(id = "soa_ac")
  private WebElement addLink;

  public OAuthSettingsPage(PageContext context) {
    super(context, By.id("clientsDiv"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/oauthadmin.do");
  }

  public OAuthClientEditorPage newClient() {
    addLink.click();
    return new OAuthClientEditorPage(context).get();
  }

  public OAuthClient addClient(OAuthClient client) {
    addLink.click();
    OAuthClientEditorPage editPage = new OAuthClientEditorPage(context).get();

    editPage.setName(client.getName());
    editPage.setClientId(client.getClientId());
    client.setSecret(editPage.getSecret());

    if (client.getUsername() != null) {
      editPage.setFlow("ccg");
      editPage.setUser(client.getUsername());
    } else if (client.getUrl() != null) {
      editPage.setFlow("acg");
      editPage.setRedirectUrl(client.getUrl());
    } else {
      editPage.setFlow("ig");
      editPage.setDefaultUrl();
    }

    editPage.save();
    return client;
  }

  public OAuthSettingsPage deleteClient(String name) {
    WebElement clientRow = driver.findElement(getRowXpath(name));
    WebElement element = clientRow.findElement(By.xpath(".//a[text()='Delete']"));
    WaitingPageObject<OAuthSettingsPage> waiter = removalWaiter(clientRow);
    element.click();
    acceptConfirmation();
    return waiter.get();
  }

  private By getRowXpath(String name) {
    return By.xpath(
        "id('clientsDiv')//tr[td[@class='name' and normalize-space(text())="
            + quoteXPath(name)
            + "]]");
  }

  public OAuthClientEditorPage editClient(String name) {
    WebElement clientRow = driver.findElement(getRowXpath(name));
    clientRow.findElement(By.xpath(".//a[text()='Edit']")).click();
    return new OAuthClientEditorPage(context).get();
  }

  public boolean clientExists(String name) {
    return isPresent(getRowXpath(name));
  }
}
