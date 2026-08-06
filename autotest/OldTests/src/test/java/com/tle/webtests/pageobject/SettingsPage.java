package com.tle.webtests.pageobject;

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.SelenideElement;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.externaltools.ShowExternalToolsPage;
import com.tle.webtests.pageobject.oauth.OAuthSettingsPage;
import com.tle.webtests.pageobject.searching.SearchSettingsPage;
import com.tle.webtests.pageobject.settings.ActiveCachingPage;
import com.tle.webtests.pageobject.settings.ContentRestrictionsPage;
import com.tle.webtests.pageobject.settings.CourseDefaultsPage;
import com.tle.webtests.pageobject.settings.DateFormatSettingPage;
import com.tle.webtests.pageobject.settings.DiagnosticsPage;
import com.tle.webtests.pageobject.settings.GoogleApiSettingsPage;
import com.tle.webtests.pageobject.settings.GoogleSettingsPage;
import com.tle.webtests.pageobject.settings.HarvesterSkipDrmPage;
import com.tle.webtests.pageobject.settings.LTI13PlatformsSettingsPage;
import com.tle.webtests.pageobject.settings.LanguageSettingsPage;
import com.tle.webtests.pageobject.settings.LoginSettingsPage;
import com.tle.webtests.pageobject.settings.MailSettingsPage;
import com.tle.webtests.pageobject.settings.ManualDataFixesPage;
import com.tle.webtests.pageobject.settings.MimeSearchPage;
import com.tle.webtests.pageobject.settings.OAISettingsPage;
import com.tle.webtests.pageobject.settings.OidcSettingsPage;
import com.tle.webtests.pageobject.settings.SelectionSessionSettingsPage;
import com.tle.webtests.pageobject.settings.ShortcutURLsSettingsPage;
import com.tle.webtests.pageobject.userscripts.ShowUserScriptsPage;
import java.util.Optional;
import org.openqa.selenium.WebElement;

public class SettingsPage extends AbstractPage<SettingsPage> {

  // --- Group identifiers ---
  private static final String GROUP_GENERAL = "General";
  private static final String GROUP_INTEGRATIONS = "Integrations";
  private static final String GROUP_SEARCHING = "Search";
  private static final String GROUP_UI = "UI";
  private static final String GROUP_DIAGNOSTICS = "Diagnostics";

  // --- General settings ---
  private static final String SETTING_ACTIVE_CACHING = "Active caching";
  private static final String SETTING_CONTENT_RESTRICTIONS = "Content restrictions and quotas";
  private static final String SETTING_DATE_FORMAT = "Display date format";
  private static final String SETTING_GOOGLE_ANALYTICS = "Google Analytics";
  private static final String SETTING_GOOGLE_API = "Google API";
  private static final String SETTING_HARVESTER = "Harvester";
  private static final String SETTING_LANGUAGES = "Languages";
  private static final String SETTING_LOGIN = "Login";
  private static final String SETTING_MAIL = "Mail";
  private static final String SETTING_MIME_TYPES = "MIME types";
  private static final String SETTING_SELECTION_SESSIONS = "Selection sessions";
  private static final String SETTING_SHORTCUT_URLS = "Shortcut URLs";
  private static final String SETTING_USER_SCRIPTS = "User scripts";

  // --- Integration settings ---
  private static final String SETTING_LTI_13 = "LTI 1.3 platforms";
  private static final String SETTING_OAUTH = "OAuth";
  private static final String SETTING_OIDC = "OpenID Connect (OIDC)";
  private static final String SETTING_OAI = "OAI";
  private static final String SETTING_COURSE_DEFAULTS = "Copyright";
  private static final String SETTING_EXTERNAL_TOOLS = "External tool providers (LTI)";

  // --- Diagnostics settings ---
  private static final String SETTING_DIAGNOSTICS = "Diagnostics";
  private static final String SETTING_MANUAL_DATA_FIXES = "Manual data fixes";

  // --- Search settings ---
  private static final String SETTING_SEARCH_PAGE = "Search page";

  // --- UI toggles (public for test access) ---
  public static final String TOGGLE_NEW_UI = "Enable new UI";
  public static final String TOGGLE_NEW_SEARCH = "Enable new search page";

