package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}

case class LoginPage(ctx: PageContext) extends LoadablePage {

  def login(username: String, password: String): HomePage =
    loginWithRedirect(username, password, new HomePage(ctx).pageExpectation)

  def loginWithRedirect[A](
      username: String,
      password: String,
      expected: ExpectedCondition[A]
  ): A = {
    val user = driver.findElement(By.id("username"))
    user.clear()
    user.sendKeys(username)
    val pass = driver.findElement(By.id("password"))
    pass.clear()
    pass.sendKeys(password)
    driver.findElement(By.id("_logonButton")).click()
    waitFor(expected)
  }

  def load() = {
    driver.get(ctx.getBaseUrl + "logon.do?logout=true&old=true")
    get()
  }

  def pageBy = By.id("_logonButton")

  private def loginNotice: WebElement = findElementById("loginNotice")

  private def loginNoticeImage: WebElement = loginNotice.findElement(By.tagName("img"))

  def loginNoticeExists: Boolean = {
    loginNotice.isDisplayed
  }

  def loginNoticeHasImageWithSrc(src: String): Boolean = {
    waitFor(ExpectedConditions.visibilityOf(loginNoticeImage))
    loginNoticeImage.isDisplayed && loginNoticeImage.getAttribute("src") == src
  }
}
