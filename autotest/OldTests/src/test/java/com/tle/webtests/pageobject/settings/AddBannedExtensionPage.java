package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AddBannedExtensionPage extends AbstractPage<AddBannedExtensionPage> {
  public static final String BANNED_EXT_TEXT_BOX_ID = "_aed_bannedExtText";

  @FindBy(id = BANNED_EXT_TEXT_BOX_ID)
  private WebElement bannedExtTextBox;

  @FindBy(id = "_aed_ok")
  private WebElement ok;

  @FindBy(id = "_aed_cancel")
  private WebElement cancel;

  public AddBannedExtensionPage(PageContext context) {
    super(
        context,
        By.xpath("//h3[text()='Enter banned extension']")); // original properties file key:
    // addbannedext.dialog.title
  }

  public AddBannedExtensionPage setBannedExt(String bannedExt) {
    bannedExtTextBox.clear();
    bannedExtTextBox.sendKeys(bannedExt);
    return get();
  }

  public AddBannedExtensionPage fail() {
    ok.click();
    return get();
  }

  public ContentRestrictionsPage ok(WaitingPageObject<ContentRestrictionsPage> returnTo) {
    ok.click();
    return returnTo.get();
  }

  public boolean textValidationExists() {
    return isPresent(
        By.xpath(
            "//div[contains(@class, 'ctrlinvalid')]/input[@id='"
                + BANNED_EXT_TEXT_BOX_ID
                + "']/../p[@class='ctrlinvalidmessage']"));
  }
}
