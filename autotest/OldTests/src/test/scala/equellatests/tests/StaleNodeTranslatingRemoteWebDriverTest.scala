package equellatests.tests

import com.tle.webtests.framework.StaleNodeTranslatingRemoteWebDriver
import org.openqa.selenium.{StaleElementReferenceException, WebDriverException}
import org.scalatest.funsuite.AnyFunSuite

/** Deterministic unit checks for [[StaleNodeTranslatingRemoteWebDriver]]'s translation decision.
  * Chrome/ChromeDriver's "-32000" inspector error ("Node with given id does not belong to the
  * document") — a plain [[WebDriverException]] — must be normalised into a
  * [[StaleElementReferenceException]] so downstream waiters retry instead of failing (Selenium
  * issue #15401), while any other error is left untouched.
  *
  * These exercise the pure `translate` decision; the correct interception layer (that this fires
  * for real command errors, including after `Augmenter.augment`) is verified separately with a live
  * browser. Fixed inputs rather than generated ones, so these are plain assertions; and being
  * browserless and UI-agnostic, this is the one suite here that carries no `@NewUIOnly` tag.
  */
class StaleNodeTranslatingRemoteWebDriverTest extends AnyFunSuite {

  private val chromeStaleMessage =
    "unknown error: unhandled inspector error: " +
      """{"code":-32000,"message":"Node with given id does not belong to the document"}"""

  test("chrome -32000 error is translated to StaleElementReferenceException") {
    val translated =
      StaleNodeTranslatingRemoteWebDriver.translate(new WebDriverException(chromeStaleMessage))
    assert(translated.isInstanceOf[StaleElementReferenceException])
  }

  test("unrelated WebDriverException is returned unchanged") {
    val original = new WebDriverException("some other error")
    assert(StaleNodeTranslatingRemoteWebDriver.translate(original) eq original)
  }

  test("classic StaleElementReferenceException is returned unchanged") {
    val original = new StaleElementReferenceException("already stale")
    assert(StaleNodeTranslatingRemoteWebDriver.translate(original) eq original)
  }
}
