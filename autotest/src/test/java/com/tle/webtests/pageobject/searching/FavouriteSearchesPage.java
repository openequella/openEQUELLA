package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class FavouriteSearchesPage extends AbstractPage<FavouriteSearchesPage> {
  private static final String TYPE = "Searches";
  private final FavouritesPage favPage;

  public FavouriteSearchesPage(PageContext context, FavouritesPage favPage) {
    super(context, favPage.xpathForPage(TYPE));
    this.favPage = favPage;
  }

  public FavouriteSearchesPage loadIfRequired() {
    if (!isLoaded()) {
      favPage.clickType(TYPE);
    }
    return get();
  }

  @Override
  public void checkLoaded() throws Error {
    favPage.checkLoaded();
    super.checkLoaded();
  }

  public void open(String searchName) {
    results().getResultForTitle(searchName, 1).clickTitle();
  }

  public <T extends AbstractSearchPage<T, ?, ?>> T open(String searchName, T returnType) {
    results().getResultForTitle(searchName, 1).clickTitle();
    return returnType.get();
  }

  public FavouriteSearchList results() {
    return new FavouriteSearchList(context).get();
  }

  public boolean delete(String searchName) {
    FavouriteSearchList results = results();
    if (results.doesResultExist(searchName, 1)) {
      ItemSearchResult result = results.getResultForTitle(searchName, 1);
      result.clickActionConfirm("Remove", true, removalWaiter(result.getLoadedElement()));
      return true;
    }
    return false;
  }

  // doesn't account for repeated results
  public void deleteAllNamed(String... searchNames) {
    FavouriteSearchList results = results();
    for (String name : searchNames) {
      if (results.doesResultExist(name)) {
        delete(name);
      }
    }
  }

  public boolean hasResults() {
    return !isPresent(
        By.xpath("//div[@id='searchresults']//h3[text() ='You have no favourites')]"));
  }
}
