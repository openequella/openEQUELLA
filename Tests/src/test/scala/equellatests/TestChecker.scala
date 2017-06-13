package equellatests

import com.tle.webtests.framework.{PageContext, StandardDriverFactory, TestConfig}
import com.tle.webtests.pageobject.institution.{ImportTab, InstitutionListTab, InstitutionTabInterface, ServerAdminLogonPage}
import com.tle.webtests.pageobject.{LoginPage, UndeterminedPage}
import equellatests.domain.{TestInst, TestLogon}
import org.openqa.selenium.WebDriver
import org.scalacheck.Prop

import scala.util.Try

object TestChecker {

  def withLogon[A](logon: TestLogon)(f: PageContext => A): A = {
    val testConfig = new TestConfig(logon.inst.baseFolder, false)
    val factory = new StandardDriverFactory(testConfig)
    val driver = factory.getDriver(getClass)
    val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)

    val res = Try {
      new LoginPage(context).load().login(logon.username, logon.password)
      f(context)
    }
    Try(driver.quit())
    res.get
  }

  def withServerAdmin[A](f: PageContext => A): A = {
    val testConfig = GlobalConfig.testConfig
    withBrowserDriver(testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
      val choice = new UndeterminedPage[InstitutionTabInterface](context,
        new InstitutionListTab(context),
        new ImportTab(context))
      new ServerAdminLogonPage(context).load.logon(testConfig.getAdminPassword, choice)
      f(context)
    }
  }

  def withBrowserDriver[A](testConfig: TestConfig)(f: WebDriver => A): A = {
    val factory = new StandardDriverFactory(testConfig)
    val driver = factory.getDriver(getClass)
    val res = Try(f(driver))
    Try(driver.quit())
    res.get
  }
}
