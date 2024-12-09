package equellatests.pages.cloudprovider

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}
import scala.jdk.CollectionConverters._

case class CloudProviderListPage(ctx: PageContext)
    extends NewTitledPage("Cloud providers", "page/cloudprovider") {

  case class CloudProviderEntry(elem: WebElement) {

    def description(): Option[String] =
      elem.findElements(By.xpath("./div/div/p")).asScala.headOption.map(_.getText)

    def delete(name: String): Unit = {
      val cloudProvider = elem.findElement(By.xpath("./div/button"))
      cloudProvider.click()
      waitFor(ExpectedConditions.elementToBeClickable(By.id("confirm-dialog-confirm-button")))
        .click()

      waitFor(
        ExpectedConditions.stalenessOf(cloudProvider)
      )
    }
  }

  def checkCloudProviderExisting(name: String): Boolean = {
    val a = driver.findElements(By.xpath(findCloudProviderPath(name)))
    a.size() > 0
  }

  def findCloudProviderPath(name: String): String = {
    "id('cloudProviderList')//li[.//span[text() = " + quoteXPath(name) + "]]"
  }

  def resultForName(name: String) =
    CloudProviderEntry(
      findElement(By.xpath(findCloudProviderPath(name)))
    )

  def waitForResults(): Unit = {
    waitFor(
      ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("id('cloudProviderList')//li"), 0)
    )
  }

  def add(url: String): Unit = {
    findElement(By.id("add-entity")).click()
    val urlTextField: WebElement = waitFor(
      ExpectedConditions.presenceOfElementLocated(By.id("new_cloud_provider_url"))
    )
    urlTextField.sendKeys(url)
    waitFor(ExpectedConditions.elementToBeClickable(By.id("confirm-register"))).click()
  }

  def delete(name: String): Unit = {
    resultForName(name).delete(name)
  }
}
