package io.github.openequella.oidc

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.generic.page.UserProfilePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.pageobject.viewitem.SummaryPage
import com.tle.webtests.pageobject.wizard.ContributePage
import com.tle.webtests.pageobject.{HomePage, LoginPage, SettingsPage}
import com.tle.webtests.test.AbstractIntegrationTest
import integtester.oidc.OidcIntegration
import integtester.oidc.OidcUser.TEST_USER
import io.github.openequella.pages.myresources.MyResourcesPage
import io.github.openequella.pages.oidc.OidcSettingsPage
import io.github.openequella.pages.search.NewSearchPage
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.testng.Assert.{assertEquals, assertTrue}
import org.testng.annotations.{DataProvider, Test}

@TestInstitution("vanilla")
class OidcIntegrationTest extends AbstractIntegrationTest {

  override protected def isCleanupItems = false

  @Test(description =
    "User should be able to login using OIDC and have proper profile and roles set up."
  )
  def singleSignOn(): Unit = {
    logonOidc()
    // User should be redirected to the home page.
    new HomePage(context).get()

    // Go to profile page and check names.
    val profilePage = new UserProfilePage(context).load()
    assertEquals(profilePage.getGivenName, TEST_USER.given_name)
    assertEquals(profilePage.getFamilyName, TEST_USER.family_name)
    assertEquals(profilePage.getEmail, TEST_USER.email)
    assertEquals(profilePage.getUsername, TEST_USER.username)

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

    logonOidc()
    checkErrorMessage(errorMsg)
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

    logonOidc()
    checkErrorMessage(errorMsg)
    OidcIntegration.setTokenRespCommand("normal")
  }

  @Test(description = "User signed in via OIDC should be able to create new Item")
  def contribute(): Unit = {
    logonOidc()

    val menu           = new MenuSection(context).get
    val contributePage = menu.clickMenu("Contribute", new ContributePage(context))
    val wizard         = contributePage.openWizard("Generic Testing Collection")

    val textField = By.xpath(s"//div[@id = '${wizard.getControlId(1)}']//input")
    wizard.getWaiter
      .until(ExpectedConditions.visibilityOfElementLocated(textField))
      .sendKeys("Edalex")
    val summaryPage: SummaryPage = wizard.save().publish()

    val owner = summaryPage.itemDetails().getOwner
    assertEquals(owner, s"${TEST_USER.given_name} ${TEST_USER.family_name}")
  }

  @Test(
    description = "OIDC users should be included in the user search result",
    dependsOnMethods = Array("contribute")
  )
  def searchUser(): Unit = {
    logon()
    val searchPage = new NewSearchPage(context).load()

    searchPage.expandRefineControlPanel()
    searchPage.selectOwner(TEST_USER.username)
    searchPage.waitForSearchCompleted(1)
  }

  @Test(
    description = "Using custom User ID attribute to retrieve the correct user ID",
    dependsOnMethods = Array("searchUser")
  )
  def userIdAttribute(): Unit = {
    logon()
    val page = new OidcSettingsPage(context).load()
    page.inputTextField("User ID attribute", "legacy_id")
    page.save()

    // Since the testing user is now associated to the existing user 'autotest' who has four items,
    // an item search filtered by this testing user should return four.
    val searchPage = new NewSearchPage(context).load()
    searchPage.expandRefineControlPanel()
    searchPage.selectOwner(TEST_USER.username)
    searchPage.waitForSearchCompleted(4)

    // Login again using the testing user and My resources page should show four items.
    logout()
    logonOidc()
    val myResourcesPage = new MyResourcesPage(context).load()
    myResourcesPage.waitForSearchCompleted(4)
  }

  private def logonOidc(): Unit = {
    val loginPage = new LoginPage(context).load()
    loginPage.loginWithOidc()
  }

  // In test cases for checking the OIDC errors, there is test flakyness about stale elements
  // due to the refresh of the Login page. As a result, use the static method to retrieve login
  // errors so that we do not need to wait and get an instance of LoginPage.
  private def checkErrorMessage(
      expectedErrorMsg: String
  ): Unit = {
    val waiter = new WebDriverWait(context.getDriver, context.getTestConfig.getStandardTimeout)
    assertEquals(LoginPage.getLoginErrorDetails(waiter), expectedErrorMsg)
  }
}
