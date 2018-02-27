package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import org.openqa.selenium.By

case class HomePage(ctx: PageContext) extends LoadablePage {
  val pageBy = By.xpath("//div[contains(@class, 'dashboard')]")

  override def load() = {
    driver.get(ctx.getBaseUrl + "home.do")
    get()
  }
}
