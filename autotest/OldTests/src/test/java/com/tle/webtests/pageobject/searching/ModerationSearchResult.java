package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.EBy;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ModerationSearchResult extends AbstractItemSearchResult<ModerationSearchResult> {

  public ModerationSearchResult(AbstractResultList<?, ?> page, SearchContext searchContext, By by) {
    super(page, searchContext, by);
  }

  public String getStepName() {
    return getDetailText("Task");
  }

  public ModerationView moderate() {
    WebElement moderateBtn = resultDiv.findElement(EBy.buttonText("Moderate"));
    ((JavascriptExecutor) driver).executeScript("arguments[0].click()", moderateBtn);
    return new ModerationView(context).get();
  }
}
