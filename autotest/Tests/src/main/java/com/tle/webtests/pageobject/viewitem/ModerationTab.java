package com.tle.webtests.pageobject.viewitem;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ModerationTab extends AbstractPage<ModerationTab> {
  @FindBy(xpath = "//img[@class='modal_close']/@src")
  private WebElement commentsCloseButton;

  public ModerationTab(PageContext context) {
    super(context, By.xpath("//h2[text()='Moderation progress']"));
  }

  public boolean isTaskPresent(String task) {
    return isPresent(
        By.xpath(
            "//h3[text()='Tasks awaiting"
                + " moderation']/following-sibling::table[1]/tbody/tr/td[text()="
                + quoteXPath(task)
                + "]"));
  }

  public SummaryPage summary() {
    driver.findElement(By.xpath("id('breadcrumbs')//a[2]")).click();
    return new SummaryPage(context).get();
  }

  public boolean moderatorCorrect(int row, String moderator) {

    return isPresent(
        By.xpath(
            "//table[@id='cmc_m']/tbody/tr["
                + quoteXPath(row)
                + "]/td[2]/ul[@class='moderators']/li/span[text()="
                + quoteXPath(moderator)
                + "]"));
  }

  public long waitTime() throws ParseException {
    WebElement time = driver.findElement(By.xpath("//div[1]/span/abbr[@class='timeago_nosuf']"));
    String timeStamp = time.getAttribute("title");
    // Since Java 20, the "NARROW NO-BREAK SPACE" character is used before the AM/PM marker.
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy, hh:mm\u202Fa");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Australia/Hobart"));
    Date d = dateFormat.parse(timeStamp);
    return d.getTime();
  }

  public int getNumberModerationComments(int row) {
    WebElement moderationComments =
        driver.findElement(
            By.xpath("//table[@id='cmc_m']/tbody/tr[" + quoteXPath(row) + "]/td[2]/a"));
    String modComText = moderationComments.getText();
    String number = modComText.substring(0, 1);
    return Integer.parseInt(number);
  }

  public void openModerationComments(int row) {
    driver
        .findElement(By.xpath("//table[@id='cmc_m']/tbody/tr[" + quoteXPath(row) + "]/td[2]/a"))
        .click();
    waitForElement(By.id("comments-list"));
  }

  public void closeComments() {
    commentsCloseButton.click();
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
    String commentXpath = xpathForComment(comment);
    return isPresent(By.xpath(commentXpath));
  }

  public String getCommentClass(String comment) {
    return driver.findElement(By.xpath(xpathForComment(comment))).getAttribute("class");
  }
}
