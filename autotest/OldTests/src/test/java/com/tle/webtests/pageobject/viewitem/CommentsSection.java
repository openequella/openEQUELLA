package com.tle.webtests.pageobject.viewitem;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class CommentsSection extends AbstractPage<CommentsSection> {
  @FindBy(id = "sc_comments_c")
  private WebElement commentField;

  @FindBy(id = "sc_comments_add")
  private WebElement addButton;

  @FindBy(id = "sc_comments_a")
  private WebElement anonymousCheck;

  public CommentsSection(PageContext context) {
    // Changed to this By because it always exist regardless of privileges.
    // "comment-form" isn't there is ADD_COMMENT has been revoked.
    super(context, By.id("comments-list"));
    setMustBeVisible(false);
  }

  public void addComment(String comment, int rating) {
    addComment(comment, rating, false);
  }

  public void addComment(String comment, int rating, boolean doGet) {
    commentField.clear();
    commentField.sendKeys(comment);
    if (rating != 0) {
      driver
          .findElement(
              By.xpath(
                  "//div[@class='rate-stars']//a[text()="
                      + quoteXPath(Integer.toString(rating))
                      + "]"))
          .click();
    }
    addButton.click();
    if (!doGet && (!Check.isEmpty(comment) || rating != 0)) {
      waitForElement(By.xpath(xpathForComment(comment)));
    } else {
      get();
    }
  }

  public void addAnonymousComment(String comment, int rating) {
    if (!anonymousCheck.isSelected()) {
      anonymousCheck.click();
    }
    addComment(comment, rating);
  }

  public boolean isAddButtonClickable() {
    return addButton.isEnabled();
  }

  private String xpathForComment(String comment) {
    if (Check.isEmpty(comment)) {
      return "//div[@class='comment' and count(div[@class='comment-content']) = 0]";
    }
    return "//div[@class='comment' and div[@class='comment-content']/p[normalize-space(text())="
        + quoteXPath(comment)
        + "]]";
  }

  public boolean containsComment(String comment) {
    return isPresent(By.xpath(xpathForComment(comment)));
  }

  public void deleteComment(final String comment) {
    driver
        .findElement(By.xpath(xpathForComment(comment) + "/div[@class='comment-delete']/a"))
        .click();
    acceptConfirmation();

    waiter.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver arg0) {
            return Boolean.valueOf(
                !isPresent(By.xpath(xpathForComment(comment) + "/div[@class='comment-delete']/a")));
          }
        });
  }

  public boolean canDeleteComment(final String comment) {
    return isPresent(By.xpath(xpathForComment(comment) + "/div[@class='comment-delete']/a"));
  }

  public boolean hasComments() {
    return isPresent(By.id("comments-list-head"));
  }

  public boolean isAddingCommentsUnavailable() {
    return !isPresent(By.id("comments-add"));
  }

  public int ratingForComment(String comment) {
    WebElement starDiv =
        driver.findElement(
            By.xpath(xpathForComment(comment) + "//div[contains(@class, 'star-rating-static-')]"));
    return Integer.parseInt(
        starDiv.getAttribute("class").substring("star-rating-static-".length()));
  }

  public String getCommentOwner(String comment) {
    return driver
        .findElement(By.xpath(xpathForComment(comment) + "/div[@class='comment-username']/span"))
        .getText();
  }

  public boolean isAnonymousComment(String comment) {
    return getCommentOwner(comment).equals("Anonymous");
  }
}
