package equellatests.sections.wizard

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.WaitingBrowserPage
import equellatests.pages.viewitem.SummaryPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}

class WizardPageTab(val ctx: PageContext, val pageNum: Int) extends WaitingBrowserPage {
  def universalControl(ctrlNum: Int) = new UniversalControl(this, ctrlNum)

  private def clickSave() =
    driver
      .findElement(
        By.xpath("//input[contains(@class, 'action-button') and normalize-space(@value)='Save']")
      )
      .click()

  def save(): WizardSaveConfirmation = {
    clickSave()
    new WizardSaveConfirmation(ctx).get()
  }

  def saveToSummary(): SummaryPage = {
    clickSave()
    new SummaryPage(ctx).get()
  }

  val pageBy = By.xpath(s"//input[@name='pages.pg' and @value=${quoteXPath(pageNum.toString)}]")

  override def mainExpectation: ExpectedCondition[_] =
    ExpectedConditions.presenceOfElementLocated(pageBy)

  def ctrl[WC <: WizardControl](f: (WizardPageTab, Int) => WC, num: Int): WC = f(this, num).get()
}
