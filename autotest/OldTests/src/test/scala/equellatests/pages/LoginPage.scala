package equellatests.pages

import com.codeborne.selenide.Selenide.$$
import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}

import scala.jdk.CollectionConverters._

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

  /** Unlike `loginNoticeHasImageWithSrc`, this must not throw when there is no `<img>` at all -
    * that's the expected (successful) outcome when sanitisation strips an untrusted image.
    */
  def loginNoticeHasNoImageWithSrc(src: String): Boolean =
    ! $$(By.cssSelector("#loginNotice img")).asScala.exists(_.getAttribute("src") == src)

  /** True if `text` is rendered inside an `<a>` within the login notice - i.e. it's still a link,
    * not just visible text.
    */
  def loginNoticeHasLinkWithText(text: String): Boolean =
    ! $$(By.xpath(s"//*[@id='loginNotice']//a[contains(text(),'$text')]")).isEmpty
}
