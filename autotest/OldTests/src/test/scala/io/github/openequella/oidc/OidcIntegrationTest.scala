package io.github.openequella.oidc

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.generic.page.UserProfilePage
import com.tle.webtests.pageobject.{HomePage, LoginPage, SettingsPage}
import com.tle.webtests.test.AbstractSessionTest
import integtester.IntegTester
import integtester.oidc.OidcIntegration
import org.testng.Assert.{assertEquals, assertTrue}
import org.testng.annotations.{BeforeClass, DataProvider, Test}

@TestInstitution("vanilla")
class OidcIntegrationTest extends AbstractSessionTest {

  @BeforeClass
  def runIntegTester(): Unit = {
    IntegTester.stream(List.empty).compile.drain.unsafeRunAsync(_ => ())
  }

  @Test(description = "The OIDC login button should be displayed in the Login page")
  def showLoginButton(): Unit = {
    val loginPage = new LoginPage(context).load()
    assertTrue(loginPage.hasOidcLoginButton)
  }

  @Test(
    description =
      "The OIDC login button should be hidden if there isn't an enabled OIDC configuration"
  )
  def hideLoginButton(): Unit = {
    // todo:  disable OIDC configuration and then check the button.
  }

  @Test(description =
    "User should be able to login using OIDC and have proper profile and roles set up."
  )
  def login(): Unit = {
    authenticate()
    // User should be redirected to the home page.
    new HomePage(context).get()

    // Go to profile page and check names.
    val profilePage = new UserProfilePage(context).load()
    assertEquals(profilePage.getFirstName, "Edalex")
    assertEquals(profilePage.getLastName, "Edalex")
    assertEquals(profilePage.getEmail, "test@Edalex")
    assertEquals(profilePage.getUsername, "Edalex tester")

    // Check role mappings. The user should have the role of `System Admin` which allows the user to access the OIDC Setting page.
    val settingsPage = new SettingsPage(context).load()
    assertTrue(settingsPage.oidcSettingsPage != null)
  }

  @DataProvider(name = "authRespErrors")
  def authRespErrors(): Array[(String, String)] = Array(
    (
      "invalid_state",
      "Single Sign-on failed: invalid_request - Invalid state provided: invalid state"
    ),
    ("missing_code", "Single Sign-on failed: invalid_request - Missing required parameter 'code'")
  )

  @Test(
    description = "Show error message if the authentication request fails",
    dataProvider = "authRespErrors"
  )
  def authError(args: (String, String)): Unit = {
    val (errorType, errorMsg) = args
    OidcIntegration.setAuthRespCommand(errorType)

    authenticate()
    // OEQ Login page should be displayed with an error describing the failure
    val loginPage = new LoginPage(context).get()
    assertEquals(loginPage.getLoginErrorDetails, errorMsg)

    // Reset the command.
    OidcIntegration.setAuthRespCommand("normal")
  }

  @DataProvider(name = "tokenRespErrors")
  def tokenRespErrors(): Array[(String, String)] = Array(
    (
      "invalid_jwt",
      "Single Sign-on failed: Provided JWT failed signature verification: The Claim 'iss' value doesn't match the required issuer."
    ),
    (
      "invalid_nonce",
      "Single Sign-on failed: Provided JWT failed nonce verification: Provided nonce does not exist"
    ),
    (
      "invalid_key",
      "Single Sign-on failed: Failed to retrieve JWK by the obtained ID token's key ID"
    ),
    (
      "invalid_resp_format",
      "Single Sign-on failed: An ID Token has been issued but can't be retrieved from an unexpected response format: DecodingFailure at .access_token: Missing required field"
    ),
    (
      "missing_username_claim",
      "Single Sign-on failed: Missing the configured username claim username in the ID token"
    ),
    ("server_error", "Single Sign-on failed: Failed to request an ID token: Auth server error")
  )

  @Test(
    description = "Show error message if the token request fails",
    dataProvider = "tokenRespErrors"
  )
  def tokenError(args: (String, String)): Unit = {
    val (errorType, errorMsg) = args
    OidcIntegration.setTokenRespCommand(errorType)

    authenticate()

    val loginPage = new LoginPage(context).get()
    assertEquals(loginPage.getLoginErrorDetails, errorMsg)

    OidcIntegration.setTokenRespCommand("normal")
  }

  private def authenticate(): Unit = {
    val loginPage = new LoginPage(context).load()
    loginPage.loginWithOidc()
  }
}
