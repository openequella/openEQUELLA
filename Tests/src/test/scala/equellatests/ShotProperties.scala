package equellatests

import com.tle.webtests.framework.{PageContext, ScreenshotTaker, TestConfig}
import com.tle.webtests.pageobject.LoginPage
import equellatests.TestChecker.withBrowserDriver
import equellatests.domain.TestLogon
import org.scalacheck.{Prop, Properties}

import scala.util.{Success, Try}

abstract class ShotProperties(name: String) extends Properties(name) {

  def withLogon[A](logon: TestLogon)(f: PageContext => Prop): Prop = {
    val testConfig = new TestConfig(GlobalConfig.baseFolderForInst(logon.inst), false)
    withBrowserDriver(testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)

      def quitDriver(shot: Boolean) = {
        if (shot)
          Try(ScreenshotTaker.takeScreenshot(driver, context.getTestConfig.getScreenshotFolder, name, context.getTestConfig.isChromeDriverSet))
        Try(driver.quit())
      }
      Try {
        new LoginPage(context).load().login(logon.username, logon.password)
        f(context)
      }.transform({ p =>
        Success(p.map { r =>
          quitDriver(r.failure)
          r
        })
      }, { failure =>
        quitDriver(true)
        throw failure
      }).get
    }
  }
}
