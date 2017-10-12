package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.moderate.ModerationView
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.{By, WebElement}

case class TaskListResult(val ctx: PageContext)(foundBy: By) extends BrowserPage {

  def elem : WebElement = findElement(foundBy)

  def moderate(): ModerationView = {
    elem.findElement(By.xpath("//button[normalize-space(text()) = 'Moderate']")).click()
    new ModerationView(ctx).get()
  }

}

class TaskListPage(val ctx:PageContext) extends WaitingBrowserPage with QuerySection with NamedResultList {


  override def pageBy: By = By.xpath("id('header-inner')/div[text()='My tasks']")

  def load() = {
    driver.get(ctx.getBaseUrl + "access/tasklist.do")
    get()
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = TaskListResult

  override def resultFromBy = TaskListResult(ctx)
}
