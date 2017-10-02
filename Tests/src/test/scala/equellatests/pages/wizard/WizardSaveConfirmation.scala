package equellatests.pages.wizard

import com.tle.webtests.framework.PageContext
import equellatests.pages.BrowserPage
import equellatests.pages.viewitem.SummaryPage
import org.openqa.selenium.By

class WizardSaveConfirmation(val ctx: PageContext) extends BrowserPage {
  override def pageBy = By.id("savePrompt")

  def buttonBy(name: String) = By.xpath("//div[@class = 'modal-footer-inner']/button[normalize-space(text()) = "+quoteXPath(name)+"]")

  def publish() : SummaryPage = {
    driver.findElement(buttonBy("Publish")).click()
    new SummaryPage(ctx).get()
  }
}
