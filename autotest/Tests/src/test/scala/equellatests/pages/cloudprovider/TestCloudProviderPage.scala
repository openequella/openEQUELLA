package equellatests.pages.cloudprovider

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.{LoadablePage, WaitingBrowserPage}
import integtester.IntegTester
import org.http4s.Uri
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

case class TestCloudProviderPage(ctx: PageContext, details: TestCloudProviderDetails)
    extends WaitingBrowserPage {

  private def returnButtonBy = By.id("returnButton")
  private def registerButton = findElement(By.id("registerButton"))

  def registerProvider(): Unit = {
    registerButton.click()
    waitFor(ExpectedConditions.presenceOfElementLocated(returnButtonBy))
  }

  def returnToEQUELLA(): CloudProviderListPage = {
    findElement(returnButtonBy).click()
    CloudProviderListPage(ctx).get()
  }

  def createRegistrationUrl(): String = {
    Uri
      .unsafeFromString(IntegTester.providerRegistrationUrl)
      .withQueryParam("name", details.name)
      .withOptionQueryParam("description", details.description)
      .withOptionQueryParam("iconUrl", details.iconUrl)
      .toString()
  }

  override def pageBy: By = By.id("testCloudProvider")

}
