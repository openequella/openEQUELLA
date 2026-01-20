package equellatests.browserpage

import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}
import org.openqa.selenium.{By, WebDriver, WebElement}

trait WaitingBrowserPage extends BrowserPage {
  // The By locator to determine if the page is existing.
  def pageBy: By
  // The By locator to determine if the page is existing when the page is in new UI mode.
  // By default, it is the same as pageBy, since new UI is still being rolled out.
  def newUiPageBy: By = pageBy

  def updatedExpectation(): ExpectedCondition[_] =
    ExpectedConditions.and(ExpectedConditions.stalenessOf(pageElement), mainExpectation)
  def pageElement: WebElement = findElement(pageBy)
  def mainExpectation: ExpectedCondition[_] = ExpectedConditions.or(
    ExpectedConditions.visibilityOfElementLocated(pageBy),
    ExpectedConditions.visibilityOfElementLocated(newUiPageBy)
  )
  def pageExpectation: ExpectedCondition[this.type] = new ExpectedCondition[this.type] {
    def apply(f: WebDriver): WaitingBrowserPage.this.type = {
      waitFor(mainExpectation)
      WaitingBrowserPage.this
    }
  }

  def exists: Boolean = findElementO(pageBy).isDefined || findElementO(newUiPageBy).isDefined

  def get(): this.type = {
    waitFor(pageExpectation)
  }

}
