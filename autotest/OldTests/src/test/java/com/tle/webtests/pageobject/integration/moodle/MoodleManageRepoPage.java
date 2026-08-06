package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class MoodleManageRepoPage extends AbstractPage<MoodleManageRepoPage> {
  private static final String TEXT_ENABLE = "Enabled and visible";

  @FindBy(id = "id_submitbutton")
  private WebElement saveButton;

  @FindBy(xpath = "id('applytoequella')//select")
  private WebElement selectElement;

  private Select equellaRepoSelect;

  @FindBy(xpath = "//a[contains(@href, 'repos=equella') and contains(@href, 'action=edit')]")
  private WebElement settingsLink;

  public MoodleManageRepoPage(PageContext context) {
    super(context, By.xpath("//body[contains(@id,'page-admin-repository')]"));
  }

  @Override
  protected void checkLoadedElement() {
    super.checkLoadedElement();
    equellaRepoSelect = new Select(selectElement);
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "admin/repository.php");
  }

  public MoodleManageRepoPage enableEquella() {
    if (TEXT_ENABLE.equals(equellaRepoSelect.getFirstSelectedOption().getText())) {
      return this;
    }
    equellaRepoSelect.selectByVisibleText(TEXT_ENABLE);
    waitForElement(saveButton);
    saveButton.click();
    return get();
  }

  public MoodleEquellaRepoList equellaSettings() {
    throw new Error("Only supported on moodle23");
  }
}
