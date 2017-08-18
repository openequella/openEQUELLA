package equellatests.pages

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedCondition

class LoginPage(val ctx: PageContext) extends BrowserPage {

  def login(username: String, password: String): HomePage =
    loginWithRedirect(username, password, new HomePage(ctx).pageExpectation)

  def loginWithRedirect[A](username: String, password: String, expected: ExpectedCondition[A]): A = {
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
    driver.get(ctx.getBaseUrl + "logon.do?logout=true")
    get()
  }

  def pageBy = By.id("_logonButton")
}
