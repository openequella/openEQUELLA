package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class FavouriteItemsPage
    extends AbstractQueryableSearchPage<FavouriteItemsPage, ItemListPage, ItemSearchResult> {
  private static final String TYPE = "Resources";
  private final FavouritesPage favPage;

  public FavouriteItemsPage(PageContext context, FavouritesPage favPage) {
    super(context);
    this.favPage = favPage;
  }

  @Override
  protected WebElement findLoadedElement() {
    return favPage.findLoadedElement();
  }

  @Override
  protected AbstractQuerySection<?> createQuerySection() {
    return new FilterByKeywordPage(context, "ifbakw_s");
  }

  public FavouriteItemsPage loadIfRequired() {
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

  public boolean hasResults() {
    return !isPresent(By.xpath("//div[@id='searchresults']//h3[text()='You have no favourites']"));
  }

  @Override
  public ItemListPage resultsPageObject() {
    return new ItemListPage(context);
  }

  @Override
  protected String getFilterOpenerId() {
    return "isra_filter";
  }

  @Override
  public ItemListPage search(String query) {
    openFilters();
    return super.search(query);
  }
}
