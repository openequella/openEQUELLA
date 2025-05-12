package com.tle.webtests.test.webservices.rest;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.oauth.OAuthSettingsPage;
import com.tle.webtests.pageobject.portal.MenuSection;

public class OAuthUtils {
  public static OAuthClient createClient(PageContext context, OAuthClient client) {
    OAuthSettingsPage oauth = openOAuthSettingsPage(context);
    return oauth.addClient(client);
  }

  public static void deleteClient(PageContext context, String name) {
    OAuthSettingsPage oauth = openOAuthSettingsPage(context);
    oauth.deleteClient(name);
  }

  public static void deleteTokens(PageContext context) {}

  private static OAuthSettingsPage openOAuthSettingsPage(PageContext context) {
    MenuSection menu = new MenuSection(context);
    SettingsPage settingsPage = menu.clickMenu("Settings", new SettingsPage(context));
    return settingsPage.oauthSettingsPage();
  }
}
