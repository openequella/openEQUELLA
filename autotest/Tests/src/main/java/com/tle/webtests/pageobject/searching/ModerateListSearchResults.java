package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class ModerateListSearchResults
    extends AbstractItemList<ModerateListSearchResults, ModerationSearchResult> {
  public ModerateListSearchResults(PageContext context) {
    super(context);
  }

  public ModerationView moderate(String title) {
    return getResultForTitle(title, 1).moderate();
  }

  public ModerationView moderate(PrefixedName title) {
    return moderate(title.toString());
  }

  public String getStepName(String title) {
    return getResultForTitle(title, 1).getStepName();
  }

  @Override
  protected ModerationSearchResult createResult(SearchContext searchContext, By by) {
    return new ModerationSearchResult(this, searchContext, by);
  }
}
