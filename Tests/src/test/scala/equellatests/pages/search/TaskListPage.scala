package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.moderate.ModerationView
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.{By, WebElement}


class TaskListPage(val ctx:PageContext) extends WaitingBrowserPage with QuerySection with NamedResultList {

  case class TaskListResult(pageBy: By) extends TaskResult {
    def ctx = TaskListPage.this.ctx
  }

  override def pageBy: By = By.xpath("id('header-inner')/div[text()='My tasks']")

  def load() = {
    driver.get(ctx.getBaseUrl + "access/tasklist.do")
    get()
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = TaskListResult

  override def resultFromBy = TaskListResult
}
