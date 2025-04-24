package equellatests.pages.viewitem

import java.util.UUID

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.WaitingBrowserPage
import equellatests.domain.ItemId
import equellatests.sections.wizard.WizardPageTab
import org.http4s.Uri
import org.openqa.selenium.By

class SummaryPage(val ctx: PageContext) extends WaitingBrowserPage {

  override def pageBy: By = By.xpath("//div[@class='itemsummary-layout']")

  private def getActionBy(actionText: String) =
    By.xpath("//a[normalize-space(text())=" + quoteXPath(actionText) + "]")

  private def clickAction(action: String): Unit = driver.findElement(getActionBy(action)).click()

  def edit(): WizardPageTab = {
    clickAction("Edit this version")
    new WizardPageTab(ctx, 0).get()
  }

  val ItemsRegex = """items/(.*)/(.*)/""".r

  def itemId(): ItemId = {
    val cu = Uri.unsafeFromString(driver.getCurrentUrl).path.renderString
    val bu = Uri.unsafeFromString(ctx.getBaseUrl).path.renderString
    cu.substring(bu.length) match {
      case ItemsRegex(uuidS, ver) => ItemId(UUID.fromString(uuidS), ver.toInt)
    }
  }
}
