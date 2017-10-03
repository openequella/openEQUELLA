package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.{By, WebElement}

trait QuerySection extends BrowserPage {

  def queryField : WebElement = findElement(By.name("q"))

  def query : String = queryField.getAttribute("value")

  def query_=(q: String) : Unit = {
    queryField.clear()
    queryField.sendKeys(q)
  }

  def searchButton : WebElement = findElement(By.id("searchform-search"))
  def resultsUpdateExpectation : ExpectedCondition[_]
  def search(): this.type = {
    val expect = resultsUpdateExpectation
    searchButton.click()
    waitFor(expect)
    this
  }

}
