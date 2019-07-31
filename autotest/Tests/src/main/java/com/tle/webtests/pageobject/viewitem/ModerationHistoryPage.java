package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ModerationHistoryPage extends AbstractPage<ModerationHistoryPage> {
  @FindBy(id = "hc_detailsSelection_1")
  private WebElement includeEdits;

  @FindBy(id = "hc_detailsSelection_2")
  private WebElement showAllDetails;

  @FindBy(id = "historyevents")
  private WebElement ajaxDiv;

  @FindBy(id = "hc_h")
  private WebElement table;

  @FindBy(xpath = "//h2[normalize-space(text())='Moderation history']")
  private WebElement historyTitle;

  public ModerationHistoryPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return historyTitle;
  }

  private WaitingPageObject<ModerationHistoryPage> tableUpdate() {
    return ajaxUpdateExpect(ajaxDiv, table);
  }

  public boolean isShowEdits() {
    return includeEdits.isSelected();
  }

  public boolean isShowAllDetails() {
    return showAllDetails.isSelected();
  }

  public void setShowEdits(boolean on) {
    if (on != isShowEdits()) {
      WaitingPageObject<ModerationHistoryPage> waiter = tableUpdate();
      includeEdits.click();
      waiter.get();
    }
  }

  public void setShowAllDetails(boolean on) {
    if (on != isShowAllDetails()) {
      WaitingPageObject<ModerationHistoryPage> waiter = tableUpdate();
      showAllDetails.click();
      waiter.get();
    }
  }

  public int eventCount() {
    return table.findElements(By.xpath("tbody/tr")).size();
  }

  public String eventAtIndex(int i) {
    return new HistoryRow(table, i).get().getEvent();
  }

  public String commentAtIndex(int i) {
    HistoryRow historyRow = new HistoryRow(table, i);
    CommentDialog comment = historyRow.get().getComment();
    String commentText = comment.getCommentText();
    comment.close(removalWaiter(comment.getLoadedElement()));
    return commentText;
  }

  private static By getByForRowIndex(int i) {
    return By.xpath("tbody/tr[" + i + "]");
  }

  public class HistoryRow extends AbstractPage<HistoryRow> {
    @FindBy(xpath = "td[1]")
    private WebElement eventElement;

    @FindBy(linkText = "Show comment")
    private WebElement commentLink;

    public HistoryRow(SearchContext searchContext, int index) {
      super(ModerationHistoryPage.this.context, searchContext, getByForRowIndex(index));
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public String getEvent() {
      return eventElement.getText();
    }

    public boolean hasComment() {
      return isPresent(commentLink);
    }

    public CommentDialog getComment() {
      commentLink.click();
      return new CommentDialog().get();
    }
  }

  public class CommentDialog extends AbstractPage<CommentDialog> {
    @FindBy(id = "hc_commentDialog")
    private WebElement mainDiv;

    @FindBy(css = "div.modal-content-inner > div")
    private WebElement commentText;

    @FindBy(className = "modal_close")
    private WebElement closeButton;

    public CommentDialog() {
      super(ModerationHistoryPage.this.context);
    }

    @Override
    protected WebElement findLoadedElement() {
      return mainDiv;
    }

    public String getCommentText() {
      return commentText.getText();
    }

    public <T extends PageObject> T close(WaitingPageObject<T> returnTo) {
      closeButton.click();
      return returnTo.get();
    }
  }

  public boolean verifyShowCommentAtIndex(int i) {
    return new HistoryRow(table, i).get().hasComment();
  }
}
