package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.settings.ActiveCachingPage;
import com.tle.webtests.pageobject.settings.ContentRestrictionsPage;
import com.tle.webtests.pageobject.settings.DateFormatSettingPage;
import com.tle.webtests.pageobject.settings.GoogleApiSettingsPage;
import com.tle.webtests.pageobject.settings.GoogleSettingsPage;
import com.tle.webtests.pageobject.settings.HarvesterSkipDrmPage;
import com.tle.webtests.pageobject.settings.LoginSettingsPage;
import com.tle.webtests.pageobject.settings.MailSettingsPage;
import com.tle.webtests.pageobject.settings.ShortcutURLsSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import java.io.IOException;
import java.util.Arrays;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Since most HTTP code return by legacy APIs are not credible (for example, it returns 403 code but
 * settings are still changed), this class extends `AbstractSessionTest` to use the `Page` to check
 * the test result.
 */
@TestInstitution("rest")
public class LegacyContentApiTest extends AbstractSessionTest {
  final HttpClient httpClient = new HttpClient();
  final ObjectMapper mapper = new ObjectMapper();
  final AuthHelper authHelper = new AuthHelper(getTestConfig().getInstitutionUrl());

  final String remoteCachingSettingsEndpoint = getAccessApiEndpoint("remotecaching.do");
  final String contentRestrictionsSettingsEndpoint = getAccessApiEndpoint("contentrestrictions.do");
  final String dateFormatSettingsEndpoint = getAccessApiEndpoint("dateformatsettings.do");
  final String googleApiSettingsEndpoint = getAccessApiEndpoint("googleapisettings.do");
  final String googleAnalyticsSettingsEndpoint = getAccessApiEndpoint("googleAnalyticsPage.do");
  final String harvesterSettingsEndpoint = getAccessApiEndpoint("harvesterskipdrmsettings.do");
  final String shortcutSettingsEndpoint = getAccessApiEndpoint("shortcuturlssettings.do");
  final String mailSettingsEndpoint = getAccessApiEndpoint("mailsettings.do");
  final String loginSettingsEndpoint = getAccessApiEndpoint("loginsettings.do");

  final NameValuePair sectionsSaveEvent = sectionsEvent(".save");

  public LegacyContentApiTest() {}

  private String getAccessApiEndpoint(String endpoint) {
    return getTestConfig().getInstitutionUrl() + "api/content/submit/access/" + endpoint;
  }

  private int makeClientRequest(HttpMethod method) throws IOException {
    return httpClient.executeMethod(method);
  }

  @BeforeMethod
  private void loginRestClient() throws IOException {
    // Login as a low privilege user.
    makeClientRequest(authHelper.buildLoginMethod(AUTOTEST_LOW_PRIVILEGE_LOGON, AUTOTEST_PASSWD));
  }

  @AfterMethod
  private void logoutRestClient() throws IOException {
    makeClientRequest(authHelper.buildLogoutMethod());
  }