  public SettingsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return $("#settingsPage").toWebElement();
  }

  @Override
  protected void loadUrl() {
    open(context.getBaseUrl() + "access/settings.do");
  }

  protected void expandGroup(String group) {
    Optional.of($$("button").findBy(text(group)).shouldBe(visible))
        .filter(btn -> btn.has(attribute("aria-expanded", "false")))
        .ifPresent(SelenideElement::click);
  }

  protected <T extends AbstractPage<T>> T clickSetting(String group, String title, T page) {
    expandGroup(group);
    $(byLinkText(title)).shouldBe(visible).click();
    return page.get();
  }

  public boolean isSettingVisible(String title) {
    return $(byLinkText(title)).isDisplayed();
  }

  // --- General Settings Navigations ---

  protected <T extends AbstractPage<T>> T clickGeneralSetting(String title, T page) {
    return clickSetting(GROUP_GENERAL, title, page);
  }

  public MimeSearchPage clickMimeSetting() {
    return clickGeneralSetting(SETTING_MIME_TYPES, new MimeSearchPage(context));
  }

  public GoogleApiSettingsPage clickGoogleApiSetting() {
    return clickGeneralSetting(SETTING_GOOGLE_API, new GoogleApiSettingsPage(context));
  }

  public GoogleSettingsPage clickGoogleSetting() {
    return clickGeneralSetting(SETTING_GOOGLE_ANALYTICS, new GoogleSettingsPage(context));
  }

  public ContentRestrictionsPage clickContentRestrictionsSetting() {
    return clickGeneralSetting(SETTING_CONTENT_RESTRICTIONS, new ContentRestrictionsPage(context));
  }

  public HarvesterSkipDrmPage clickHarvestSkipDrmSetting() {
    return clickGeneralSetting(SETTING_HARVESTER, new HarvesterSkipDrmPage(context));
  }

  public LanguageSettingsPage clickLanguageSetting() {
    return clickGeneralSetting(SETTING_LANGUAGES, new LanguageSettingsPage(context));
  }

  public LoginSettingsPage clickLoginSetting() {
    return clickGeneralSetting(SETTING_LOGIN, new LoginSettingsPage(context));
  }

  public SelectionSessionSettingsPage clickSelectionSessionSetting() {
    return clickGeneralSetting(
        SETTING_SELECTION_SESSIONS, new SelectionSessionSettingsPage(context));
  }

  public ShortcutURLsSettingsPage clickShortcutURLsSetting() {
    return clickGeneralSetting(SETTING_SHORTCUT_URLS, new ShortcutURLsSettingsPage(context));
  }

  public ActiveCachingPage clickActiveCachingSetting() {
    return clickGeneralSetting(SETTING_ACTIVE_CACHING, new ActiveCachingPage(context));
  }

  public MailSettingsPage clickMailSetting() {
    return clickGeneralSetting(SETTING_MAIL, new MailSettingsPage(context));
  }

  public ShowUserScriptsPage clickUserScriptsSetting() {
    return clickGeneralSetting(SETTING_USER_SCRIPTS, new ShowUserScriptsPage(context));
  }

  public DateFormatSettingPage clickDateFormatSetting() {
    return clickGeneralSetting(SETTING_DATE_FORMAT, new DateFormatSettingPage(context));
  }

  // --- Integration Settings Navigations ---

  protected <T extends AbstractPage<T>> T clickIntegrationSetting(String title, T page) {
    return clickSetting(GROUP_INTEGRATIONS, title, page);
  }

  public CourseDefaultsPage clickCourseDefaultsSetting() {
    return clickIntegrationSetting(SETTING_COURSE_DEFAULTS, new CourseDefaultsPage(context));
  }

  public LTI13PlatformsSettingsPage clickLti13PlatformsSetting() {
    return clickIntegrationSetting(SETTING_LTI_13, new LTI13PlatformsSettingsPage(context));
  }

  public OidcSettingsPage clickOidcSetting() {
    return clickIntegrationSetting(SETTING_OIDC, new OidcSettingsPage(context));
  }

  public OAuthSettingsPage clickOAuthSetting() {
    return clickIntegrationSetting(SETTING_OAUTH, new OAuthSettingsPage(context));
  }

  public OAISettingsPage clickOaiSetting() {
    return clickIntegrationSetting(SETTING_OAI, new OAISettingsPage(context));
  }

  public ShowExternalToolsPage clickExternalToolsSetting() {
    return clickIntegrationSetting(SETTING_EXTERNAL_TOOLS, new ShowExternalToolsPage(context));
  }

  // --- Diagnostics Settings Navigations ---

  public DiagnosticsPage clickDiagnosticSetting() {
    return clickSetting(GROUP_DIAGNOSTICS, SETTING_DIAGNOSTICS, new DiagnosticsPage(context));
  }

  public ManualDataFixesPage clickManualDataFixesSetting() {
    return clickSetting(
        GROUP_DIAGNOSTICS, SETTING_MANUAL_DATA_FIXES, new ManualDataFixesPage(context));
  }

  // --- Search Settings Navigations ---

  public SearchSettingsPage clickSearchSetting() {
    return clickSetting(GROUP_SEARCHING, SETTING_SEARCH_PAGE, new SearchSettingsPage(context));
  }

  // --- UI Settings Toggling ---

  /** Enable or disable new UI feature. */
  public void setNewUI(boolean enable) {
    setUiGroupSwitchState(TOGGLE_NEW_UI, enable);
    $(enable ? "#mainDiv" : "#eqpageForm").shouldBe(exist);
  }

  /** Enable or disable new search page UI. NewSearch only works when new UI is enabled. */
  public void setNewSearchUI(boolean enable) {
    SelenideElement checkbox = setUiGroupSwitchState(TOGGLE_NEW_SEARCH, enable);
    checkbox.shouldHave(enable ? checked : not(checked));
  }

  /**
   * Helper method to find toggle switch by label and click it only if the state change is required.
   * Returns the checkbox element so callers can perform their own specific post-click assertions.
   */
  private SelenideElement setUiGroupSwitchState(String switchText, boolean expectedState) {
    expandGroup(GROUP_UI);

    SelenideElement label = $$("label").findBy(text(switchText)).shouldBe(visible);
    SelenideElement checkbox = label.$("input[type='checkbox']");

    if (checkbox.isSelected() != expectedState) {
      checkbox.shouldBe(enabled);
      label.click();
    }

    return checkbox;
  }
}
