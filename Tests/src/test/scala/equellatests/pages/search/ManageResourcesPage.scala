package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.moderate.ModerationView
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.{By, WebElement}



class ManageResourcesPage(val ctx:PageContext) extends WaitingBrowserPage with QuerySection with NamedResultList {


  case class ManageResourcesResult(resultBy: By) extends BrowserPage with SelectableResult {
    def moderate(): ModerationView = {
      elem.findElement(By.xpath(".//button[normalize-space(text()) = 'Moderate']")).click()
      new ModerationView(ctx).get()
    }

    override def ctx: PageContext = ManageResourcesPage.this.ctx
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

