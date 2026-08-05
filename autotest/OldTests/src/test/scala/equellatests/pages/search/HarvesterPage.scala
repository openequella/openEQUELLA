package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import org.openqa.selenium.By

case class HarvesterPage(ctx: PageContext) extends LoadablePage {
  override def load() = {
    driver.get(ctx.getBaseUrl + "harvest.do")
    get()
  }

  override def pageBy = By.xpath("//div[h1[text() = 'Pages'] and ul[@class = 'pages']]")
}
