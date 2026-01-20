package equellatests

import com.tle.webtests.framework.{PageContext, ScreenshotTaker, StandardDriverFactory, TestConfig}
import com.tle.webtests.pageobject.UndeterminedPage
import com.tle.webtests.pageobject.institution.{
  ImportTab,
  InstitutionListTab,
  InstitutionTabInterface,
  ServerAdminLogonPage
}
import equellatests.GlobalConfig.testConfig
import org.openqa.selenium.WebDriver

import scala.util.{Failure, Success, Try}

object TestChecker {

  def withServerAdmin[A](name: String, f: PageContext => A): A = {
    val testConfig = GlobalConfig.testConfig
    withBrowserDriver(name, testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
      val choice = new UndeterminedPage[InstitutionTabInterface](
        context,
        new InstitutionListTab(context),
        new ImportTab(context)
      )
      new ServerAdminLogonPage(context).load.logon(testConfig.getAdminPassword, choice)
      val r = f(context)
      driver.quit()
      r
    }
  }

  def withBrowserDriver[A](name: String, testConfig: TestConfig)(f: WebDriver => A): A = {
    val factory = new StandardDriverFactory(testConfig)
    val driver  = factory.getDriver(getClass)
    Try(f(driver))
      .transform(
        Success.apply,
        { t =>
          ScreenshotTaker.takeScreenshot(
            driver,
            testConfig.getScreenshotFolder,
            name,
            testConfig.isChromeDriverSet
          )
          Failure(t)
        }
      )
      .fold(
        t => {
          driver.quit()
          throw t
        },
        identity
      )
  }
}
