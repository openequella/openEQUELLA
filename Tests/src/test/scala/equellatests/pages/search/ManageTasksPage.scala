package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.moderate.ModerationView
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.By


class ManageTasksPage(val ctx:PageContext) extends WaitingBrowserPage with QuerySection with NamedResultList {

  case class ManageTasksBulkOps(ctx: PageContext, parent: ManageTasksPage) extends BulkOperationDialog
  {
    type Parent = ManageTasksPage
  }

  def performOperation(): BulkOperationDialog = {
    findElement(By.xpath("//input[@value = 'Perform an action']")).click()
    ManageTasksBulkOps(ctx, this).get()
  }


  case class ManageTaskResult(pageBy: By) extends TaskResult with SelectableResult {

    def ctx = ManageTasksPage.this.ctx
  }

  override def pageBy: By = By.xpath("id('header-inner')/div[text()='Manage tasks']")

  def load() = {
    driver.get(ctx.getBaseUrl + "access/managetasks.do")
    get()
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = ManageTaskResult

  override def resultFromBy = ManageTaskResult
}

