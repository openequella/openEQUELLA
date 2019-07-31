package com.tle.webtests.pageobject.remoterepo;

import com.tle.webtests.pageobject.searching.SearchResult;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

public class RemoteRepoSearchResult extends SearchResult<RemoteRepoSearchResult> {

  public RemoteRepoSearchResult(RemoteRepoListPage page, SearchContext relativeTo, By by) {
    super(page, relativeTo, by);
  }

  public <T extends RemoteRepoViewResultPage<T>> T viewResult(T type) {
    clickTitle();
    return type.get();
  }
}
