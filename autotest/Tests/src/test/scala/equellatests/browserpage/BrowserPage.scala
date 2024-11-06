package equellatests.browserpage

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}

import java.time.Duration
import scala.util.Try

object BrowserPage {
  def quoteXPath(input: String): String = {
    val txt = input
    if (txt.indexOf("'") > -1 && txt.indexOf("\"") > -1)
      "concat('" + txt.replace("'", "', \"'\", '") + "')"
    else if (txt.indexOf("\"") > -1) "'" + txt + "'"
    else "\"" + txt + "\""
  }
}

trait BrowserPage {
  def ctx: PageContext
  def driver: WebDriver                        = ctx.getDriver
  def findElement(by: By): WebElement          = driver.findElement(by)
  def findElementById(id: String): WebElement  = findElement(By.id(id))
  def findElementO(by: By): Option[WebElement] = Try(driver.findElement(by)).toOption
  val waiter                                   = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(50))
  def waitFor[A](c: ExpectedCondition[A]): A   = waiter.until(c)

  def updatedBy(by: By): ExpectedCondition[_] =
    ExpectedConditions.and(ExpectedConditions.stalenessOf(findElement(by)),
                           ExpectedConditions.visibilityOfElementLocated(by))

  def quoteXPath(i: String): String = BrowserPage.quoteXPath(i)
}
