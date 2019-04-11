package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ImageGalleryPage extends AbstractPage<ImageGalleryPage> {
  @FindBy(id = "fancybox-img")
  private WebElement image;

  @FindBy(id = "fancybox-title-main")
  private WebElement title;

  @FindBy(id = "fancybox-right")
  private WebElement right;

  @FindBy(id = "fancybox-left")
  private WebElement left;

  @FindBy(id = "fancybox-close")
  private WebElement close;

  public ImageGalleryPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return image;
  }

  public String getDisplayedFilename() {
    return image.getAttribute("alt");
  }

  public String getDisplayedSrc() {
    return image.getAttribute("src");
  }

  public String getDisplayedTitle() {
    waitForElement(title);
    return title.getText();
  }

  public ImageGalleryPage clickNext() {
    if (hasNext()) {
      WaitingPageObject<ImageGalleryPage> updateWaiter = updateWaiter(image);
      right.click();
      return updateWaiter.get();
    }
    return this;
  }

  public ImageGalleryPage clickPrevious() {
    if (hasPrevious()) {
      WaitingPageObject<ImageGalleryPage> updateWaiter = updateWaiter(image);
      left.click();
      return updateWaiter.get();
    }
    return this;
  }

  public <T extends PageObject> T closeGallery(WaitingPageObject<T> target) {
    WaitingPageObject<T> disappearWaiter = ExpectWaiter.waiter(removalCondition(image), target);
    close.click();
    return disappearWaiter.get();
  }

  public boolean hasNext() {
    return right.isDisplayed();
  }

  public boolean hasPrevious() {
    return left.isDisplayed();
  }
}
