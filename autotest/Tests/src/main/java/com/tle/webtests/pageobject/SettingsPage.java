package com.tle.webtests.pageobject;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
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
import com.tle.webtests.pageobject.settings.PSSSettingsPage;
import com.tle.webtests.pageobject.settings.SelectionSessionSettingsPage;
import com.tle.webtests.pageobject.settings.ShortcutURLsSettingsPage;
import com.tle.webtests.pageobject.userscripts.ShowUserScriptsPage;
import org.openqa.selenium.WebElement;

public class SettingsPage extends AbstractPage<SettingsPage> {

  private static final String GROUP_GENERAL = "General";
  private static final String GROUP_INTEGRATIONS = "Integrations";
  private static final String GROUP_SEARCHING = "Search";
  private static final String GROUP_UI = "UI";

  private static final String SETTING_ACTIVE_CACHING = "Active caching";
  private static final String SETTING_CONTENT_RESTRICTIONS = "Content restrictions and quotas";
  private static final String SETTING_COURSE_DEFAULTS = "Copyright";
  private static final String SETTING_DATE_FORMAT = "Display date format";
  private static final String SETTING_DIAGNOSTICS = "Diagnostics";
  private static final String SETTING_EXTERNAL_TOOLS = "External tool providers (LTI)";
  private static final String SETTING_GOOGLE_ANALYTICS = "Google Analytics";
  private static final String SETTING_GOOGLE_API = "Google API";
  private static final String SETTING_HARVESTER = "Harvester";
  private static final String SETTING_LANGUAGES = "Languages";
  private static final String SETTING_LOGIN = "Login";
  private static final String SETTING_LTI_13 = "LTI 1.3 platforms";
  private static final String SETTING_MAIL = "Mail";
  private static final String SETTING_MANUAL_DATA_FIXES = "Manual data fixes";
  private static final String SETTING_MIME_TYPES = "MIME types";
  private static final String SETTING_OAI = "OAI";
  private static final String SETTING_OAUTH = "OAuth";
  private static final String SETTING_OIDC = "OpenID Connect (OIDC)";
  private static final String SETTING_PSS = "Pearson SCORM Services (PSS)";
  private static final String SETTING_SEARCH_INDEXING = "Searching and content indexing";
  private static final String SETTING_SEARCH_PAGE = "Search page";
  private static final String SETTING_SELECTION_SESSIONS = "Selection sessions";
  private static final String SETTING_SHORTCUTURLS = "Shortcut URLs";
  private static final String SETTING_USER_SCRIPTS = "User scripts";

  public static final String TOGGLE_NEW_UI = "Enable new UI";
  public static final String TOGGLE_NEW_SEARCH = "Enable new search page";

  public SettingsPage(PageContext context) {
    super(context);
    // Tell Selenide to use the existing browser session
    WebDriverRunner.setWebDriver(driver);
  }

  @Override
  protected WebElement findLoadedElement() {
    return $("#settingsPage");
  }

  @Override
  protected void loadUrl() {
    open(context.getBaseUrl() + "access/settings.do");
  }

  protected SelenideElement expandGroup(String group) {
    SelenideElement groupElement =
        $$(".SettingsPage-heading").findBy(text(group)).closest(".MuiAccordion-root");

    // Only click if not already expanded
    if (!groupElement.has(cssClass("Mui-expanded"))) {
      SelenideElement heading = groupElement.$(".SettingsPage-heading");
      heading.click();
    }

    return groupElement;
  }

  protected <T extends AbstractPage<T>> T clickSetting(String group, String title, T page) {
    expandGroup(group).$(byLinkText(title)).click();
    return page.get();
  }

  /** Overloaded helper for the most common group: General */
  protected <T extends AbstractPage<T>> T clickSetting(String title, T page) {
    return clickSetting(GROUP_GENERAL, title, page);
  }

  public boolean isSettingVisible(String title) {
    return $(byLinkText(title)).isDisplayed();
  }

  // --- General Settings Navigations ---

  public MimeSearchPage mimeSettings() {
    return clickSetting(SETTING_MIME_TYPES, new MimeSearchPage(context));
  }

  public GoogleApiSettingsPage googleApiSettings() {
    return clickSetting(SETTING_GOOGLE_API, new GoogleApiSettingsPage(context));
  }

  public GoogleSettingsPage googleSettings() {
    return clickSetting(SETTING_GOOGLE_ANALYTICS, new GoogleSettingsPage(context));
  }

  public ContentRestrictionsPage contentRestrictionsSettings() {
    return clickSetting(SETTING_CONTENT_RESTRICTIONS, new ContentRestrictionsPage(context));
  }

