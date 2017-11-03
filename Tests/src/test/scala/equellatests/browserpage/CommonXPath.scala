package equellatests.browserpage

import org.openqa.selenium.By

object CommonXPath  {

  def buttonWithText(t: String) : By = By.xpath(s".//button[normalize-space(text()) = ${BrowserPage.quoteXPath(t)}]")

  def pageTitle(title: String) : By = By.xpath("id('header-inner')/div[text()="+BrowserPage.quoteXPath(title)+"]")
}
