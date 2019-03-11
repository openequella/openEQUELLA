package equellatests

import com.tle.webtests.framework.{PageContext, StandardDriverFactory, TestConfig}
import com.tle.webtests.pageobject.UndeterminedPage
import com.tle.webtests.pageobject.institution.{
  ImportTab,
  InstitutionListTab,
  InstitutionTabInterface,
  ServerAdminLogonPage
}
import org.openqa.selenium.WebDriver

object TestChecker {

  def withServerAdmin[A](f: PageContext => A): A = {
    val testConfig = GlobalConfig.testConfig
    withBrowserDriver(testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
      val choice = new UndeterminedPage[InstitutionTabInterface](context,
                                                                 new InstitutionListTab(context),
                                                                 new ImportTab(context))
      new ServerAdminLogonPage(context).load.logon(testConfig.getAdminPassword, choice)
      val r = f(context)
      driver.quit()
      r
    }
  }

  def withBrowserDriver[A](testConfig: TestConfig)(f: WebDriver => A): A = {
    val factory = new StandardDriverFactory(testConfig)
    val driver  = factory.getDriver(getClass)
    try {
      f(driver)
    } catch {
      case error: Throwable =>
        driver.quit()
        throw error
    }
  }
}
