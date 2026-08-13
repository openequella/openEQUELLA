package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ModerationQueueShowCommentDialog
    extends AbstractPage<ModerationQueueShowCommentDialog> {

  // Close Button
  @FindBy(className = "modal_close")
  private WebElement dialogClose;

  public ModerationQueueShowCommentDialog(PageContext context) {
    super(context, By.id("mqil_commentDialog"));
  }

  public void closeComment() {
    dialogClose.click();
  }

  public String getComment() {
    return driver
        .findElement(By.className("modal-content-inner"))
        .findElement(By.tagName("div"))
        .getText();
  }
}
