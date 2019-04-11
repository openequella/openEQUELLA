package com.tle.webtests.pageobject.wizard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SelectThumbnailDialog extends AbstractPage<SelectThumbnailDialog> {
  @FindBy(id = "2_save")
  private WebElement saveButton;

  @FindBy(id = "2_cancel")
  private WebElement cancelButton;

  @FindBy(id = "2_options_0")
  private WebElement defaultOption;

  @FindBy(id = "2_options_1")
  private WebElement noneOption;

  @FindBy(id = "2_options_2")
  private WebElement customOption;

  public SelectThumbnailDialog(PageContext context) {
    super(context, By.className("select_options"), -1);
  }

  public void selectDefault() {
    defaultOption.click();
  }

  public void selectNone() {
    noneOption.click();
  }

  public void selectCustom() {
    customOption.click();
  }

  public void selectCustomThumbnail(String title) {
    WebElement image =
        driver.findElement(
            By.xpath("//div[@class='thumbcontainer']/ul/li/a/img[@title='" + title + "']"));
    image.click();
  }

  public <T extends PageObject> T saveDialog(WaitingPageObject<T> targetPage) {
    saveButton.click();
    return targetPage.get();
  }

  public <T extends PageObject> T cancelDialog(WaitingPageObject<T> targetPage) {
    cancelButton.click();
    return targetPage.get();
  }
}
