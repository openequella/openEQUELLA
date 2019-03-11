package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class ModerationSearchResult extends AbstractItemSearchResult<ModerationSearchResult> {

  public ModerationSearchResult(AbstractResultList<?, ?> page, SearchContext searchContext, By by) {
    super(page, searchContext, by);
  }

  public String getStepName() {
    return getDetailText("Task");
  }

  public ModerationView moderate() {
    resultDiv.findElement(EBy.buttonText("Moderate")).click();
    return new ModerationView(context).get();
  }
}
