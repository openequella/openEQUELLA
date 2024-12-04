package equellatests.sections.search

import equellatests.browserpage.BrowserPage
import org.openqa.selenium.By

trait NamedResultList extends BrowserPage {
  type Result
  def resultFromBy: By => Result
  def resultForName(name: String): Result = {
    resultFromBy(
      By.xpath(
        s"//div[@class='itemresult-wrapper' and .//h3/a[normalize-space(string())=${quoteXPath(name)}]][1]"
      )
    )
  }

}
