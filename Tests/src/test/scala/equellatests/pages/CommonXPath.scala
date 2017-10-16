package equellatests.pages

import org.openqa.selenium.By

object CommonXPath  {

  def buttonWithText(t: String) : By = By.xpath(s".//button[normalize-space(text()) = ${BrowserPage.quoteXPath(t)}]")
}
