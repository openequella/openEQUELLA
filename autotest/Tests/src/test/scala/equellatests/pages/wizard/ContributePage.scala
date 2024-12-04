package equellatests.pages.wizard

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.TitledPage
import equellatests.sections.wizard.WizardPageTab
import org.openqa.selenium.By

case class ContributePage(ctx: PageContext)
    extends TitledPage("Contribute", "access/contribute.do") {

  def openWizard(name: String): WizardPageTab = {
    findElement(
      By.xpath(
        ".//table[contains(@class, 'zebra')]//a[normalize-space(text())="
          + quoteXPath(name) + "]"
      )
    ).click()
    new WizardPageTab(ctx, 0).get()
  }
}
