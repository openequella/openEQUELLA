package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import org.openqa.selenium.By

case class HomePage(ctx: PageContext) extends LoadablePage {
  val pageBy: By               = By.xpath("//div[contains(@class, 'dashboard')]")
  override val newUiPageBy: By = By.xpath("//h5[text()='Dashboard']")

  override def load(): this.type = {
    driver.get(ctx.getBaseUrl + "home.do")
    get()
  }
}
