package equellatests

import com.tle.webtests.framework.{PageContext, TestConfig}
import com.tle.webtests.pageobject.LoginPage
import equellatests.TestChecker.withBrowserDriver
import equellatests.domain.TestLogon
import org.scalacheck.Prop

/** For property-based suites whose properties are each a self-contained browser session: opens a
  * driver, logs on, evaluates the property, then quits.
  *
  * Mix into a [[PropertyBasedBrowserTest]] and hand the resulting `Prop` to `check`.
  */
trait ShotTest { self: PropertyBasedBrowserTest =>

  def withLogon(logon: TestLogon)(f: PageContext => Prop): Prop = {
    val testConfig = new TestConfig(GlobalConfig.baseFolderForInst(logon.inst), false)
    withBrowserDriver(suiteName, testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getInstitutionUrl)
      new LoginPage(context).load().login(logon.username, logon.password)
      f(context)
    }
  }
}
