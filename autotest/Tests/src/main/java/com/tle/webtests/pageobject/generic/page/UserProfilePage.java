package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UserProfilePage extends AbstractPage<UserProfilePage> {
  @FindBy(id = "ed_ll")
  private WebElement langSelect;

  @FindBy(id = "ed_df")
  private WebElement dateFormatSelect;

  @FindBy(id = "ed_saveButton")
  private WebElement saveButton;

  @FindBy(id = "ed_changePassButton")
  private WebElement changePasswordButton;

  @FindBy(id = "accessMode")
  private WebElement acModeCheckbox;

  public UserProfilePage(PageContext context) {
    super(context, By.id("editUser"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/user.do");
  }

  public PasswordDialog changePasswordDialog() {
    changePasswordButton.click();
    return new PasswordDialog(context).get();
  }

  private UserProfilePage save(WaitingPageObject<UserProfilePage> returnTo) {
    scrollToElement(saveButton);
    saveButton.click();
    return returnTo.get();
  }

  private WaitingPageObject<UserProfilePage> errorMessageWaiter(String msg) {
    return ExpectWaiter.waiter(
        ExpectedConditions.presenceOfElementLocated(
            By.xpath(
                "//p[normalize-space(@class)='ctrlinvalidmessage' and text()="
                    + quoteXPath(msg)
                    + "]")),
        this);
  }

  public WaitingPageObject<UserProfilePage> containsPasswordMatchError() {
    return errorMessageWaiter("The new passwords you entered do not match");
  }

  public WaitingPageObject<UserProfilePage> containsWrongOldPasswordError() {
    return errorMessageWaiter("The old password you entered is incorrect");
  }

  public WaitingPageObject<UserProfilePage> passwordChange() {
    return ReceiptPage.waiter("Password changed successfully.", this);
  }

  /**
   * @param locale use the short code, eg aa_DJ or en_AU
   */
  public void setLanguageByCode(String locale) {
    new EquellaSelect(context, langSelect).get().selectByValue(locale);
  }

  public UserProfilePage saveSuccesful() {
    return save(ReceiptPage.waiter("Profile saved successfully.", this));
  }

  public void setAccessibilityMode(boolean enabled) {
    if (enabled != acModeCheckbox.isSelected()) {
      acModeCheckbox.click();
    }
  }

  public boolean isAccessibilityMode() {
    return acModeCheckbox.isSelected();
  }

  public boolean isAccessibleDropdown() {
    return isVisible(langSelect);
  }

  public void setDateFormat(String formatSetting) {
    new EquellaSelect(context, dateFormatSelect).get().selectByValue(formatSetting);
  }

  public String getDateFormat() {
    return new EquellaSelect(context, dateFormatSelect).get().getSelectedText();
  }
}
