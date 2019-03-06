package equellatests.browserpage

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By

case class ErrorPage(ctx: PageContext) extends WaitingBrowserPage {
  override def pageBy = By.xpath("//div[contains(@class, 'error') and contains(@class, 'area')")
}
