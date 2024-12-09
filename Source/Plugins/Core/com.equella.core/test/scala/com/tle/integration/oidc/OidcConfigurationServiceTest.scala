package com.tle.integration.oidc

import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.auditlog.AuditLogService
import com.tle.core.encryption.EncryptionService
import com.tle.core.encryption.impl.EncryptionServiceImpl
import com.tle.core.services.user.UserService
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.{CommonDetails, Auth0, GenericIdentityProviderDetails}
import com.tle.integration.oidc.service.OidcConfigurationServiceImpl
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito._
import org.scalatest.GivenWhenThen
import org.scalatest.Inside.inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI

class OidcConfigurationServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val mockConfigurationService: ConfigurationService = mock(classOf[ConfigurationService])
  val mockAuditLogService: AuditLogService           = mock(classOf[AuditLogService])
  val userService: UserService                       = mock(classOf[UserService])
  implicit val encryptionService: EncryptionService  = new EncryptionServiceImpl

  val auth0: Auth0 = Auth0(
    issuer = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com",
    authCodeClientId = "C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ",
    authCodeClientSecret =
      Option("_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L"),
    authUrl = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize",
    keysetUrl = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json",
    tokenUrl = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token",
    usernameClaim = None,
    defaultRoles = Set.empty,
    roleConfig = None,
    enabled = true,
    apiUrl = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users",
    apiClientId = "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps",
    apiClientSecret = Option("JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I")
  )

  val auth0EncryptedStringRepr =
    """{"commonDetails":{"platform":"AUTH0","issuer":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com","authCodeClientId":"C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ","authCodeClientSecret":"0RnV+1iXrd3qJDnTjjgaoU4i5/1Vxz1i6myVJh6X/yN2aerAXLdBd/E8fq9yLT8DhX5PR0ekjYk7BB10Bzy4fqQJO0TLKkZXTFopUTHZdh0=","authUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize","keysetUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json","tokenUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token","usernameClaim":null,"defaultRoles":[],"roleConfig":null,"enabled":true},"apiUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users","apiClientId":"1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps","apiClientSecret":"UytNdbUEE44SRQg/Tz40tQ7sNXa1ufZKCeHJOlfIH/rIdBvz8W+XhseTAsIA0tWUZ4wm8dcKClWmaubj2J9UB035i0sWOmwUiQxWPlFmRD8="}"""
  val PROPERTY_NAME = "OIDC_IDENTITY_PROVIDER"

  mockStatic(classOf[CurrentUser])
  when(CurrentUser.getUsername).thenReturn("Test user")

  class Fixture {
    val service =
      new OidcConfigurationServiceImpl(mockConfigurationService, mockAuditLogService, userService)
  }

  def fixture = new Fixture

  describe("Saving configuration") {

    it("saves a valid configuration through Configuration") {
      val f = fixture

      When("A valid configuration is saved")
      f.service.save(auth0)

      Then("String representation of the configuration is sent to the ConfigurationService")
      val idpStringRepr = ArgumentCaptor.forClass[String, String](classOf[String])
      verify(mockConfigurationService).setProperty(
        ArgumentCaptor.forClass[String, String](classOf[String]).capture(),
        idpStringRepr.capture()
      )

      idpStringRepr.getValue shouldBe auth0EncryptedStringRepr
    }

    it("captures all the invalid values") {
      val f = fixture

      Given("A configuration which has some invalid values")
      val badAuth0 = auth0.copy(
        authCodeClientId = "",
        authUrl = "http://abc/ authorise/",
        keysetUrl = "htp://keyset.com",
        apiUrl = "www.userlisting.com"
      )

      When("attempting to save this configuration")
      val result = f.service.save(badAuth0)

      Then("All the invalid values should be captured")
      inside(result) { case Left(e) =>
        e.getMessage shouldBe "Missing value for required field: Authorisation Code flow Client ID,Invalid value for Auth URL: Illegal character in path at index 11: http://abc/ authorise/,Invalid value for Key set URL: unknown protocol: htp,Invalid value for API URL: URI is not absolute"
      }
    }

    it("captures other errors") {
      val f = fixture

      When("An exception is thrown from the saving")
      val error = "Failed to save due to a DB issue"
      when(mockConfigurationService.setProperty(anyString(), anyString()))
        .thenThrow(new RuntimeException(error))

      val result = f.service.save(auth0)

      Then("The error message should be captured")
      inside(result) { case Left(e) =>
        e.getMessage shouldBe error
      }
    }

    it("keeps the existing sensitive values if these values are absent in a new configuration") {
      val f = fixture

      Given("An existing configuration")
      when(mockConfigurationService.getProperty(PROPERTY_NAME))
        .thenReturn(auth0EncryptedStringRepr)

      When("A new configuration does not include sensitive values")
      val newAuth0 = auth0.copy(
        authCodeClientSecret = None,
        apiClientSecret = None
      )
      f.service.save(newAuth0)

      Then("The existing sensitive values should be kept")
      val idpStringRepr = ArgumentCaptor.forClass[String, String](classOf[String])
      verify(mockConfigurationService, atLeastOnce()).setProperty(
        ArgumentCaptor.forClass[String, String](classOf[String]).capture(),
        idpStringRepr.capture()
      )

      idpStringRepr.getValue shouldBe auth0EncryptedStringRepr
    }

    it(
      "returns errors for sensitive values if they are neither provided or available in an existing config"
    ) {
      val f = fixture

      Given("No configuration is available")
      when(mockConfigurationService.getProperty(PROPERTY_NAME))
        .thenReturn(null)

      When("A new configuration does not include sensitive values")
      val newAuth0 = auth0.copy(
        authCodeClientSecret = None,
        apiClientSecret = None
      )

      Then("Error messages returned for the missing sensitive values")
      val result = f.service.save(newAuth0)
      inside(result) { case Left(e) =>
        e.getMessage shouldBe
          "Missing value for required field: Authorisation Code flow Client Secret," +
          "Missing value for required field: API Client Secret"
      }
    }

    it("creates an audit log for the saving") {
      val f = fixture

      When("A valid configuration is saved")
      f.service.save(auth0)

      Then("An audit log should be created for the saving")
      verify(mockAuditLogService, atLeastOnce()).logGeneric(
        "OIDC",
        "update IdP",
        null,
        null,
        null,
        auth0EncryptedStringRepr
      )
    }

  }

  describe("Retrieving configuration") {

    it("retrieves a configuration through ConfigurationService") {
      val f = fixture

      Given("An Identity Provider whose string representation has been saved in DB")
      when(mockConfigurationService.getProperty(PROPERTY_NAME))
        .thenReturn(auth0EncryptedStringRepr)

      When("The Identity Provider is retrieved")
      val result = f.service.get

      Then(
        "The string representation should have been converted to the object and returned through ConfigurationService"
      )
      val expected = GenericIdentityProviderDetails(
        commonDetails = CommonDetails(
          platform = auth0.platform,
          issuer = auth0.issuer,
          authCodeClientId = auth0.authCodeClientId,
          authCodeClientSecret = auth0.authCodeClientSecret.get,
          authUrl = URI.create(auth0.authUrl).toURL,
          keysetUrl = URI.create(auth0.keysetUrl).toURL,
          tokenUrl = URI.create(auth0.tokenUrl).toURL,
          usernameClaim = auth0.usernameClaim,
          defaultRoles = auth0.defaultRoles,
          roleConfig = auth0.roleConfig,
          enabled = auth0.enabled
        ),
        apiUrl = URI.create(auth0.apiUrl).toURL,
        apiClientId = auth0.apiClientId,
        apiClientSecret = auth0.apiClientSecret.get
      )
      result shouldBe Right(expected)
    }

    it("returns an error for retrieval if there isn't any configured Identity Provider") {
      val f = fixture

      Given("No Identity Provider has been configured")
      when(mockConfigurationService.getProperty(PROPERTY_NAME)).thenReturn(null)

      When("Attempting to retrieve a configuration")
      val result = f.service.get

      Then("An error message should be returned")
      inside(result) { case Left(e) =>
        e.getMessage shouldBe "No Identity Provider configured"
      }
    }

    it("returns an error for retrieval if the string representation is corrupted") {
      val f = fixture

      Given("A bad string representation that has been saved in DB")
      val badStringRepr =
        auth0EncryptedStringRepr.replace("authCodeClientId", "auth_Code_Client_Id")
      when(mockConfigurationService.getProperty(PROPERTY_NAME)).thenReturn(badStringRepr)

      When("Attempting to retrieve the configuration")
      val result = f.service.get

      Then("An error message should be returned")
      inside(result) { case Left(e) =>
        e.getMessage shouldBe "DecodingFailure at .authCodeClientId: Missing required field"
      }
    }
  }
}
