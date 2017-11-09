package equellatests.sections.search

import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By

trait FilterableSearchPage extends ResultsUpdatable {

  type Filters <: WaitingBrowserPage

  protected def filterPage: Filters

  def openFilters() : Filters = {
    val page = filterPage.pageExpectation
    findElement(By.xpath("id('actionbuttons')/button[normalize-space(text()) = 'Filter']")).click()
    waitFor(page)
  }

  def closeFilters() : Unit = ???

  def clearFiltersIfSet() : Unit = {
    findElementO(By.id("rf_resetButton")).foreach { b =>
      val up = resultsUpdateExpectation
      b.click()
      waitFor(up)
    }
  }
}
