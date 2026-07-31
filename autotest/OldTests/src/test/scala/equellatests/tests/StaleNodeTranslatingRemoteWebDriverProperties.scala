package equellatests.tests

import com.tle.webtests.framework.StaleNodeTranslatingRemoteWebDriver
import org.openqa.selenium.{StaleElementReferenceException, WebDriverException}
import org.scalacheck.Prop.propBoolean
import org.scalacheck.Properties

/** Deterministic unit checks for [[StaleNodeTranslatingRemoteWebDriver]]'s translation decision.
  * Chrome/ChromeDriver's "-32000" inspector error ("Node with given id does not belong to the
  * document") — a plain [[WebDriverException]] — must be normalised into a
  * [[StaleElementReferenceException]] so downstream waiters retry instead of failing (Selenium
  * issue #15401), while any other error is left untouched.
  *
  * These exercise the pure `translate` decision; the correct interception layer (that this fires
  * for real command errors, including after `Augmenter.augment`) is verified separately with a live
  * browser.
  */
object StaleNodeTranslatingRemoteWebDriverProperties
    extends Properties("StaleNodeTranslatingRemoteWebDriver") {

  private val chromeStaleMessage =
    "unknown error: unhandled inspector error: " +
      """{"code":-32000,"message":"Node with given id does not belong to the document"}"""

  property("chrome -32000 error is translated to StaleElementReferenceException") =
    StaleNodeTranslatingRemoteWebDriver
      .translate(new WebDriverException(chromeStaleMessage))
      .isInstanceOf[StaleElementReferenceException]

  property("unrelated WebDriverException is returned unchanged") = {
    val original = new WebDriverException("some other error")
    StaleNodeTranslatingRemoteWebDriver.translate(original) eq original
  }

  property("classic StaleElementReferenceException is returned unchanged") = {
    val original = new StaleElementReferenceException("already stale")
    StaleNodeTranslatingRemoteWebDriver.translate(original) eq original
  }
}
