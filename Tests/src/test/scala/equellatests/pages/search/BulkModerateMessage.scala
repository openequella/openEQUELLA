package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.pages.WaitingBrowserPage
import org.openqa.selenium.By

case class BulkModerateMessage(commendFieldId: String)(val ctx: PageContext) extends WaitingBrowserPage {
  override def pageBy = By.id(commendFieldId)

  def comment : String = pageElement.getText
  def comment_=(v: String) : Unit = {
    pageElement.clear()
    pageElement.sendKeys(v)
  }
}

object BulkModerateMessage {
  val approveMessage = BulkModerateMessage("bwato_commentField") _
  val rejectMessage = BulkModerateMessage("bwrto2_commentField") _
}