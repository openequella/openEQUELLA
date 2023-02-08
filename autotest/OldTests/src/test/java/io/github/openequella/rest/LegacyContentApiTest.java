package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.ActiveCachingPage;
import com.tle.webtests.pageobject.settings.ContentRestrictionsPage;
import com.tle.webtests.pageobject.settings.DateFormatSettingPage;
import com.tle.webtests.pageobject.settings.GoogleApiSettingsPage;
import com.tle.webtests.pageobject.settings.GoogleSettingsPage;
import com.tle.webtests.pageobject.settings.LoginSettingsPage;
import com.tle.webtests.pageobject.settings.ShortcutURLsSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Since most HTTP code return by legacy APIs are not credible (for example, it returns 403 code but
 * settings are still changed), thus this class extends `AbstractSessionTest` to use the `Page` to
 * check the test result.
 */
class MixedSessionRestTest extends AbstractSessionTest {
  protected final HttpClient httpClient = new HttpClient();
  protected final ObjectMapper mapper = new ObjectMapper();

  /** Login as a low privilege user. */
  public void loginRestWithNoACL() throws IOException {
    makeClientRequest(buildLoginMethod(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD));
  }

  public String getAuthApiEndpoint() {
    return getTestConfig().getInstitutionUrl() + "api/auth";
  }

  protected HttpMethod buildLogoutMethod() {
    final String logoutEndpoint = getAuthApiEndpoint() + "/logout";
    return new PutMethod(logoutEndpoint);
  }

  protected int makeClientRequest(HttpMethod method) throws IOException {
    return httpClient.executeMethod(method);
  }

  protected HttpMethod buildLoginMethod(String username, String password) {
    final String loginEndpoint = getAuthApiEndpoint() + "/login";
    final NameValuePair[] queryVals = {
      new NameValuePair("username", username), new NameValuePair("password", password)
    };
    final HttpMethod method = new PostMethod(loginEndpoint);
    method.setQueryString(queryVals);
    return method;
  }
}

@TestInstitution("rest")
public class LegacyContentApiTest extends MixedSessionRestTest {
  final String remoteCachingSettingsEndpoint = getAccessApiEndpoint("remotecaching.do");
  final String ContentRestrictionsSettingsEndpoint = getAccessApiEndpoint("contentrestrictions.do");
  final String GoogleApiSettingsEndpoint = getAccessApiEndpoint("googleapisettings.do");
  final String GoogleAnalyticsSettingsEndpoint = getAccessApiEndpoint("googleAnalyticsPage.do");
  final String shortcutSettingsEndpoint = getAccessApiEndpoint("shortcuturlssettings.do");
  final String dateFormatSettingsEndpoint = getAccessApiEndpoint("dateformatsettings.do");
  final String loginSettingsEndpoint = getAccessApiEndpoint("loginsettings.do");

  public LegacyContentApiTest() throws URISyntaxException {}

  public String getAccessApiEndpoint(String endpoint) throws URISyntaxException {
    return getTestConfig().getInstitutionUrl() + "api/content/submit/access/" + endpoint;
  }

  @BeforeMethod
  public void loginRestClient() throws IOException {
    loginRestWithNoACL();
  }

  @AfterMethod
  public void logoutRestClient() throws IOException {
    makeClientRequest(buildLogoutMethod());
  }

