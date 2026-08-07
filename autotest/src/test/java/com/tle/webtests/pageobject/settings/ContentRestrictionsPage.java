/** */
package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ContentRestrictionsPage extends AbstractPage<ContentRestrictionsPage> {
  public static final String BANNED_EXT_SECTION_TITLE = "Banned file extensions";

  @FindBy(id = "_bannedExtensions")
  private WebElement bannedExtTable;

  @FindBy(id = "_ael")
  private WebElement addExtLink;

  @FindBy(id = "_userQuotas")
  private WebElement userQuotasTable;

  @FindBy(id = "_aql")
  private WebElement addQuotaLink;

  public ContentRestrictionsPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(BANNED_EXT_SECTION_TITLE) + ']'));
  }

  public boolean isExtPresent(String bannedExt) {
    return isPresent(getBannedExtBy(bannedExt));
  }

  public ContentRestrictionsPage deleteBannedExt(String name) {
    WebElement ext = bannedExtTable.findElement(getBannedExtBy(name));
    WaitingPageObject<ContentRestrictionsPage> waiter = removalWaiter(ext);
    WebElement wibble =
        ext.findElement(By.xpath("following-sibling::td[@class='actions']/a[@class='unselect']"));
    wibble.click();
    acceptConfirmation();
    return waiter.get();
  }

  public int countRowsBannedExt() {
    return bannedExtTable.findElements(By.xpath(".//td[@class='name']")).size();
  }

  public ContentRestrictionsPage addBannedExtOk(String bannedExtension) {
    addExtLink.click();
    AddBannedExtensionPage abePage = new AddBannedExtensionPage(context).get();
    abePage.setBannedExt(bannedExtension);
    return abePage.ok(newWaiter(bannedExtension));
  }

  private ExpectWaiter<ContentRestrictionsPage> newWaiter(String named) {
    return ExpectWaiter.waiter(
        ExpectedConditions2.visibilityOfElementLocated(bannedExtTable, getBannedExtBy(named)),
        this);
  }

  private By getBannedExtBy(String name) {
    return By.xpath(".//td[@class='name' and text()=" + quoteXPath(name) + "]");
  }

  public int countUserQuotas() {
    return getUserQuotasElements().size();
  }

  public AddUserQuotasPage openUserQuotaPage() {
    addQuotaLink.click();
    AddUserQuotasPage auqPage = new AddUserQuotasPage(context).get();
    return auqPage.get();
  }

  private List<WebElement> getUserQuotasElements() {
    return userQuotasTable.findElements(By.xpath(".//td[@class='name']"));
  }

  public String getUserQuotasInfoByExpression(String expression) {
    return driver
        .findElement(
            By.xpath(
                "//div[@id='userQuotas']/table/tbody/tr/td[text()="
                    + quoteXPath(expression)
                    + "]/../td[@class='name']"))
        .getText();
  }

  public void deleteUserQuotasByExpression(String expression) {
    driver
        .findElement(
            By.xpath(
                "//div[@id='userQuotas']/table/tbody/tr/td[text()="
                    + quoteXPath(expression)
                    + "]/../td[@class='actions']/a[2]"))
        .click();
    acceptConfirmation();
  }

  public <T extends PageObject> T editUserQuotasByExpression(
      String expression, WaitingPageObject<T> targetPage) {
    driver
        .findElement(
            By.xpath(
                "//div[@id='userQuotas']/table/tbody/tr/td[text()="
                    + quoteXPath(expression)
                    + "]/../td[@class='actions']/a[1]"))
        .click();
    return targetPage.get();
  }
}
