package equellatests.pages

import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

trait WaitingBrowserPage extends BrowserPage {
  def pageBy : By
  def updatedExpectation(): ExpectedCondition[_] = ExpectedConditions.and(ExpectedConditions.stalenessOf(pageElement), mainExpectation)
  def pageElement: WebElement = driver.findElement(pageBy)
  def mainExpectation: ExpectedCondition[_] = ExpectedConditions.visibilityOfElementLocated(pageBy)
  def pageExpectation: ExpectedCondition[this.type] = new ExpectedCondition[this.type] {
    def apply(f: WebDriver): WaitingBrowserPage.this.type = {
      waitFor(mainExpectation)
      WaitingBrowserPage.this
    }
  }

  def get() : this.type = {
    waitFor(pageExpectation)
  }

}