  private ActiveCachingPage logonToActiveCachingPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().activeCachingSettings();
  }

  private ContentRestrictionsPage logonToContentRestrictionsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().contentRestrictionsSettings();
  }

  private GoogleApiSettingsPage logonToGoogleApiSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().googleApiSettings();
  }

  private GoogleSettingsPage logonToGoogleAnalyticsSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().googleSettings();
  }

  private ShortcutURLsSettingsPage logonToShortcutURLsSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().shortcutURLsSettingsPage();
  }

  private DateFormatSettingPage logonToDateFormatSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().dateFormatSettingPage();
  }

  private LoginSettingsPage logonToLoginSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().loginSettings();
  }

  /** Add each NameValuePair value to JsonNode (wrapped with Array) and `post` it with body. */
  public int post(String endpoint, NameValuePair... params) throws IOException {
    final PostMethod method = new PostMethod(endpoint);

    ObjectNode payload = mapper.createObjectNode();
    Arrays.stream(params)
        .forEach(x -> payload.put(x.getName(), mapper.createArrayNode().add(x.getValue())));

    method.setRequestEntity(
        new StringRequestEntity(payload.toString(), "application/json", "UTF-8"));

    return makeClientRequest(method);
  }

  @Test(description = "Guest users are not allowed to access any content under `/access/` path")
  public void preventGuestVisitAccessPathTest() throws IOException {
    logoutRestClient();
    int statusCode =
        post(
            dateFormatSettingsEndpoint,
            new NameValuePair("event__", ".save"),
            new NameValuePair("_dateFormats", "format.exact"));
    assertEquals(
        statusCode, HttpStatus.SC_UNAUTHORIZED, "Guest still able to access `access` path.");
  }

  @Test(description = "User without permission shouldn't be able to edit remote caching")
  public void preventEditingRemoteCachingSettingsTest() throws IOException {
    post(
        remoteCachingSettingsEndpoint,
        new NameValuePair("event__", ".save"),
        new NameValuePair("_eu", "checked"));

    // make sure remote caching is not enabled.
    ActiveCachingPage page = logonToActiveCachingPage();
    assertEquals(page.getEnableUseChecked(), false);
  }

  @Test(description = "User without permission shouldn't be able to add banned file extensions")
  public void preventAddingBannedFileExtensionsSettingsTest() throws IOException {
    final String FILE_EXTENSION_NAME = "example";
    post(
        ContentRestrictionsSettingsEndpoint,
        new NameValuePair("event__", "$UP0$.addBannedExtension"),
        new NameValuePair("eventp__0", FILE_EXTENSION_NAME));

    // make sure the new entry is not added.
    ContentRestrictionsPage page = logonToContentRestrictionsPage();
    assertEquals(page.isExtPresent(FILE_EXTENSION_NAME), false);
  }

  @Test(description = "User without permission shouldn't be able to delete banned file extensions")
  public void preventDeletingBannedFileExtensionsSettingsTest() throws IOException {
    final String FILE_EXTENSION_NAME = "exe";
    post(
        ContentRestrictionsSettingsEndpoint,
        new NameValuePair("event__", "$UP1$.removeBannedExtension"),
        new NameValuePair("eventp__0", FILE_EXTENSION_NAME));

    // make sure the entry is not deleted.
    ContentRestrictionsPage page = logonToContentRestrictionsPage();
    assertEquals(page.isExtPresent(FILE_EXTENSION_NAME), true);
  }

  @Test(description = "User without permission shouldn't be able to remove user content quotas")
  public void preventDeletingUserQuotasSettingsTest() throws IOException {
    // try to delete the first item
    post(
        ContentRestrictionsSettingsEndpoint,
        new NameValuePair("event__", "$UP2$.removeUserQuota"),
        new NameValuePair("eventp__0", "0"));

    // make sure the quota is not deleted.
    ContentRestrictionsPage page = logonToContentRestrictionsPage();
    assertEquals(page.countUserQuotas(), 1);
  }

  @Test(description = "User without permission shouldn't be able to edit google api settings")
  public void preventEditingGoogleApiSettingsTest() throws IOException {
    final String API = "123";
    post(
        GoogleApiSettingsEndpoint,
        new NameValuePair("event__", ".save"),
        new NameValuePair("_apiKey", API));

    // make sure the api is not changed.
    logonToGoogleApiSettingsPage();
    assertEquals(getValueInId("_apiKey"), "");
  }

  @Test(description = "User without permission shouldn't be able to edit google analytics settings")
  public void preventEditingGoogleAnalyticsSettingsTest() throws IOException {
    final String ACCOUNT_ID = "123";
    post(
        GoogleAnalyticsSettingsEndpoint,
        new NameValuePair("event__", ".setup"),
        new NameValuePair("_g", ACCOUNT_ID));

    // make sure the account id is not changed.
    logonToGoogleAnalyticsSettingsPage();
    assertEquals(getValueInId("_g"), "");
  }

  @Test(description = "User without permission shouldn't be able to add shortcut url settings")
  public void preventAddingShortcutUrlSettingsTest() throws IOException {
    final String URL = "https://www.scarywebsite.com";

    post(
        shortcutSettingsEndpoint,
        new NameValuePair("event__", "$UP$<BODY>"),
        new NameValuePair("eventp__0", "_addShortcutUrlDialog.addShortcutUrl"),
        new NameValuePair("_addShortcutUrlDialog.showing", "true"),
        new NameValuePair("_addShortcutUrlDialog_shortcutText", "nope"),
        new NameValuePair("_addShortcutUrlDialog_urlText", URL));

    // make sure the shortcut url is not added.
    logonToShortcutURLsSettingsPage();
    assertEquals(isTextPresent(URL), false);
  }

  @Test(description = "User without permission shouldn't be able to delete shortcut url settings")
  public void preventDeletingShortcutUrlSettingsTest() throws IOException {
    final String NAME = "test-shortcut-name";
    post(
        shortcutSettingsEndpoint,
        new NameValuePair("event__", ".delete"),
        new NameValuePair("eventp__0", NAME));

    // make sure the shortcut url is still existing.
    logonToShortcutURLsSettingsPage();
    assertEquals(isTextPresent(NAME), true);
  }

  @Test(description = "User without permission shouldn't be able to edit date format settings")
  public void preventEditingDateformatSettingsTest() throws IOException {
    post(
        dateFormatSettingsEndpoint,
        new NameValuePair("event__", ".save"),
        new NameValuePair("_dateFormats", "format.exact"));

    // make sure the format is not `exact` type
    DateFormatSettingPage page = logonToDateFormatSettingsPage();
    assertEquals(page.isExactDateFormat(), false);
  }

  @Test(description = "User without permission shouldn't be able to edit login settings")
  public void preventEditingLoginSettingsTest() throws IOException {
    // save event without params will overwrite all available settings value to false.
    post(loginSettingsEndpoint, new NameValuePair("event__", ".save"));

    // make sure enable via ip is still enabled.
    LoginSettingsPage page = logonToLoginSettingsPage();
    assertEquals(page.isEnableViaIp(), true);
  }
}
