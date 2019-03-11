package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LargeImageViewerPage extends AbstractPage<LargeImageViewerPage> {
  @FindBy(id = "liv_zoomIn")
  private WebElement zoomInButton;

  public LargeImageViewerPage(PageContext context) {
    super(context, By.id("viewer"));
  }

  @Override
  public void checkLoaded() throws Error {
    if (isPresent(By.id("liv_startButton"))) {
      driver.findElement(By.id("liv_startButton")).click();
    }
    super.checkLoaded();
  }

  public boolean zoomInButtonExists() {
    return isVisible(zoomInButton);
  }
}
