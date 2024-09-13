package com.tle.integration.oidc

import cats.data.Validated.{Invalid, Valid}
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.GenericIdentityProvider
import com.tle.integration.oidc.idp.IdentityProviderCodec._
import com.tle.integration.oidc.service.OidcConfigurationServiceImpl
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.{mock, never, verify, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class OidcConfigurationServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val mockConfigurationService: ConfigurationService = mock(classOf[ConfigurationService])
  val auth0: GenericIdentityProvider = GenericIdentityProvider(
    "Auth0",
    "C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ",
    "_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L",
    "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize",
    "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json",
    "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token",
    None,
    None,
    Set.empty,
    Map.empty,
    enabled = true,
    "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users",
    "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps",
    "JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I",
  )
  val auth0StringRepr =
    """{"name":"Auth0","clientId":"C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ","clientSecret":"_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L","authUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize","keysetUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json","tokenUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token","usernameClaim":null,"roleClaim":null,"unknownRoles":[],"customRoles":{},"enabled":true,"userListingUrl":"https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users","clientCredClientId":"1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps","clientCredClientSecret":"JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I"}"""
  val PROPERTY_NAME = "OIDC_IDENTITY_PROVIDER"

  class Fixture {
    val service = new OidcConfigurationServiceImpl(mockConfigurationService)
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
        idpStringRepr.capture(),
      )

      idpStringRepr.getValue shouldBe auth0StringRepr
    }

    it("captures all the invalid values") {
      val f = fixture

      Given("A configuration which has some invalid values")
      val badAuth0 = auth0.copy(
        authCodeClientId = "",
        authUrl = "http://abc/ authorise/",
        keysetUrl = "htp://keyset.com",
        userListingUrl = "www.userlisting.com",
        clientCredClientSecret = null,
      )

      When("attempting to save this configuration")
      val result = f.service.save(badAuth0)

      Then("All the invalid values should be captured")
      result shouldBe Left(
        "Missing value for required field: client ID,Invalid value for Auth URL: Illegal character in path at index 11: http://abc/ authorise/,Invalid value for Key set URL: unknown protocol: htp,Missing value for required field: Client Credentials Client secret,Invalid value for User listing URL: URI is not absolute")
    }

    it("captures other errors") {
      val f = fixture

      When("An exception is thrown from the saving")
      val exception = "Failed to save due to a DB issue"
      when(mockConfigurationService.setProperty(anyString(), anyString()))
        .thenThrow(new RuntimeException(exception))

      val result = f.service.save(auth0)

      Then("The error message should be captured")
      result shouldBe Left(exception)
    }
  }

  describe("Retrieving configuration") {

    it("retrieves a configuration through ConfigurationService") {
      val f = fixture

      Given("An Identity Provider whose string representation has been saved in DB")
      when(mockConfigurationService.getProperty(PROPERTY_NAME))
        .thenReturn(auth0StringRepr)

      When("The Identity Provider is retrieved")
      val result = f.service.get

      Then(
        "The string representation should have been converted to the object and returned through ConfigurationService")
      result shouldBe Right(auth0)
    }

    it("returns an error for retrieval if there isn't any configured Identity Provider") {
      val f = fixture

      Given("No Identity Provider has been configured")
      when(mockConfigurationService.getProperty(PROPERTY_NAME)).thenReturn(null)

      When("Attempting to retrieve a configuration")
      val result = f.service.get

      Then("An error message should be returned")
      result shouldBe Left("No Identity Provider configured")
    }

    it("returns an error for retrieval if the string representation is corrupted") {
      val f = fixture

      Given("A bad string representation that has been saved in DB")
      val badStringRepr = auth0StringRepr.replace("clientId", "client_Id")
      when(mockConfigurationService.getProperty(PROPERTY_NAME)).thenReturn(badStringRepr)

      When("Attempting to retrieve the configuration")
      val result = f.service.get

      Then("An error message should be returned")
      result shouldBe Left("DecodingFailure at .clientId: Missing required field")
    }
  }
}
