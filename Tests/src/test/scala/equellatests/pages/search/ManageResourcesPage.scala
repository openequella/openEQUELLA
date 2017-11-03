package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.TitledPage
import equellatests.sections.search.{MetadataResult, NamedResultList, QuerySection, SelectableResult}
import org.openqa.selenium.By



case class ManageResourcesPage(ctx:PageContext) extends TitledPage("Manage resources", "access/itemadmin.do")
  with QuerySection with NamedResultList {

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
}

