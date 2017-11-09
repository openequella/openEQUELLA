package equellatests.sections.search

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By

case class ResetToTaskConfigPage(ctx: PageContext) extends WaitingBrowserPage {
  def selectTask(task: String): Unit = {
    val sel = new EquellaSelect(ctx, findElementById("task"))
    sel.selectByVisibleText(task)
  }

  def comment_=(msg: String) : Unit = {
    val field = findElementById("bwtmo_commentField")
    field.clear()
    field.sendKeys(msg)
  }

  override def pageBy = By.xpath("//h3[text()='Resetting to another workflow task']")
}
