package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import java.net.URL;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;

public class LanguageSettingsPage extends AbstractPage<LanguageSettingsPage> {
  // original property is language.title
  public static final String LANGUAGES_LINK = "Languages";
  // original property is language.separator.langpack
  public static final String LANGUAGES_PAGE_HEADER = "Language Packs";

  @FindBy(id = "_contributionLanguageTbl")
  private WebElement contributionLanguageTable;

  @FindBy(id = "_addContribLangLink")
  private WebElement addContribLangLink;

  @FindBy(id = "_fiup")
  private WebElement langPackUpload;

  @FindBy(id = "_imprtlnk")
  private WebElement importLink;

  @FindBy(id = "_languagePacksTbl")
  private WebElement languagePacksTable;

  public LanguageSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(LANGUAGES_PAGE_HEADER) + ']'));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/language.do");
  }

  public boolean doesContribLangByDisplayNameExist(String displayName) {
    return isPresent(contributionLanguageTable, getRowBy(displayName));
  }

  public LanguageSettingsPage deleteContribLangByName(String name) {
    ExpectWaiter<LanguageSettingsPage> disappearWaiter = contDisappearWaiter(name);
    contributionLanguageTable
        .findElement(
            new ByChained(getRowBy(name), By.xpath("td[@class='actions']/a[@class='unselect']")))
        .click();

    acceptConfirmation();
    return disappearWaiter.get();
  }

  public int countContribLangs() {
    return getContribLangElements().size();
  }

  private By getRowBy(String shortcutName) {
    return By.xpath(".//tr[td[1][text()=" + quoteXPath(shortcutName) + "]]");
  }

  private ExpectWaiter<LanguageSettingsPage> newContWaiter(String named) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(contributionLanguageTable, getRowBy(named)),
        this);
  }

  private ExpectWaiter<LanguageSettingsPage> contDisappearWaiter(String named) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.invisibilityOfElementLocated(
            contributionLanguageTable, getRowBy(named)),
        this);
  }

  private ExpectWaiter<LanguageSettingsPage> newPackWaiter(String named) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(languagePacksTable, getRowBy(named)), this);
  }

  private ExpectWaiter<LanguageSettingsPage> packDisappearWaiter(String named) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.invisibilityOfElementLocated(languagePacksTable, getRowBy(named)),
        this);
  }

  private List<WebElement> getContribLangElements() {
    return contributionLanguageTable.findElements(By.xpath(".//tr"));
  }

  public LanguageSettingsPage uploadLanguagePack(
      URL langURL, String languageName, String fullname) {
    waitForHiddenElement(langPackUpload);
    langPackUpload.sendKeys(getPathFromUrl(langURL));
    waitForElement(importLink);
    importLink.click();
    return newPackWaiter(fullname).get();
  }

  public AbstractPage<?> addContributionLanguageAndCountry(
      String languageVal, String countryVal, String fullname) {
    // open the dialog
    addContribLangLink.click();
    AddContribLangPage dialog = new AddContribLangPage(context).get();
    dialog.selectLanguage(languageVal);
    dialog.selectCountry(countryVal);

    dialog.ok(updateWaiter(contributionLanguageTable));
    return newContWaiter(fullname).get();
  }

  public boolean doesLanguagePackExist(String languageName) {
    return isPresent(languagePacksTable, getRowBy(languageName));
  }

  public boolean deleteLanguagePack(String languageName) {
    ExpectWaiter<LanguageSettingsPage> packDisappearWaiter = packDisappearWaiter(languageName);
    WebElement row = languagePacksTable.findElement(getRowBy(languageName));
    row.findElement(By.linkText("Delete")).click();
    acceptConfirmation();
    packDisappearWaiter.get();
    return !doesLanguagePackExist(languageName);
  }
}
