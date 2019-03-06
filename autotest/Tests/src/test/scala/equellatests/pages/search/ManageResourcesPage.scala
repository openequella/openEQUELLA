package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.browserpage.{TitledPage, WaitingBrowserPage}
import equellatests.sections.search._
import org.openqa.selenium.By



case class ManageResourcesPage(ctx:PageContext) extends TitledPage("Manage resources", "access/itemadmin.do")
  with QuerySection with NamedResultList with FilterableSearchPage {


  case object ManageResourceFilters extends WaitingBrowserPage
  {
    def onlyModeration(yes: Boolean) : Unit = {
      val modCheck = driver.findElement(By.id("modonly"))
      if (yes != modCheck.isSelected) {
        val update = resultsUpdateExpectation
        modCheck.click()
        waitFor(update)
      }
    }

    def filterByWorkflow(workflowName: Option[String]) : Unit = {
      val sel = new EquellaSelect(ctx, findElement(By.id("workflow")))
      val update = resultsUpdateExpectation
      sel.selectByVisibleText(workflowName.getOrElse("Within all workflows"))
      waitFor(update)
    }

    override def pageBy: By = By.id("owner")

    override def ctx: PageContext = ManageResourcesPage.this.ctx
  }

  case object ManageResourcesBulkOps extends BulkOperationDialog
  {
    type Parent = ManageResourcesPage
    def parent = ManageResourcesPage.this
    def ctx = ManageResourcesPage.this.ctx
  }

  def performOperation(): BulkOperationDialog = {
    findElement(By.xpath("//input[@value = 'Perform an action']")).click()
    ManageResourcesBulkOps.get()
  }

  case class ManageResourcesResult(pageBy: By) extends SelectableResult with MetadataResult {
    def status : String = {
      val fs = metadataText("Status:")
      fs.indexOf('|') match {
        case -1 => fs
        case i => fs.substring(0, i)
      }
    }

    override def ctx = ManageResourcesPage.this.ctx
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = ManageResourcesResult

  override def resultFromBy = ManageResourcesResult

  override type Filters = ManageResourceFilters.type

  override protected def filterPage = ManageResourceFilters
}

