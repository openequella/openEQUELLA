package equellatests.pages.search

import equellatests.pages.BrowserPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

trait SelectableResult extends BrowserPage {
  def resultBy : By
  def elem : WebElement = findElement(resultBy)

  def select(): Unit = {
    elem.findElement(By.xpath(".//button[normalize-space(text()) = 'Select']")).click()
    waitFor(ExpectedConditions.presenceOfNestedElementLocatedBy(resultBy, By.xpath("./div[contains(@class, 'selected')]")))
  }
}
