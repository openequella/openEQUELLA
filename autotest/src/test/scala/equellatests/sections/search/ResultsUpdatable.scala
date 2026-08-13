package equellatests.sections.search

import equellatests.browserpage.BrowserPage
import org.openqa.selenium.support.ui.ExpectedCondition

trait ResultsUpdatable extends BrowserPage {

  def resultsUpdateExpectation: ExpectedCondition[_]

}
