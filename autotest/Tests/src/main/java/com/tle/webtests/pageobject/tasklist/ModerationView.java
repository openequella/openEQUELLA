package com.tle.webtests.pageobject.tasklist;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.wizard.ApproveMessagePage;
import com.tle.webtests.pageobject.wizard.CommentMessagePage;
import com.tle.webtests.pageobject.wizard.ModerationMessagePage;
import com.tle.webtests.pageobject.wizard.RejectMessagePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ModerationView extends AbstractPage<ModerationView> {
  @FindBy(className = "moderate-reject")
  private WebElement rejectButton;

  @FindBy(className = "moderate-approve")
  private WebElement approveButton;

  @FindBy(id = "_tasks_assignButton")
  private WebElement assignLink;

  @FindBy(id = "_tasks_listButton")
  private WebElement taskNavigationLink;

  @FindBy(id = "_tasks_nextButton")
  private WebElement nextTaskButton;

  @FindBy(id = "_tasks_prevButton")
  private WebElement prevTaskButton;

  @FindBy(id = "_tasks_postButton")
  private WebElement postCommentLink;

  public ModerationView(PageContext context) {
    super(context, By.id("moderate"));
  }

  public ModerationMessagePage reject() {
    rejectButton.click();
    return new RejectMessagePage(context).get();
  }

  public void accept() {
    acceptToMessagePage().acceptWithMessage("");
  }

  public ModerationMessagePage acceptToMessagePage() {
    approveButton.click();
    return new ApproveMessagePage(context).get();
  }

  public ModerationView assignToMe() {
    assignLink.click();
    return get();
  }

  public boolean isAssignedToMe() {
    return assignLink.getText().equalsIgnoreCase("cancel assignment");
  }

  public ModerationCommentsPage moderationComments() {
    return new ModerationCommentsPage(context).get();
  }

  public String getTaskNavigationInfo() {
    return taskNavigationLink.getText();
  }

  public ModerationView navigateNext() {
    nextTaskButton.click();
    return new ModerationView(context).get();
  }

  public ModerationView navigatePrev() {
    prevTaskButton.click();
    return new ModerationView(context).get();
  }

  // When disabled they are rendered as spans, links (a) when enabled
  public boolean navigationDisabled() {
    if (nextTaskButton.getTagName().equalsIgnoreCase("span")
        && prevTaskButton.getTagName().equalsIgnoreCase("span")) {
      return true;
    } else {
      return false;
    }
  }

  // Return true if the approve/reject buttons are disabled
  public boolean moderationDisabled() {
    boolean reject = "true".equalsIgnoreCase(rejectButton.getAttribute("disabled"));
    boolean approve = "true".equalsIgnoreCase(approveButton.getAttribute("disabled"));
    return reject && approve;
  }

  public ModerationMessagePage postComment() {
    postCommentLink.click();
    return new CommentMessagePage(context).get();
  }

  private String xpathForComment(String comment) {
    if (Check.isEmpty(comment)) {
      return "//div[@class='comment' and count(div[@class='comment-content']) = 0]";
    }
    return "//div[@class[contains(.,'comment')] and contains(div[@class='comment-content'],'"
        + comment
        + "')]";
  }
}
