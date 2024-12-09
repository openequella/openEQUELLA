package equellatests.browserpage

import org.openqa.selenium.By

object CommonXPath {

  def buttonWithText(t: String): By =
    By.xpath(s".//button[normalize-space(text()) = ${BrowserPage.quoteXPath(t)}]")

  def pageTitle(title: String): By =
    By.xpath(
      "id('header-inner')/div[text()=" + BrowserPage.quoteXPath(title) + "]"
    ) // for pages in the old UI

  def newPageTitle(title: String): By =
    By.xpath(
      "//header/div/div/h5[text()=" + BrowserPage.quoteXPath(title) + "]"
    ) // for pages in the new UI
}
