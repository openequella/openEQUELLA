package com.tle.webtests.pageobject.tasklist;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class ModerationCommentsPage extends AbstractPage<ModerationCommentsPage> {

  public ModerationCommentsPage(PageContext context) {
    super(context, By.id("moderate"));
  }

  private String xpathForComment(String comment) {
    if (Check.isEmpty(comment)) {
      return "//div[@class='modcomment' and count(div[@class='comment-content']) = 0]";
    }
    return "//div[contains(@class,'modcomment') and .//div[@class='modcomment-content' and"
        + " normalize-space(text()) = "
        + quoteXPath(comment)
        + "]]";
  }

  public boolean containsComment(String comment) {
    return isPresent(By.xpath(xpathForComment(comment)));
  }

  public String getCommentClass(String comment) {
    return driver.findElement(By.xpath(xpathForComment(comment))).getAttribute("class");
  }
}
