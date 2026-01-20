package equellatests.sections.wizard

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.WaitingBrowserPage
import equellatests.pages.viewitem.SummaryPage
import org.openqa.selenium.By

class WizardSaveConfirmation(val ctx: PageContext) extends WaitingBrowserPage {
  override def pageBy = By.id("savePrompt")

  def buttonBy(name: String) =
    By.xpath(
      "//div[@class = 'modal-footer-inner']/button[normalize-space(text()) = " + quoteXPath(
        name
      ) + "]"
    )

  def clickButton(name: String) = driver.findElement(buttonBy(name)).click()
  def publish(): SummaryPage = {
    clickButton("Publish")
    new SummaryPage(ctx).get()
  }

  def submitForModeration(): SummaryPage = {
    clickButton("Submit for moderation")
    new SummaryPage(ctx).get()
  }
}
