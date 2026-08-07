package com.tle.webtests.pageobject.institution;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ClonePage extends AbstractPage<ClonePage> implements DbSelectable<ClonePage> {
  private static final String ID_SELECTDB = "isclo_selectDatabase";

  @FindBy(id = "isclo_name")
  private WebElement name;

  @FindBy(id = "isclo_url")
  private WebElement institutionUrl;

  @FindBy(id = "isclo_filestore")
  private WebElement filestoreLocation;

  @FindBy(id = "isclo_actionButton")
  private WebElement cloneButton;

  @FindBy(id = ID_SELECTDB)
  private WebElement selectDbButton;

  @FindBy(
      xpath =
          "//div[@class='settingRow' and div[@class='settingLabel']//label[text()='Target"
              + " database']]")
  private WebElement dbSettingRow;

  private final InstitutionListTab listTab;

  public ClonePage(PageContext context, InstitutionListTab listTab) {
    super(context, By.id("isclo_actionButton"));
    this.listTab = listTab;
  }

  @Override
  public WaitingPageObject<ClonePage> getUpdateWaiter() {
    return ExpectWaiter.waiter(ExpectedConditions2.updateOfElement(dbSettingRow), this);
  }

  private ClonePage selectFirstDbIfPresent() {
    if (isPresent(By.id(ID_SELECTDB))) {
      selectDbButton.click();
      SelectDbDialog<ClonePage> selectDbPage = new SelectDbDialog<ClonePage>(this, "isclo").get();
      return selectDbPage.selectFirst();
    }
    return this;
  }

  public StatusPage<InstitutionListTab> clone(String url, String shortName) {
    selectFirstDbIfPresent();

    name.clear();
    name.sendKeys(shortName);

    institutionUrl.clear();
    institutionUrl.sendKeys(url);

    filestoreLocation.clear();
    filestoreLocation.sendKeys(shortName);

    cloneButton.click();
    acceptConfirmation();
    return new StatusPage<InstitutionListTab>(context, listTab).get();
  }
}
