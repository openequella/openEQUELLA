/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.oidc

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.test.AbstractSessionTest
import io.github.openequella.pages.oidc.OidcSettingsPage
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.{DataProvider, Test}
import com.tle.webtests.pageobject.LoginPage

/** New UI tests for OIDC settings page. */
@TestInstitution("fiveo") class OidcSettingsPageTest extends AbstractSessionTest {
  val issuerLabel                = "Issuer *"
  val clientIdLabel              = "Client ID *"
  val clientSecretLabel          = "Client secret *"
  val loginUrlLabel              = "Identity Provider Login URL *"
  val keyUrlLabel                = "Public Key Endpoint URL *"
  val tokenUrlLabel              = "Token URL *"
  val usernameClaimLabel         = "Username claim"
  val apiEndpointLabel           = "API endpoint *"
  val apiClientIdLabel           = "API Client ID *"
  val apiClientSecretLabel       = "API Client Secret *"
  val roleClaimLabel             = "Role claim"
  val userIdAttributeLabel       = "User ID attribute"
  val defaultRoles: List[String] = List("Admin", "Student")
  val customRoles: Map[String, List[String]] = Map(
    "TestRole" -> List("Admin")
  )

  @Test(description = "User should be able to create a new OIDC settings.")
  def createOidcSettings(): Unit = {
    val entraId = "Entra ID"
    val textFieldValues: Map[String, String] = Map(
      issuerLabel          -> "https://example.com",
      clientIdLabel        -> "123456",
      clientSecretLabel    -> "secret value",
      loginUrlLabel        -> "https://example.com/login",
      keyUrlLabel          -> "https://example.com/key",
      tokenUrlLabel        -> "https://example.com/token",
      usernameClaimLabel   -> "email",
      apiClientIdLabel     -> "entra id client ID",
      apiClientSecretLabel -> "entra id client secret",
      roleClaimLabel       -> "role",
      userIdAttributeLabel -> "my_id"
    )
    // Secret values should be empty.
    val expectedValues =
      textFieldValues.updated(clientSecretLabel, "").updated(apiClientSecretLabel, "")

    logon()
    val oidcSettingsPage = new OidcSettingsPage(context).load()

    oidcSettingsPage.enableOidc(true)
    oidcSettingsPage.selectIdP(entraId)

    textFieldValues.foreach { case (label, value) =>
      oidcSettingsPage.inputTextField(label, value)
    }

    oidcSettingsPage.selectDefaultRoles(defaultRoles)
    oidcSettingsPage.selectCustomRoles(customRoles)
    oidcSettingsPage.save()

    // Reload page and check the value.
    val newOidcSettingsPage = new OidcSettingsPage(context).load()

    assertTrue(newOidcSettingsPage.isOidcEnabled)
    assertEquals(newOidcSettingsPage.getIdpValue, entraId)

    val realSettings: Map[String, String] = expectedValues.map { case (label, _) =>
      label -> newOidcSettingsPage.getTextFieldValue(label)
    }

    assertEquals(realSettings, expectedValues)
    assertEquals(newOidcSettingsPage.getDefaultRolesNumber, 2)
    assertEquals(newOidcSettingsPage.getCustomRolesNumber, 1)
  }

  @Test(
    description = "The OIDC login button should be displayed in the Login page",
    dependsOnMethods = Array("createOidcSettings")
  )
  def showLoginButton(): Unit = {
    val loginPage = new LoginPage(context).load()
    assertTrue(loginPage.hasOidcLoginButton)
  }

  @Test(
    description =
      "The OIDC login button should be hidden if there isn't an enabled OIDC configuration",
    dependsOnMethods = Array("showLoginButton")
  )
  def hideLoginButton(): Unit = {
    logon()
    val oidcSettingsPage = new OidcSettingsPage(context).load()
    oidcSettingsPage.enableOidc(false)
    oidcSettingsPage.save()

    val loginPage = new LoginPage(context).load()
    assertFalse(loginPage.hasOidcLoginButton)
  }

  @Test(
    description = "User should be able to update the general details.",
    dependsOnMethods = Array("hideLoginButton")
  )
  def updateGeneralDetails(): Unit = {
    val newCommonDetails: Map[String, String] = Map(
      "Issuer *"                      -> "https://new.example.com",
      "Client ID *"                   -> "new123456",
      "Identity Provider Login URL *" -> "https://new.example.com/login",
      "Public Key Endpoint URL *"     -> "https://new.example.com/key",
      "Token URL *"                   -> "https://new.example.com/token",
      "Username claim"                -> "new email"
    )

    logon()
    val oidcSettingsPage = new OidcSettingsPage(context).load()

    newCommonDetails.foreach({ case (label, value) =>
      oidcSettingsPage.inputTextField(label, value)
    })
    oidcSettingsPage.save()

    // Reload page and check the value.
    val newOidcSettingsPage = new OidcSettingsPage(context).load()
    val realSettings = newCommonDetails.map({ case (label, _) =>
      label -> newOidcSettingsPage.getTextFieldValue(label)
    })
    assertEquals(realSettings, newCommonDetails)
  }

  @DataProvider(name = "idp")
  def idpData(): Array[Array[Any]] = {
    Array(
      Array(
        "Auth0",
        Map(
          apiEndpointLabel -> "https://auth0.com/api",
          apiClientIdLabel -> "Auth0 client ID"
        )
      ),
      Array(
        "Okta",
        Map(
          apiEndpointLabel -> "https://okta.com/api",
          apiClientIdLabel -> "okta client ID"
        )
      )
    )
  }

  @Test(
    description = "User should be able to update API details for different IdP",
    dataProvider = "idp",
    dependsOnMethods = Array("updateGeneralDetails")
  )
  def updateApiDetails(idpName: String, apiSettings: Map[String, String]): Unit = {
    val oidcSettingsPage = new OidcSettingsPage(context).get()

    oidcSettingsPage.selectIdP(idpName)
    apiSettings.foreach({ case (label, value) =>
      oidcSettingsPage.inputTextField(label, value)
    })
    oidcSettingsPage.save()

    // Reload page and check the value.
    val newOidcSettingsPage = new OidcSettingsPage(context).load()
    assertEquals(newOidcSettingsPage.getIdpValue, idpName)
    apiSettings.foreach({ case (label, newValue) =>
      assertEquals(newOidcSettingsPage.getTextFieldValue(label), newValue)
    })
  }

  @Test(
    description = "User should be able to update the mapping details",
    dependsOnMethods = Array("updateGeneralDetails")
  )
  def updateMappingDetails(): Unit = {
    val newRoleClaim     = "new role"
    val oidcSettingsPage = new OidcSettingsPage(context).get()

    oidcSettingsPage.selectDefaultRoles(List("Developer"))
    oidcSettingsPage.inputTextField(roleClaimLabel, newRoleClaim)
    oidcSettingsPage.selectCustomRoles(customRoles)
    // todo: update user ID attribute after the support for all the three platforms are implemented.
    oidcSettingsPage.save()

    // Reload page and check the value.
    val newOidcSettingsPage = new OidcSettingsPage(context).load()
    // 2 original roles + 1 new role.
    assertEquals(newOidcSettingsPage.getDefaultRolesNumber, 3)
    assertEquals(newOidcSettingsPage.getCustomRolesNumber, 1)
    assertEquals(newOidcSettingsPage.getTextFieldValue(roleClaimLabel), newRoleClaim)
  }
}
