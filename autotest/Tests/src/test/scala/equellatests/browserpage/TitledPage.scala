package equellatests.browserpage

import com.tle.common.URLUtils
import org.openqa.selenium.By

abstract class TitledPage(title: String, urlPath: String) extends LoadablePage {
  override def load(): this.type = {
    driver.get(URLUtils.appendQueryString(ctx.getBaseUrl + urlPath, "old=true"))
    get()
  }

  override def pageBy: By = CommonXPath.pageTitle(title)
}
