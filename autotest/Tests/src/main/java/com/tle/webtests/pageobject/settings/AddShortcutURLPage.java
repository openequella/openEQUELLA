package com.tle.webtests.pageobject.settings;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AddShortcutURLPage extends AbstractPage<AddShortcutURLPage> {
  public static final String ADD_SHORTCUT_HEADER = "Add shortcut URL"; // original
  // property
  // key:
  // shortcuturls.dialog.title

  public static final String SHORTCUT_TEXT_ID = "_addShortcutUrlDialog_shortcutText";
  public static final String URL_TEXT_ID = "_addShortcutUrlDialog_urlText";

  @FindBy(id = SHORTCUT_TEXT_ID)
  private WebElement shortcutTextBox;

  @FindBy(id = URL_TEXT_ID)
  private WebElement urlTextBox;

  @FindBy(id = "_addShortcutUrlDialog_ok")
  private WebElement okButton;

  @FindBy(id = "_addShortcutUrlDialog_close")
  private WebElement closeButton;

  @FindBy(id = "_addShortcutUrlDialogfooter")
  private WebElement footer;

  private final ShortcutURLsSettingsPage page;

  public AddShortcutURLPage(ShortcutURLsSettingsPage page) {
    super(page.getContext());
    this.page = page;
  }

  @Override
  protected WebElement findLoadedElement() {
    return footer;
  }

  public AddShortcutURLPage setShortcutText(String shortcutText) {
    shortcutTextBox.clear();
    shortcutTextBox.sendKeys(shortcutText);
    return this;
  }

  public AddShortcutURLPage setUrlText(String urlText) {
    urlTextBox.clear();
    urlTextBox.sendKeys(urlText);
    return this;
  }

  public ShortcutURLsSettingsPage ok(WaitingPageObject<ShortcutURLsSettingsPage> returnTo) {
    okButton.click();
    return returnTo.get();
  }

  public AddShortcutURLPage okFailure(WaitingPageObject<AddShortcutURLPage> returnTo) {
    okButton.click();
    return returnTo.get();
  }

  public ShortcutURLsSettingsPage close() {
    closeButton.click();
    return page.get();
  }

  private By errorBy(String errorText) {
    return By.xpath("../p[@class = 'ctrlinvalidmessage' and text()=" + quoteXPath(errorText) + "]");
  }

  public WaitingPageObject<AddShortcutURLPage> fieldError(boolean urls, String text) {
    WebElement fieldElem = shortcutTextBox;
    if (urls) {
      fieldElem = urlTextBox;
    }
    return visibilityWaiter(fieldElem, errorBy(text));
  }
}
