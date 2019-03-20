package equellatests.pages.cloudprovider

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

case class CloudProviderListPage(ctx: PageContext)
    extends NewTitledPage("Cloud providers", "page/cloudprovider") {
  case class CloudProviderEntry(elem: WebElement) {
    def description(): String = elem.findElement(By.xpath("./div/div/p")).getText()

  }

  def resultForName(name: String) =
    CloudProviderEntry(
      driver.findElement(
        By.xpath("id('cloudProviderList')//li[.//h6[text() = " + quoteXPath(name) + "]]")))

  def waitForResults(): Unit = {
    waitFor(
      ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("id('cloudProviderList')//li"), 0))
  }
}
