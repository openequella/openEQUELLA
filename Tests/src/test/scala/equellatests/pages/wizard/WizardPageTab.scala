package equellatests.pages.wizard

import com.tle.webtests.framework.PageContext
import equellatests.pages.BrowserPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}

class WizardPageTab(val ctx: PageContext, val pageNum: Int) extends BrowserPage {
  def universalControl(ctrlNum: Int) = new UniversalControl(this, ctrlNum)

  val pageBy = By.xpath(s"//input[@name='pages.pg' and @value=${quoteXPath(pageNum.toString)}]")

  override def mainExpectation: ExpectedCondition[_] = ExpectedConditions.presenceOfElementLocated(pageBy)
}
