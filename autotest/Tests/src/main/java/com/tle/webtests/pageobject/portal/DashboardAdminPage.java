package com.tle.webtests.pageobject.portal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DashboardAdminPage
    extends AbstractQueryableSearchPage<DashboardAdminPage, ItemListPage, ItemSearchResult> {
  @FindBy(id = "searchresults")
  private WebElement mainElem;

  public DashboardAdminPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return mainElem;
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/portaladmin.do");
  }

  public void deleteAllPortlet(String query) {
    ItemListPage portals = search(query);

    while (portals.isResultsAvailable()) {
      ItemSearchResult result = portals.getResult(1);
      result.clickActionConfirm("Delete", true, removalWaiter(result.getLoadedElement()));
    }
  }

  public void deleteAllPortlet() {
    deleteAllPortlet("");
  }

  public ItemListPage search(String query) {
    return querySection.search(query, resultsPageObject.getUpdateWaiter());
  }

  public <P extends AbstractPortalEditPage<P>> P addPortal(P portal) {
    return new PortalScreenOptions(context).open().addPortal(portal);
  }

  @Override
  public ItemListPage resultsPageObject() {
    return new ItemListPage(context);
  }
}
