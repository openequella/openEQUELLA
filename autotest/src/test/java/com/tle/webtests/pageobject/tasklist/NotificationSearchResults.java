package com.tle.webtests.pageobject.tasklist;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractItemList;
import com.tle.webtests.pageobject.searching.ModerationSearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class NotificationSearchResults
    extends AbstractItemList<NotificationSearchResults, ModerationSearchResult> {
  public NotificationSearchResults(PageContext context) {
    super(context);
  }

  public boolean isReasonRejected(String itemFullName) {
    String reason = getResultForTitle(itemFullName, 1).getDetailText("Reason");
    return reason.equals("Rejected");
  }

  @Override
  protected ModerationSearchResult createResult(SearchContext searchContext, By by) {
    return new ModerationSearchResult(this, searchContext, by);
  }

  public NotificationSearchResults clearNotification(String title, int index) {
    ModerationSearchResult result = getResultForTitle(title, index);
    return result.clickAction("Clear", removalWaiter(result.getLoadedElement()));
  }
}
