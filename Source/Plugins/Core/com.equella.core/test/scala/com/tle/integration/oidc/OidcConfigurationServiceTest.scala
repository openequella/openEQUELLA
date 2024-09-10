package com.tle.integration.oidc

import cats.data.Validated.{Invalid, Valid}
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.GenericIdentityProvider
import com.tle.integration.oidc.idp.IdentityProviderCodec._
import com.tle.integration.oidc.service.OidcConfigurationServiceImpl
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{mock, verify, when}
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

  class Fixture {
    val service = new OidcConfigurationServiceImpl(mockConfigurationService)
  }

  def fixture = new Fixture

  describe("DB operations") {
    it("saves an Identity Provider configuration") {
      val f = fixture

      When("The configuration of an Identity Provider is saved")
      f.service.save(auth0)

      Then(
        "String representation of the configuration should have been saved in DB through ConfigurationService")
      val idpStringRepr = ArgumentCaptor.forClass[String, String](classOf[String])
      verify(mockConfigurationService).setProperty(
        ArgumentCaptor.forClass[String, String](classOf[String]).capture(),
        idpStringRepr.capture(),
      )

      idpStringRepr.getValue shouldBe auth0StringRepr

    }

    it("retrieves the Identity Provider configuration") {
      val f = fixture

      Given("An Identity Provider whose string representation has been saved in DB")
      when(mockConfigurationService.getProperty(f.service.PROPERTY_NAME))
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
      when(mockConfigurationService.getProperty(f.service.PROPERTY_NAME)).thenReturn(null)

      When("Attempting to retrieve the configuration")
      val result = f.service.get

      Then("An error message should be returned")
      result shouldBe Left("Failed to get Identity Provider: No Identity Provider configured")
    }

    it("returns an error for retrieval if the string representation is corrupted") {
      val f = fixture

      Given("A bad string representation that has been saved in DB")
      val badStringRepr = auth0StringRepr.replace("clientId", "client_Id")
      when(mockConfigurationService.getProperty(f.service.PROPERTY_NAME)).thenReturn(badStringRepr)

      When("Attempting to retrieve the configuration")
      val result = f.service.get

      Then("An error message should be returned")
      result shouldBe Left(
        "Failed to get Identity Provider: DecodingFailure at .clientId: Missing required field")
    }
  }

  describe("Validation") {
    it("returns a validated Identity Provider") {
      val f = fixture

      When("An Identity Provider is successfully validated")
      val result = f.service.validate(auth0)

      Then("The Identity Provider should be returned")
      result shouldBe Valid(auth0)
    }

    it("captures errors for all the invalid values") {
      val f = fixture

      Given("An Identity Provider which has some invalid values")
      val badAuth0 = auth0.copy(
        clientId = "",
        authUrl = "http://abc/ authorise/",
        keysetUrl = "htp://keyset.com",
        userListingUrl = "www.userlisting.com",
        clientCredClientSecret = null,
      )

      When("A validation is performed for this Identity Provider")
      val result = f.service.validate(badAuth0)

      Then("All the invalid values should be captured")
      result shouldBe Invalid(
        List(
          "Missing value for required field: client ID",
          "Invalid value for Auth URL: Illegal character in path at index 11: http://abc/ authorise/",
          "Invalid value for Key set URL: unknown protocol: htp",
          "Missing value for required field: Client Credentials Client secret",
          "Invalid value for User listing URL: URI is not absolute"
        ))
    }

  }
}
