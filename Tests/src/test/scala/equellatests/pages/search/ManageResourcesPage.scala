package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.moderate.ModerationView
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.{By, WebElement}



class ManageResourcesPage(val ctx:PageContext) extends WaitingBrowserPage with QuerySection with NamedResultList {


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

  override def pageBy: By = By.xpath("id('header-inner')/div[text()='Manage resources']")

  def load() = {
    driver.get(ctx.getBaseUrl + "access/itemadmin.do")
    get()
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = ManageResourcesResult

  override def resultFromBy = ManageResourcesResult
}

