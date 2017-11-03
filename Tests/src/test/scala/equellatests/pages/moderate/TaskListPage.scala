package equellatests.pages.moderate

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.TitledPage
import equellatests.sections.search.{NamedResultList, QuerySection, TaskResult}
import org.openqa.selenium.By


case class TaskListPage(ctx:PageContext) extends TitledPage("My tasks", "access/tasklist.do")
  with QuerySection with NamedResultList {

  case class TaskListResult(pageBy: By) extends TaskResult {
    def ctx = TaskListPage.this.ctx
  }

  override def resultsUpdateExpectation = updatedBy(By.xpath("id('searchresults')[div[@class='itemlist'] or h3]/*[1]"))

  override type Result = TaskListResult

  override def resultFromBy = TaskListResult
}
