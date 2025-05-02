package com.tle.webtests.pageobject;

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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SettingsPage extends AbstractPage<SettingsPage> {

  public static final String SEARCH_SETTINGS_LINK_TITLE = "Search page";
  public static final String COURSE_DEFAULTS_LINK_TITLE = "Copyright";
  private static final String SEARCH_SETTING_TITLE = "Searching and content indexing";

  private static final String GROUP_SEARCHING = "Search";
  private static final String GROUP_INTEGRATIONS = "Integrations";

  public SettingsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getSettingsGroup();
  }

  private WebElement getSettingsGroup() {
    return driver.findElement(By.id("settingsPage"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/settings.do");
  }

  protected <T extends AbstractPage<T>> T clickSetting(String title, T page) {
    return clickSetting("General", title, page);
  }

  protected WebElement openGroup(String group, By untilVisible) {
    String titlePath = ".//p[text() = " + quoteXPath(group) + "]";
    WebElement settingGroup =
        getWaiter()
            .until(
                ExpectedConditions.visibilityOfNestedElementsLocatedBy(
                    getSettingsGroup(), By.xpath("div[" + titlePath + "]")))
            .get(0);
    settingGroup.findElement(By.xpath(titlePath)).click();
    waiter.until(ExpectedConditions.visibilityOfElementLocated(untilVisible));
    return settingGroup;
  }

  protected WebElement openGroupContaining(String group, By untilVisible) {
    WebElement settingGroup = openGroup(group, untilVisible);
    return settingGroup.findElement(untilVisible);
  }

  protected <T extends AbstractPage<T>> T clickSetting(String group, String title, T page) {
    By linkByTitle = By.xpath("//div[@id='settingsPage']//a[text()='" + title + "']");
    WebElement groupElem = openGroup(group, linkByTitle);

    WebElement link = groupElem.findElement(linkByTitle);
    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", link);
    return page.get();
  }

  public boolean isSettingVisible(String title) {
    return !driver.findElements(By.linkText(title)).isEmpty();
  }

  public MimeSearchPage mimeSettings() {
    return clickSetting("MIME types", new MimeSearchPage(context));
  }

  public GoogleApiSettingsPage googleApiSettings() {
    return clickSetting("Google API", new GoogleApiSettingsPage(context));
  }

  public GoogleSettingsPage googleSettings() {
    return clickSetting("Google Analytics", new GoogleSettingsPage(context));
  }

  public SearchSettingsPage searchSettings() {
    return clickSetting(
        GROUP_SEARCHING, SEARCH_SETTINGS_LINK_TITLE, new SearchSettingsPage(context));
  }

  public CourseDefaultsPage courseDefaultsSettings() {
    return clickSetting(
        GROUP_INTEGRATIONS, COURSE_DEFAULTS_LINK_TITLE, new CourseDefaultsPage(context));
  }

  public ContentRestrictionsPage contentRestrictionsSettings() {
    return clickSetting("Content restrictions and quotas", new ContentRestrictionsPage(context));
  }

  public ShowExternalToolsPage externalToolsSettings() {
    return clickSetting("External tool providers (LTI)", new ShowExternalToolsPage(context));
  }

  public HarvesterSkipDrmPage harvestSkipDrmSettings() {
    return clickSetting("Harvester", new HarvesterSkipDrmPage(context));
  }

  public LanguageSettingsPage languageSetingsPage() {
    return clickSetting("Languages", new LanguageSettingsPage(context));
  }

  public LoginSettingsPage loginSettings() {
    return clickSetting(LoginSettingsPage.LOGIN_SETTINGS, new LoginSettingsPage(context));
  }

  public OAISettingsPage oaiSettingsPage() {
    return clickSetting("OAI", new OAISettingsPage(context));
  }

  public SelectionSessionSettingsPage selectionSessionSettingsPage() {
    return clickSetting("Selection sessions", new SelectionSessionSettingsPage(context));
  }

  public ShortcutURLsSettingsPage shortcutURLsSettingsPage() {
    return clickSetting(
        ShortcutURLsSettingsPage.SHORTCUT_URLS_TITLE, new ShortcutURLsSettingsPage(context));
  }

  public ActiveCachingPage activeCachingSettings() {
    return clickSetting("Active caching", new ActiveCachingPage(context));
  }

  public DiagnosticsPage diagnosticsPage() {
    return clickSetting("Diagnostics", new DiagnosticsPage(context));
  }

  public boolean isSearchSettingsVisible() {
    return isSettingVisible(SEARCH_SETTING_TITLE);
  }

  public MailSettingsPage mailSettingsPage() {
    return clickSetting("Mail", new MailSettingsPage(context));
  }

  public PSSSettingsPage pssSettingsPage() {
    return clickSetting("Pearson SCORM Services (PSS)", new PSSSettingsPage(context));
  }

  public ShowUserScriptsPage userScriptsPage() {
    return clickSetting("User scripts", new ShowUserScriptsPage(context));
  }

  public DateFormatSettingPage dateFormatSettingPage() {
    return clickSetting("Display date format", new DateFormatSettingPage(context));
  }

  public ManualDataFixesPage maualDataFixPage() {
    return clickSetting("Manual data fixes", new ManualDataFixesPage(context));
  }

  public LTI13PlatformsSettingsPage lti13PlatformsSettingsPage() {
    return clickSetting(
        GROUP_INTEGRATIONS, "LTI 1.3 platforms", new LTI13PlatformsSettingsPage(context));
  }

  public OidcSettingsPage oidcSettingsPage() {
    return clickSetting(GROUP_INTEGRATIONS, "OpenID Connect (OIDC)", new OidcSettingsPage(context));
  }

  public OAuthSettingsPage oauthSettingsPage() {
    return clickSetting(GROUP_INTEGRATIONS, "OAuth", new OAuthSettingsPage(context));
  }

  /** Enable or disable new search UI. */
  public void setNewUI(boolean enable) {
    WebElement newUI =
        openGroupContaining(
            "UI", By.xpath(".//label[./span[text() = 'Enable new UI']]/span[1]/span"));
    boolean isChecked = newUI.getAttribute("class").contains("Mui-checked");

    if ((enable && !isChecked) || (!enable && isChecked)) {
      newUI.click();
    }

    // Yeah this sucks, it auto saves in the background
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /** Enable or disable new search UI. NewSearch only works when new UI is enabled. */
  public void setNewSearchUI(boolean enable) {
    WebElement newSearchUI =
        openGroupContaining(
            "UI", By.xpath(".//label[./span[text() = 'Enable new search page']]/span[1]/span"));
    boolean isChecked = newSearchUI.getAttribute("class").contains("Mui-checked");

    if (enable != isChecked) {
      newSearchUI.click();
    }
  }
}
