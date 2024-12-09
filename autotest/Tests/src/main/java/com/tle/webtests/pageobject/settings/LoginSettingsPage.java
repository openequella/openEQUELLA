package com.tle.webtests.pageobject.settings;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginSettingsPage extends AbstractPage<LoginSettingsPage> {
  public static final String LOGIN_SETTINGS = "Login";
  private static final String SAVE_RECEIPT = "Login settings saved successfully";

  @FindBy(id = "_enableViaIpCheck")
  private WebElement enableViaIpCheck;

  @FindBy(id = "_disableAutoLoginCheck")
  private WebElement disableAutoLoginCheck;

  @FindBy(id = "_disallowUserEditCheck")
  private WebElement disallowUserEditCheck;

  @FindBy(id = "_transientDRMCheck")
  private WebElement drmCheck;

  @FindBy(id = "_ipAddressTable")
  private WebElement ipAddressTable;

  @FindBy(id = "_addIpAddressLink")
  private WebElement addIpAddressLink;

  @FindBy(id = "_selectUserDialog_opener")
  private WebElement selectUser;

  @FindBy(id = "_saveButton")
  private WebElement save;

  @FindBy(id = "selecteduser")
  private WebElement selectedUserAjax;

  @FindBy(xpath = "//h2[text()='Login']")
  private WebElement title;

  @FindBy(id = "enteredIpAddress")
  private WebElement ipAjaxDiv;

  public LoginSettingsPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return title;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/loginsettings.do");
  }

  public boolean isEnableViaIp() {
    return enableViaIpCheck.isSelected();
  }

  public LoginSettingsPage setEnableViaIp(boolean check) {
    WaitingPageObject<LoginSettingsPage> showWaiter = showWaiter(disableAutoLoginCheck, check);
    if (enableViaIpCheck.isSelected() != check) {
      enableViaIpCheck.click();
    }
    return showWaiter.get();
  }

  public boolean isDisableAutoLogin() {
    return disableAutoLoginCheck.isSelected();
  }

  public LoginSettingsPage setDisableAutoLogin(boolean check) {
    if (disableAutoLoginCheck.isSelected() != check) {
      disableAutoLoginCheck.click();
    }
    return get();
  }

  public boolean isDisallowUserEdit() {
    return disallowUserEditCheck.isSelected();
  }

  public LoginSettingsPage setDisallowUserEdit(boolean check) {
    if (disallowUserEditCheck.isSelected() != check) {
      disallowUserEditCheck.click();
    }
    return get();
  }

  public boolean isDoNotStoreDrm() {
    return drmCheck.isSelected();
  }

  public LoginSettingsPage setDoNotStoreDrm(boolean check) {
    if (drmCheck.isSelected() != check) {
      drmCheck.click();
    }
    return get();
  }

  public boolean controlsHidden() {
    return !isVisible(disableAutoLoginCheck);
  }

  public int countIPAddresses() {
    return getIpAddressElements().size();
  }

  public int findIpAddress(String ipAddress) {
    List<WebElement> trs = getIpAddressElements();
    if (!Check.isEmpty(trs)) {
      for (int i = 0; i < trs.size(); ++i) {
        WebElement we = trs.get(i);
        if (we.findElement(By.xpath("./td[@class='name']")).getText().equals(ipAddress)) return i;
      }
    }
    return -1;
  }

  public LoginSettingsPage addIPAddress(String ipAddress) {
    WaitingPageObject<LoginSettingsPage> ajaxUpdate = ajaxUpdate(ipAjaxDiv);
    return openAddIPDialog().setIpAddress(ipAddress).ok(ajaxUpdate);
  }

  public AddIPAddressPage openAddIPDialog() {
    addIpAddressLink.click();
    return new AddIPAddressPage(context).get();
  }

  public LoginSettingsPage setUser(String user) {
    selectUser.click();
    SelectUserDialog dialog = new SelectUserDialog(context, "_selectUserDialog").get();
    return dialog.search(user).selectAndFinish(user, ajaxUpdate(selectedUserAjax));
  }

  public boolean hasUser(String user) {
    return isPresent(By.xpath("//span[@title=" + quoteXPath(user) + "]"));
  }

  public boolean deleteIpAddressByIndex(int index) {
    boolean deleted = false;
    List<WebElement> trs = getIpAddressElements();
    if (trs.size() > index) {
      WebElement tr = trs.get(index);
      WebElement wobble = tr.findElement(By.xpath("td[@class='actions']/a[@class='unselect']"));
      // confirmTrue (to clear the confirmation box) before calling
      // click()
      // (which creates the confirmation box).
      wobble.click();
      acceptConfirmation();
      deleted = true;
    }
    return deleted;
  }

  public LoginSettingsPage deleteIpAddress(String ip) {
    WaitingPageObject<LoginSettingsPage> ajaxUpdate = ajaxUpdate(ipAjaxDiv);
    WebElement ipRow =
        driver.findElement(By.xpath("//tr/td[@class='name' and text()=" + quoteXPath(ip) + "]"));
    ipRow.findElement(By.xpath("../td[@class='actions']/a[@class='unselect']")).click();
    return ajaxUpdate.get();
  }

  public boolean hasIpAddress(String ip) {
    return isPresent(By.xpath("//tr/td[@class='name' and text()=" + quoteXPath(ip) + "]"));
  }

  private LoginSettingsPage save(WaitingPageObject<LoginSettingsPage> returnTo) {
    save.click();
    return returnTo.get();
  }

  public LoginSettingsPage saveSuccess() {
    return save(ReceiptPage.waiter(SAVE_RECEIPT, this));
  }

  public LoginSettingsPage saveFailure() {
    return save(updateWaiter());
  }

  public List<String> getErrors() {
    List<WebElement> errorElements = driver.findElements(By.className("ctrlinvalidmessage"));

    return Lists.transform(
        errorElements,
        new Function<WebElement, String>() {

          @Override
          public String apply(WebElement we) {
            return we.getText();
          }
        });
  }

  public boolean hasError(String text) {
    return isPresent(
        By.xpath("//div[contains(@class, 'ctrlinvalid')]/p[text()=" + quoteXPath(text) + "]"));
  }

  /**
   * Returns the rows containing 'name' elements, so should exclude the table header (if any) "
   *
   * @return a non-null list, possibly empty
   */
  private List<WebElement> getIpAddressElements() {
    return ipAddressTable.findElements(By.xpath(".//tr[td[@class='name']]"));
  }
}
