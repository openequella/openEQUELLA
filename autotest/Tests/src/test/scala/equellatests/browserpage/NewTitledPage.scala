package equellatests.browserpage
import org.openqa.selenium.By

abstract class NewTitledPage(title: String, urlPath: String) extends LoadablePage {
  override def load(): this.type = {
    driver.get(ctx.getBaseUrl + urlPath)
    get()
  }

  override def pageBy: By = CommonXPath.newPageTitle(title)

}
