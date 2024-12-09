package equellatests.sections.search

import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

trait SelectableResult extends WaitingBrowserPage {
  def select(): Unit = {
    pageElement.findElement(By.xpath(".//button[normalize-space(text()) = 'Select']")).click()
    waitFor(
      ExpectedConditions
        .presenceOfNestedElementLocatedBy(pageBy, By.xpath("./div[contains(@class, 'selected')]"))
    )
  }
}
