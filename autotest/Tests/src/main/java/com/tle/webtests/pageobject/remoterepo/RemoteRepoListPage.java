package com.tle.webtests.pageobject.remoterepo;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.SkipException;
import com.tle.webtests.pageobject.searching.AbstractResultList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RemoteRepoListPage
    extends AbstractResultList<RemoteRepoListPage, RemoteRepoSearchResult> {
  @FindBy(id = "searchresults")
  private WebElement resultDiv;

  public RemoteRepoListPage(PageContext context) {
    super(context, 120);
  }

  @Override
  public WebElement getResultsDiv() {
    return resultDiv;
  }

  @Override
  public void checkLoaded() throws Error {
    List<WebElement> loadingImage = driver.findElements(By.xpath("id('searchresults')/img"));
    if (!loadingImage.isEmpty()) {
      throw new Error("Still ajax loading");
    }
    super.checkLoaded();
  }

  @Override
  protected void isError() {
    if (isPresent(By.xpath("//div[@class='searchError']/p[contains(text(),'Read timed out')]"))) {
      throw new SkipException("Remote repo timed out...");
    }
    if (isPresent(
        By.xpath(
            "//div[@class='searchError']/p[contains(text(),'The host did not accept the"
                + " connection')]"))) {
      throw new SkipException("Remote repo timed out...");
    }
    super.isError();
  }

  @Override
  protected RemoteRepoSearchResult createResult(SearchContext searchContext, By by) {
    return new RemoteRepoSearchResult(this, searchContext, by);
  }
}
