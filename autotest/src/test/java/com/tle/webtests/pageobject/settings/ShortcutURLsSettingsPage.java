package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ShortcutURLsSettingsPage extends AbstractPage<ShortcutURLsSettingsPage> {
  public static final String SHORTCUT_URLS_TITLE = "Shortcut URLs"; // original

  // property
  // key:
  // shortcuturls.title

  @FindBy(id = "_shortcutsTable")
  private WebElement shortcutTable;

  @FindBy(id = "_addShortcutUrlLink")
  private WebElement addShortcutUrlLink;

  public ShortcutURLsSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(SHORTCUT_URLS_TITLE) + ']'));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/shortcuturlssettings.do");
  }

  public ShortcutURLsSettingsPage addShortcutTextAndURL(String shortcutTxt, String urlTxt) {
    AddShortcutURLPage dialog = addShortcut();
    dialog.setShortcutText(shortcutTxt);
    dialog.setUrlText(urlTxt);
    return dialog.ok(receiptWaiter());
  }

  public WaitingPageObject<ShortcutURLsSettingsPage> receiptWaiter() {
    return ReceiptPage.waiter("Shortcut URL has been saved", this);
  }

  public AddShortcutURLPage addShortcutTextAndURLFailure(
      String shortcutTxt, String urlTxt, boolean urls, String failureText) {
    AddShortcutURLPage dialog = addShortcut();
    dialog.setShortcutText(shortcutTxt);
    dialog.setUrlText(urlTxt);
    return dialog.okFailure(dialog.fieldError(urls, failureText));
  }

  public AddShortcutURLPage addShortcut() {
    addShortcutUrlLink.click();
    return new AddShortcutURLPage(this).get();
  }

  public boolean containsShortcut(String shortcutName) {
    return isPresent(shortcutTable, getRowBy(shortcutName));
  }

  private By getRowBy(String shortcutName) {
    return By.xpath(".//tr[td[1][text()=" + quoteXPath(shortcutName) + "]]");
  }

  public void deleteShortcut(String shortcutName) {
    WebElement row = shortcutTable.findElement(getRowBy(shortcutName));
    WaitingPageObject<ShortcutURLsSettingsPage> removalWaiter = removalWaiter(row);
    row.findElement(By.xpath(".//td/a[@class='unselect']")).click();
    acceptConfirmation();
    removalWaiter.get();
  }
}
