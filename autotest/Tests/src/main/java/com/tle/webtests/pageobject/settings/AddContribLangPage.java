package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AddContribLangPage extends AbstractPage<AddContribLangPage> {
  public static final String ADD_CONTRIBLANG_HEADER =
      "Select Language and/or Country"; // original property is addlanguage.dialog.title.title

  private static final String LANG_DROPDOWN_ID = "_addLanguageDialog_lnge";

  private static final String CNTR_DROPDOWN_ID = "_addLanguageDialog_cntr";

  @FindBy(id = "_addLanguageDialog_ok")
  private WebElement ok;

  public AddContribLangPage(PageContext context) {
    super(context, By.xpath("//h3[text()=" + quoteXPath(ADD_CONTRIBLANG_HEADER) + ']'));
  }

  public void selectLanguage(String languageVal) {
    // click on the displayed item (there's only one at first) to pull down the drop-down
    EquellaSelect languageList =
        new EquellaSelect(context, driver.findElement(By.id(LANG_DROPDOWN_ID)));
    languageList.selectByValue(languageVal);
  }

  public void selectCountry(String countryVal) {
    EquellaSelect countryList =
        new EquellaSelect(context, driver.findElement(By.id(CNTR_DROPDOWN_ID)));
    countryList.selectByValue(countryVal);
  }

  public LanguageSettingsPage ok(WaitingPageObject<LanguageSettingsPage> returnTo) {
    ok.click();
    return returnTo.get();
  }
}
