package io.github.openequella.oidc

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.LoginPage
import com.tle.webtests.test.AbstractSessionTest
import org.testng.annotations.Test
import org.testng.Assert.assertTrue

@TestInstitution("fiveo")
class OidcIntegrationTest extends AbstractSessionTest {

  @Test(description = "The OIDC login button should be displayed in the Login page")
  def loginButton(): Unit = {
    val loginPage = new LoginPage(context).load()
    assertTrue(loginPage.hasOidcLoginButton)
  }
}
