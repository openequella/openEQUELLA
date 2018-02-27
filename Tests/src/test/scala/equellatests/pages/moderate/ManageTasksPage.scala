package equellatests.pages.moderate

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.{CommonXPath, LoadablePage, TitledPage}
import equellatests.sections.search._
import org.openqa.selenium.By


case class ManageTasksPage(ctx:PageContext) extends TitledPage("Manage tasks", "access/managetasks.do") with QuerySection with NamedResultList {

  case class ManageTasksBulkOps(parent: ManageTasksPage) extends BulkOperationDialog
  {
    type Parent = ManageTasksPage
    def ctx = ManageTasksPage.this.ctx
  }

  def performOperation(): BulkOperationDialog = {
    findElement(By.xpath("//input[@value = 'Perform an action']")).click()
    ManageTasksBulkOps(this).get()
  }


  case class ManageTaskResult(pageBy: By) extends TaskResult with SelectableResult {


    def ctx = ManageTasksPage.this.ctx
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = ManageTaskResult

  override def resultFromBy = ManageTaskResult
}

