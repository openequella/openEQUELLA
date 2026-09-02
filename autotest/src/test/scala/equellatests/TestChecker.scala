package equellatests

import com.codeborne.selenide.{Selenide, WebDriverRunner}
import com.tle.webtests.framework.{PageContext, ScreenshotTaker, StandardDriverFactory, TestConfig}
import com.tle.webtests.pageobject.UndeterminedPage
import com.tle.webtests.pageobject.institution._
import org.openqa.selenium.WebDriver

import scala.util.control.NonFatal

/** Browser driver lifecycle for the ScalaTest suites.
  *
  * Every driver is bound to Selenide with `setWebDriver`, and Selenide keeps a reference to it
  * until `Selenide.closeWebDriver` releases it again — a bare `driver.quit()` does not. So opening
  * and closing are paired here rather than left to each caller.
  */
object TestChecker {

  def withServerAdmin[A](name: String, f: PageContext => A): A = {
    val testConfig = GlobalConfig.testConfig
    withBrowserDriver(name, testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
      // The possible pages after login.
      val pagesAfterLogin = new UndeterminedPage[InstitutionTabInterface](
        context,
        new InstitutionListTab(context),
        new ImportTab(context),
        // If the database migration is still running, after login the admin will be taken to the databases page.
        new DatabasesPage(context)
      )

      new ServerAdminLogonPage(context).load.logon(testConfig.getAdminPassword, pagesAfterLogin)
      f(context)
    }
  }

  /** Opens a browser driver, runs `f` against it, and always closes it again. `name` names any
    * screenshot taken when `f` fails.
    */
  def withBrowserDriver[A](name: String, testConfig: TestConfig)(f: WebDriver => A): A = {
    val driver = newBoundDriver(testConfig)
    try screenshotOnFailure(driver, name, testConfig)(f(driver))
    finally closeBrowserDriver()
  }

  /** Opens a browser driver and runs `setUp` against it, returning `setUp`'s result with the driver
    * still open — the caller owns it from then on and must call [[closeBrowserDriver]] on the same
    * thread. If `setUp` fails the driver is screenshotted under `name` and closed.
    *
    * Use [[withBrowserDriver]] instead wherever the driver need not outlive the block.
    */
  def openBrowserDriver[A](name: String, testConfig: TestConfig)(setUp: WebDriver => A): A = {
    val driver = newBoundDriver(testConfig)
    try
      screenshotOnFailure(driver, name, testConfig) {
        setUp(driver)
      }
    catch {
      case NonFatal(t) =>
        closeBrowserDriver()
        throw t
    }
  }

  /** Quits the current thread's browser driver and releases Selenide's registration of it. */
  def closeBrowserDriver(): Unit = Selenide.closeWebDriver()

  private def newBoundDriver(testConfig: TestConfig): WebDriver = {
    val driver = new StandardDriverFactory(testConfig).getDriver(getClass)
    WebDriverRunner.setWebDriver(driver)
    driver
  }

  private def screenshotOnFailure[A](driver: WebDriver, name: String, testConfig: TestConfig)(
      f: => A
  ): A =
    try f
    catch {
      case NonFatal(t) =>
        ScreenshotTaker.takeScreenshot(
          driver,
          testConfig.getScreenshotFolder,
          name,
          testConfig.isChromeDriverSet
        )
        throw t
    }
}
