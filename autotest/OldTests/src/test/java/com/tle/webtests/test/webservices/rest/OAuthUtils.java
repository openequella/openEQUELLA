package com.tle.webtests.test.webservices.rest;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.oauth.OAuthSettingsPage;

public class OAuthUtils {
  public static OAuthClient createClient(PageContext context, OAuthClient client) {
    OAuthSettingsPage oauth = new OAuthSettingsPage(context).load();
    return oauth.addClient(client);
  }

  public static void deleteClient(PageContext context, String name) {
    OAuthSettingsPage oauth = new OAuthSettingsPage(context).load();
    oauth.deleteClient(name);
  }

  public static void deleteTokens(PageContext context) {}
}
