package equellatests.sections.search

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By

case class BulkOpConfirm(title: String)(val ctx: PageContext) extends WaitingBrowserPage {
  override def pageBy = By.xpath(s"id('bulkresults_inner')/h3[text()=${quoteXPath(title)}]")
}
