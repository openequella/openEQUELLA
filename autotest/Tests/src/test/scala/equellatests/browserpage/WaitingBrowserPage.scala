package equellatests.browserpage

import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}
import org.openqa.selenium.{By, WebDriver, WebElement}

trait WaitingBrowserPage extends BrowserPage {
  def pageBy : By
  def updatedExpectation(): ExpectedCondition[_] = ExpectedConditions.and(ExpectedConditions.stalenessOf(pageElement), mainExpectation)
  def pageElement: WebElement = findElement(pageBy)
  def mainExpectation: ExpectedCondition[_] = ExpectedConditions.visibilityOfElementLocated(pageBy)
  def pageExpectation: ExpectedCondition[this.type] = new ExpectedCondition[this.type] {
    def apply(f: WebDriver): WaitingBrowserPage.this.type = {
      waitFor(mainExpectation)
      WaitingBrowserPage.this
    }
  }

  def exists : Boolean = findElementO(pageBy).isDefined

  def get() : this.type = {
    waitFor(pageExpectation)
  }

}
