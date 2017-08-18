package equellatests.pages.wizard

import com.tle.webtests.framework.PageContext
import equellatests.pages.BrowserPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}

class ContributePage(val ctx: PageContext) extends BrowserPage {

  val pageBy = By.className("contribution-selection-page")

  def load() = {
    driver.get(ctx.getBaseUrl + "access/contribute.do")
    get()
  }

  def openWizard(name: String) : WizardPageTab = {
    pageElement.findElement(By.xpath(".//table[contains(@class, 'zebra')]//a[normalize-space(text())=" + quoteXPath(name) + "]")).click()
    new WizardPageTab(ctx, 0).get
  }
}
