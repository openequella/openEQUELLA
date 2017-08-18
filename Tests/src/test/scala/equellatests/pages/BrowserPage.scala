package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.pages
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

import scala.util.Try

trait BrowserPage {
  def ctx: PageContext
  def driver : WebDriver = ctx.getDriver
  val waiter = new WebDriverWait(driver, 10, 50L)
  def waitFor[A](c: ExpectedCondition[A]) : A = waiter.until(c)
  def findElementO(by: By): Option[WebElement] = Try(driver.findElement(by)).toOption
  def pageBy : By
  def updatedExpectation(): ExpectedCondition[_] = ExpectedConditions.and(ExpectedConditions.stalenessOf(pageElement), mainExpectation)
  def pageElement: WebElement = driver.findElement(pageBy)
  def mainExpectation: ExpectedCondition[_] = ExpectedConditions.visibilityOfElementLocated(pageBy)
  def pageExpectation: ExpectedCondition[this.type] = new ExpectedCondition[this.type] {
    def apply(f: WebDriver): BrowserPage.this.type = {
      waitFor(mainExpectation)
      BrowserPage.this
    }
  }

  def get() : this.type = {
    waitFor(pageExpectation)
  }

  def quoteXPath(input: String): String = {
    val txt = input
    if (txt.indexOf("'") > -1 && txt.indexOf("\"") > -1) "concat('" + txt.replace("'", "', \"'\", '") + "')"
    else if (txt.indexOf("\"") > -1) "'" + txt + "'"
    else "\"" + txt + "\""
  }
}
