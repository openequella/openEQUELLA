package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FavouriteItemDialog<T extends PageObject>
    extends AbstractPage<FavouriteItemDialog<T>> {

  @FindBy(xpath = "//button[normalize-space(text())='Add']")
  private WebElement saveButton;

  @FindBy(id = "fd_dialog_tf")
  private WebElement tagsInput;

  @FindBy(xpath = "//div[@id='fd_dialog_v']/input[@value='true']")
  private WebElement latestversion;

  @FindBy(xpath = "//div[@id='fd_dialog_v']/input[@value='false']")
  private WebElement thisversion;

  private WaitingPageObject<T> refresh;

  public FavouriteItemDialog(PageContext context, WaitingPageObject<T> refresh) {
    super(context, By.xpath("//h3[text()='Add to favourites:']"));
    this.refresh = refresh;
  }

  public FavouriteItemDialog<T> setTags(String tags) {
    tagsInput.clear();
    tagsInput.sendKeys(tags);
    return this;
  }

  public T clickAdd() {
    saveButton.click();
    return refresh.get();
  }

  public FavouriteItemDialog<T> setLatestVersion(boolean latest) {
    if (latest) {
      latestversion.click();
    } else {
      thisversion.click();
    }
    return this;
  }
}
