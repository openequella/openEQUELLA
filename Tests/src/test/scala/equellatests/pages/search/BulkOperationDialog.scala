package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.pages.{BrowserPage, CommonXPath, WaitingBrowserPage}
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

trait BulkOperationDialog extends WaitingBrowserPage {

  def pageBy = By.id("bss_bulkDialog")

  type Parent <: WaitingBrowserPage

  def parent : Parent

  def execute() : Unit = {
    val exBy = CommonXPath.buttonWithText("Execute")
    waitFor(ExpectedConditions.visibilityOfNestedElementsLocatedBy(pageBy, exBy))
    pageElement.findElement(exBy).click()
    waitFor(ExpectedConditions.visibilityOfNestedElementsLocatedBy(pageBy, By.xpath(".//p[text() = 'Operations finished']")))
  }

  def cancel() : Parent = {
    val expect = parent.updatedExpectation()
    findElementById("bss_bulkDialog_close").click()
    waitFor(expect)
    parent
  }

  def selectAction(name: String) : Unit = {
    new EquellaSelect(ctx, findElementById("bss_bulkDialog_operationList")).selectByVisibleText(name)
  }

  def next[P <: WaitingBrowserPage](p: PageContext => P): P = {
    pageElement.findElement(CommonXPath.buttonWithText("Next")).click()
    p(ctx).get()
  }
}
