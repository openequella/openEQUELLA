package equellatests.sections.search

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.browserpage.{CommonXPath, WaitingBrowserPage}
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

trait BulkOperationDialog extends WaitingBrowserPage {

  def pageBy = By.id("bss_bulkDialog")

  type Parent <: WaitingBrowserPage

  def parent: Parent

  def execute(): Unit = {
    val exBy = CommonXPath.buttonWithText("Execute")
    waitFor(ExpectedConditions.visibilityOfNestedElementsLocatedBy(pageBy, exBy))
    pageElement.findElement(exBy).click()
    waitFor(
      ExpectedConditions.visibilityOfElementLocated(
        By.xpath("id('bss_bulkDialog')//p[text() = 'Operations finished']")
      )
    )
  }

  def cancel(): Parent = {
    val expect = parent.updatedExpectation()
    findElementById("bss_bulkDialog_close").click()
    waitFor(expect)
    parent
  }

  def selectAction(name: String): Unit = {
    val waitUp = updatedBy(By.xpath("id('bss_bulkDialogfooter')/*[1]"))
    val sel    = new EquellaSelect(ctx, findElementById("bss_bulkDialog_operationList"))
    if (sel.getSelectedText != name) {
      sel.selectByVisibleText(name)
      waitFor(waitUp)
    }
  }

  def next[P <: WaitingBrowserPage](p: PageContext => P): P = {
    pageElement.findElement(CommonXPath.buttonWithText("Next")).click()
    p(ctx).get()
  }
}
