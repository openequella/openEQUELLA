package equellatests.sections.search

import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By

trait MetadataResult extends WaitingBrowserPage {

  def metadataText(name: String): String = {
    val metaBy = By.xpath(
      s".//div[@class='itemresult-metaline' and strong[text() = ${quoteXPath(name)}]]/span[@class='itemresult-meta-delim']"
    )
    pageElement.findElement(metaBy).getText
  }
}
