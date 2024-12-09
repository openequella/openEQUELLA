package com.tle.webtests.test.admin.settings;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.generic.page.UserProfilePage;
import com.tle.webtests.pageobject.settings.LanguageSettingsPage;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class LanguageSettingsTest extends AbstractSessionTest {
  /** I'm assuming this is a sufficiently improbable combination. */
  public static final String DEMO_LANGUAGE = "Yiddish";

  public static final String DEMO_LANGUAGE_VAL = "yi";
  public static final String DEMO_COUNTRY = "Yemen";
  private static final String DEMO_NAME = DEMO_LANGUAGE + " (" + DEMO_COUNTRY + ')';
  public static final String DEMO_COUNTRY_VAL = "YE";
  public static final String UP_LANGUAGE = "Arabic";
  public static final String UP_LANGUAGE_FULL = UP_LANGUAGE + " (Saudi Arabia)";

  @Test
  public void testAddContribLang() {
    LanguageSettingsPage lsp = loginToLanguageSettingsPage();
    int numContribLangsPrior = lsp.countContribLangs();
    lsp.addContributionLanguageAndCountry(DEMO_LANGUAGE_VAL, DEMO_COUNTRY_VAL, DEMO_NAME);
    int numContribLangsPost = lsp.countContribLangs();
    assertTrue(
        (numContribLangsPrior + 1) == numContribLangsPost,
        "Expected number of langs to increase to "
            + (numContribLangsPrior + 1)
            + ", but have "
            + numContribLangsPost
            + '.');
    logout();
    // return and find our entry
    String displayYiddishInYemen = DEMO_NAME;
    lsp = loginToLanguageSettingsPage();
    assertTrue(
        lsp.doesContribLangByDisplayNameExist(displayYiddishInYemen),
        "Expected to see " + displayYiddishInYemen + ", but not found.");
  }

  @Test
  public void testAddDeleteLanguagePack() {
    LanguageSettingsPage lsp = loginToLanguageSettingsPage();

    URL langPack = Attachments.get("ar_SA.zip");
    lsp.uploadLanguagePack(langPack, UP_LANGUAGE, UP_LANGUAGE_FULL);

    UserProfilePage userPage = new UserProfilePage(context).load();
    // change but don't save (testing existence)
    userPage.setLanguageByCode("ar_SA");

    lsp = new LanguageSettingsPage(context).load();
    assertTrue(lsp.deleteLanguagePack(UP_LANGUAGE_FULL), "Language pack not deleted");
  }

  private LanguageSettingsPage loginToLanguageSettingsPage() {
    new LoginPage(context).load().login("AutoTest", "automated");
    SettingsPage sp = new SettingsPage(context).load();
    return sp.languageSetingsPage();
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    LanguageSettingsPage lsp = loginToLanguageSettingsPage();
    if (lsp.doesContribLangByDisplayNameExist(DEMO_NAME)) {
      lsp.deleteContribLangByName(DEMO_NAME);
    }
    if (lsp.doesLanguagePackExist(UP_LANGUAGE_FULL)) {
      lsp.deleteLanguagePack(UP_LANGUAGE_FULL);
    }
    super.cleanupAfterClass();
  }
}
