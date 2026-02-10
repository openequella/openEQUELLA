package io.github.openequella.pages.manage

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.searching.{
  AbstractQueryableSearchPage,
  ModerateListSearchResults,
  ModerationSearchResult
}
import org.openqa.selenium.{By, WebElement}

class ManageResourcesPage(context: PageContext)
    extends AbstractQueryableSearchPage[
      ManageResourcesPage,
      ModerateListSearchResults,
      ModerationSearchResult
    ](context) {
  loadedBy = By.xpath("//h5[text()='Manage resources']")

  override protected def findLoadedElement: WebElement = driver.findElement(loadedBy)

  override def resultsPageObject = new ModerateListSearchResults(context)

  /** Gets the number of results currently displayed on the page.
    */
  def getResultSize: Int = results.getResults.size
}