  private ActiveCachingPage logonToActiveCachingSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().activeCachingSettings();
  }

  private ContentRestrictionsPage logonToContentRestrictionsSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().contentRestrictionsSettings();
  }

  private DateFormatSettingPage logonToDateFormatSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().dateFormatSettingPage();
  }

  private GoogleApiSettingsPage logonToGoogleApiSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().googleApiSettings();
  }

  private GoogleSettingsPage logonToGoogleAnalyticsSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().googleSettings();
  }

  private HarvesterSkipDrmPage logonToHarvesterSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().harvestSkipDrmSettings();
  }

  private ShortcutURLsSettingsPage logonToShortcutURLsSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().shortcutURLsSettingsPage();
  }

  private LoginSettingsPage logonToLoginSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().loginSettings();
  }

  private MailSettingsPage logonToMailSettingsPage() {
    logon(context, AUTOTEST_LOGON, AUTOTEST_PASSWD);
    return new SettingsPage(context).load().mailSettingsPage();
  }

  /** Add each NameValuePair value to JsonNode (wrapped with Array) and `post` it with body. */
  private int post(String endpoint, NameValuePair... params) throws IOException {
    final PostMethod method = new PostMethod(endpoint);

    ObjectNode payload = mapper.createObjectNode();
    Arrays.stream(params)
        .forEach(x -> payload.put(x.getName(), mapper.createArrayNode().add(x.getValue())));

    method.setRequestEntity(
        new StringRequestEntity(payload.toString(), "application/json", "UTF-8"));

    return makeClientRequest(method);
  }

  private NameValuePair sectionsEvent(String eventName) {
    return new NameValuePair("event__", eventName);
  }

  final NameValuePair sectionsEventParam0(String param) {
    return new NameValuePair("eventp__0", param);
  }

  private int editingRemoteCachingSettings() throws IOException {
    return post(
        remoteCachingSettingsEndpoint, sectionsSaveEvent, new NameValuePair("_eu", "checked"));
  }

  @Test(description = "Guest users are not allowed to access any content under `/access/` path")
  public void preventGuestVisitAccessPathTest() throws IOException {
    logoutRestClient();
    assertEquals(
        editingRemoteCachingSettings(),
        HttpStatus.SC_UNAUTHORIZED,
        "Guest still able to access `access` path.");
  }

  @Test(description = "User without permission shouldn't be able to edit remote caching")
  public void preventEditingRemoteCachingSettingsTest() throws IOException {
    editingRemoteCachingSettings();

    // make sure remote caching is not enabled.
    ActiveCachingPage page = logonToActiveCachingSettingsPage();
    assertFalse(page.getEnableUseChecked());
  }

  @Test(description = "User without permission shouldn't be able to add banned file extensions")
  public void preventAddingBannedFileExtensionsSettingsTest() throws IOException {
    final String FILE_EXTENSION_NAME = "example";
    post(
        contentRestrictionsSettingsEndpoint,
        sectionsEvent("$UP0$.addBannedExtension"),
        sectionsEventParam0(FILE_EXTENSION_NAME));

    // make sure the new entry is not added.
    ContentRestrictionsPage page = logonToContentRestrictionsSettingsPage();
    assertFalse(page.isExtPresent(FILE_EXTENSION_NAME));
  }

  @Test(description = "User without permission shouldn't be able to delete banned file extensions")
  public void preventDeletingBannedFileExtensionsSettingsTest() throws IOException {
    final String FILE_EXTENSION_NAME = "exe";
    post(
        contentRestrictionsSettingsEndpoint,
        sectionsEvent("$UP1$.removeBannedExtension"),
        sectionsEventParam0(FILE_EXTENSION_NAME));

    // make sure the entry is not deleted.
    ContentRestrictionsPage page = logonToContentRestrictionsSettingsPage();
    assertTrue(page.isExtPresent(FILE_EXTENSION_NAME));
  }

  @Test(description = "User without permission shouldn't be able to remove user content quotas")
  public void preventDeletingUserQuotasSettingsTest() throws IOException {
    // try to delete the first item
    post(
        contentRestrictionsSettingsEndpoint,
        sectionsEvent("$UP2$.removeUserQuota"),
        sectionsEventParam0("0"));

    // make sure the quota is not deleted.
    ContentRestrictionsPage page = logonToContentRestrictionsSettingsPage();
    assertEquals(page.countUserQuotas(), 1);
  }

  @Test(description = "User without permission shouldn't be able to edit date format settings")
  public void preventEditingDateformatSettingsTest() throws IOException {
    post(
        dateFormatSettingsEndpoint,
        sectionsSaveEvent,
        new NameValuePair("_dateFormats", "format.exact"));

    // make sure the format is not `exact` type
    DateFormatSettingPage page = logonToDateFormatSettingsPage();
    assertFalse(page.isExactDateFormat());
  }

  @Test(description = "User without permission shouldn't be able to edit google api settings")
  public void preventEditingGoogleApiSettingsTest() throws IOException {
    final String API = "123";
    post(googleApiSettingsEndpoint, sectionsSaveEvent, new NameValuePair("_apiKey", API));

    // make sure the api is not changed.
    logonToGoogleApiSettingsPage();
    assertEquals(getValueInId("_apiKey"), "");
  }

  @Test(description = "User without permission shouldn't be able to edit google analytics settings")
  public void preventEditingGoogleAnalyticsSettingsTest() throws IOException {
    final String ACCOUNT_ID = "123";
    post(
        googleAnalyticsSettingsEndpoint,
        sectionsEvent(".setup"),
        new NameValuePair("_g", ACCOUNT_ID));

    // make sure the account id is not changed.
    logonToGoogleAnalyticsSettingsPage();
    assertEquals(getValueInId("_g"), "");
  }

  @Test(description = "User without permission shouldn't be able to edit harvester settings")
  public void preventEditingHarvesterSettingsTest() throws IOException {
    post(harvesterSettingsEndpoint, sectionsSaveEvent, new NameValuePair("sdk", "checked"));

    // make sure SkipDrmChecked is not changed.
    HarvesterSkipDrmPage page = logonToHarvesterSettingsPage();
    assertFalse(page.getSkipDrmChecked());
  }

  @Test(description = "User without permission shouldn't be able to add shortcut url settings")
  public void preventAddingShortcutUrlSettingsTest() throws IOException {
    final String URL = "https://www.scarywebsite.com";

    post(
        shortcutSettingsEndpoint,
        sectionsEvent("$UP$<BODY>"),
        sectionsEventParam0("_addShortcutUrlDialog.addShortcutUrl"),
        new NameValuePair("_addShortcutUrlDialog.showing", "true"),
        new NameValuePair("_addShortcutUrlDialog_shortcutText", "nope"),
        new NameValuePair("_addShortcutUrlDialog_urlText", URL));

    // make sure the shortcut url is not added.
    logonToShortcutURLsSettingsPage();
    assertFalse(isTextPresent(URL));
  }

  @Test(description = "User without permission shouldn't be able to delete shortcut url settings")
  public void preventDeletingShortcutUrlSettingsTest() throws IOException {
    final String NAME = "test-shortcut-name";
    post(shortcutSettingsEndpoint, sectionsEvent(".delete"), sectionsEventParam0(NAME));

    // make sure the shortcut url is still existing.
    logonToShortcutURLsSettingsPage();
    assertTrue(isTextPresent(NAME));
  }

  @Test(description = "User without permission shouldn't be able to edit login settings")
  public void preventEditingLoginSettingsTest() throws IOException {
    // save event without params will overwrite all available settings value to false.
    post(loginSettingsEndpoint, sectionsSaveEvent);

    // make sure enable via ip is still enabled.
    LoginSettingsPage page = logonToLoginSettingsPage();
    assertTrue(page.isEnableViaIp());
  }

  @Test(description = "User without permission shouldn't be able to edit mail settings")
  public void preventEditingMailSettingsTest() throws IOException {
    post(mailSettingsEndpoint, sectionsSaveEvent, new NameValuePair("_dn", "Display name"));

    // make sure display name is not changed.
    logonToMailSettingsPage();
    assertEquals(getValueInId("_dn"), "");
  }
}
