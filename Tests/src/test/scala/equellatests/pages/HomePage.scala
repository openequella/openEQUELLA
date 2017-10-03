package equellatests.pages

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By

class HomePage(val ctx: PageContext) extends WaitingBrowserPage {
  val pageBy = By.xpath("//div[@class='dashboard']")
}
