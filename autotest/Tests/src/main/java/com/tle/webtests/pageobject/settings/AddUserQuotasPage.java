package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AddUserQuotasPage extends AbstractPage<AddUserQuotasPage> {
  public static final String USER_QUOTA_TEXT_BOX_ID = "_aqd_userQuotaText";

  @FindBy(id = "ecr_qs")
  private WebElement quotaTextField;

  @FindBy(id = "ecr_userSelector_opener")
  private WebElement userSelectButton;

  @FindBy(id = "ecr_sv")
  private WebElement addButton;

  @FindBy(id = "ecr_cl")
  private WebElement cancelButton;

  public AddUserQuotasPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return quotaTextField;
  }

  public <T extends PageObject> T openRecipientSelector(WaitingPageObject<T> targetPage) {
    userSelectButton.click();
    return targetPage.get();
  }

  public void setUserQuota(String userQuota) {
    quotaTextField.clear();
    quotaTextField.sendKeys(userQuota);
  }

  public String getSelectedExpression() {
    By by = By.xpath("//span[@class='expression']");
    waitForElement(by);
    return driver.findElement(by).getText();
  }

  public <T extends PageObject> T saveUserQuota(WaitingPageObject<T> returnPage) {
    addButton.click();
    return returnPage.get();
  }
}
