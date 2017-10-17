package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.WaitingBrowserPage
import org.openqa.selenium.By

abstract class BulkModerateMessage(commendFieldId: String) extends WaitingBrowserPage {
  override def pageBy = By.id(commendFieldId)

  def comment : String = pageElement.getText
  def comment_=(v: String) : Unit = {
    pageElement.clear()
    pageElement.sendKeys(v)
  }
}

case class BulkApproveMessage(ctx: PageContext) extends BulkModerateMessage("bwato_commentField")

case class BulkRejectMessage(ctx: PageContext) extends BulkModerateMessage("bwrto2_commentField")
