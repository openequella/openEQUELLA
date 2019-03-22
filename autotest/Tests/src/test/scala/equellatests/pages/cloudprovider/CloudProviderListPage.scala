package equellatests.pages.cloudprovider

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.NewTitledPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

case class CloudProviderListPage(ctx: PageContext)
    extends NewTitledPage("Cloud providers", "page/cloudprovider") {
  case class CloudProviderEntry(elem: WebElement) {
    def description(): String = elem.findElement(By.xpath("./div/div/p")).getText()
    def delete(name: String): Unit = {
      val cloudProvider = elem.findElement(By.xpath("./div/button"))
      cloudProvider.click()
      waitFor(ExpectedConditions.elementToBeClickable(By.id("confirm-delete"))).click()

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
    "id('cloudProviderList')//li[.//h6[text() = " + quoteXPath(name) + "]]"
  }

  def resultForName(name: String) =
    CloudProviderEntry(
      findElement(By.xpath(findCloudProviderPath(name)))
    )

  def waitForResults(): Unit = {
    waitFor(
      ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("id('cloudProviderList')//li"), 0))
  }

  def add(): CloudProviderRegisterPage = {
    findElement(By.id("add-entity")).click()
    CloudProviderRegisterPage(ctx).get()
  }

  def delete(name: String): Unit = {
    resultForName(name).delete(name)
  }
}

case class CloudProviderRegisterPage(ctx: PageContext)
    extends NewTitledPage("Create cloud providers", "page/cloudprovider/new") {
  def registerProvider(url: String): Unit = {
    findElement(By.id("new_cloud_provider_url")).sendKeys(url)
    findElement(By.id("register-cloud-provider")).click()
  }
}