  public ShowExternalToolsPage externalToolsSettings() {
    return clickSetting(SETTING_EXTERNAL_TOOLS, new ShowExternalToolsPage(context));
  }

  public HarvesterSkipDrmPage harvestSkipDrmSettings() {
    return clickSetting(SETTING_HARVESTER, new HarvesterSkipDrmPage(context));
  }

  public LanguageSettingsPage languageSetingsPage() {
    return clickSetting(SETTING_LANGUAGES, new LanguageSettingsPage(context));
  }

  public LoginSettingsPage loginSettings() {
    return clickSetting(SETTING_LOGIN, new LoginSettingsPage(context));
  }

  public OAISettingsPage oaiSettingsPage() {
    return clickSetting(SETTING_OAI, new OAISettingsPage(context));
  }

  public SelectionSessionSettingsPage selectionSessionSettingsPage() {
    return clickSetting(SETTING_SELECTION_SESSIONS, new SelectionSessionSettingsPage(context));
  }

  public ShortcutURLsSettingsPage shortcutURLsSettingsPage() {
    return clickSetting(SETTING_SHORTCUTURLS, new ShortcutURLsSettingsPage(context));
  }

  public ActiveCachingPage activeCachingSettings() {
    return clickSetting(SETTING_ACTIVE_CACHING, new ActiveCachingPage(context));
  }

  public DiagnosticsPage diagnosticsPage() {
    return clickSetting(SETTING_DIAGNOSTICS, new DiagnosticsPage(context));
  }

  public MailSettingsPage mailSettingsPage() {
    return clickSetting(SETTING_MAIL, new MailSettingsPage(context));
  }

  public PSSSettingsPage pssSettingsPage() {
    return clickSetting(SETTING_PSS, new PSSSettingsPage(context));
  }

  public ShowUserScriptsPage userScriptsPage() {
    return clickSetting(SETTING_USER_SCRIPTS, new ShowUserScriptsPage(context));
  }

  public DateFormatSettingPage dateFormatSettingPage() {
    return clickSetting(SETTING_DATE_FORMAT, new DateFormatSettingPage(context));
  }

  public ManualDataFixesPage maualDataFixPage() {
    return clickSetting(SETTING_MANUAL_DATA_FIXES, new ManualDataFixesPage(context));
  }

  // --- Search Settings Navigations ---

  public boolean isSearchSettingsVisible() {
    return isSettingVisible(SETTING_SEARCH_INDEXING);
  }

  public SearchSettingsPage searchSettings() {
    return clickSetting(GROUP_SEARCHING, SETTING_SEARCH_PAGE, new SearchSettingsPage(context));
  }

  // --- Integration Settings Navigations ---

  public CourseDefaultsPage courseDefaultsSettings() {
    return clickSetting(
        GROUP_INTEGRATIONS, SETTING_COURSE_DEFAULTS, new CourseDefaultsPage(context));
  }

  public LTI13PlatformsSettingsPage lti13PlatformsSettingsPage() {
    return clickSetting(
        GROUP_INTEGRATIONS, SETTING_LTI_13, new LTI13PlatformsSettingsPage(context));
  }

  public OidcSettingsPage oidcSettingsPage() {
    return clickSetting(GROUP_INTEGRATIONS, SETTING_OIDC, new OidcSettingsPage(context));
  }

  public OAuthSettingsPage oauthSettingsPage() {
    return clickSetting(GROUP_INTEGRATIONS, SETTING_OAUTH, new OAuthSettingsPage(context));
  }

  // --- UI Settings Toggling ---

  /** Enable or disable new search UI. */
  public void setNewUI(boolean enable) {
    boolean stateChanged = toggleSwitchInUIGroup(TOGGLE_NEW_UI, enable);

    // Only sleep if we actually clicked the switch and triggered a background save
    if (stateChanged) {
      sleep(1000);
    }
  }

  /** Enable or disable new search UI. NewSearch only works when new UI is enabled. */
  public void setNewSearchUI(boolean enable) {
    toggleSwitchInUIGroup(TOGGLE_NEW_SEARCH, enable);
  }

  /**
   * Toggles a Material-UI switch.
   *
   * @return true if the switch state was changed, false if it was already correct.
   */
  private boolean toggleSwitchInUIGroup(String switchText, boolean enable) {
    SelenideElement groupElement = expandGroup(GROUP_UI);
    SelenideElement label = groupElement.$$("label").findBy(text(switchText));
    SelenideElement checkbox = label.$("input[type='checkbox']");

    if (checkbox.isSelected() != enable) {
      checkbox.shouldBe(enabled);
      label.click();
      return true;
    }

    return false;
  }
}
