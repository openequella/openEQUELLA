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

package com.tle.integration.oidc

import com.auth0.jwk.{Jwk, JwkException, UrlJwkProvider}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.{JWTDecodeException, SignatureVerificationException}
import com.auth0.jwt.interfaces.DecodedJWT
import com.tle.beans.Institution
import com.tle.common.institution.CurrentInstitution
import com.tle.core.services.user.UserService
import com.tle.integration.jwk.JwkProvider
import com.tle.integration.oauth2.error.authorisation.{InvalidRequest, InvalidState}
import com.tle.integration.oauth2.error.general.{GeneralError, InvalidJWT, ServerError}
import com.tle.integration.oidc.idp.{
  CommonDetails,
  GenericIdentityProviderDetails,
  IdentityProviderPlatform
}
import com.tle.integration.oidc.service._
import io.lemonlabs.uri.AbsoluteUrl
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{mock, mockStatic, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table

import java.net.URI
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.time.{Clock, Instant}
import scala.jdk.CollectionConverters._

class OidcAuthServiceTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val commonDetails: CommonDetails = CommonDetails(
    platform = IdentityProviderPlatform.AUTH0,
    issuer = "https://dev-mzxj31ghj626ldi7.us.auth0.com/",
    authCodeClientId = "Mp6Ud5KqFKXOITXXXr3uqtZi5uLh7ORs",
    authCodeClientSecret = "_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L",
    authUrl = new URI("https://dev-mzxj31ghj626ldi7.us.auth0.com/authorize").toURL,
    keysetUrl = new URI("https://dev-mzxj31ghj626ldi7.us.auth0.com/.well-known/jwks.json").toURL,
    tokenUrl = new URI("https://dev-mzxj31ghj626ldi7.us.auth0.com/oauth/token").toURL,
    usernameClaim = None,
    defaultRoles = Set.empty,
    roleConfig = None,
    userIdAttribute = None,
    enabled = true
  )
  val auth0: GenericIdentityProviderDetails = GenericIdentityProviderDetails(
    commonDetails = commonDetails,
    apiUrl = new URI("https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users").toURL,
    apiClientId = "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps",
    apiClientSecret = "JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I"
  )

  // Mock of CurrentInstitution
  val inst = new Institution
  inst.setUniqueId(2024L)
  inst.setUrl("http://localhost:8080/test/")
  mockStatic(classOf[CurrentInstitution])
  when(CurrentInstitution.get()).thenReturn(inst)

  // Mock of the essential Services
  val mockUserService: UserService                       = mock(classOf[UserService])
  val mockJwkProvider: JwkProvider                       = mock(classOf[JwkProvider])
  val mockConfigurationService: OidcConfigurationService = mock(classOf[OidcConfigurationService])
  val mockStateService: OidcStateService                 = mock(classOf[OidcStateService])
  implicit val mockNonceService: OidcNonceService        = mock(classOf[OidcNonceService])
  val authService: OidcAuthService = new OidcAuthService(
    mockStateService,
    mockUserService,
    mockConfigurationService,
    mockJwkProvider,
    java.util.Collections.emptyMap()
  )

  when(mockConfigurationService.get).thenReturn(Right(auth0))
  val STATE = "5a3f7b2c9d12a4f8"
  when(mockStateService.createState(any[OidcStateDetails]())).thenReturn(STATE)
  val NONCE = "4bff7b2c9d12a4f8"
  when(mockNonceService.createNonce(STATE)).thenReturn(NONCE)

  describe("Authorisation request URL") {
    it("builds a URL that points to the configured IdP's authorisation endpoint") {
      Given("an OIDC configuration")
      val result = authService.buildAuthUrl(
        authUrl = commonDetails.authUrl.toString,
        clientId = commonDetails.authCodeClientId,
        targetPage = "hierarchy.do"
      )

      Then("the URL should consist of correct path and query params")
      // Due to the limitation of mocking a Scala package object, it's pretty hard to mock the function `generatePKCEPair`.
      // Hence, instead of checking the full URL string repr, we can check the path and each query param individually.
      val url: AbsoluteUrl = AbsoluteUrl.parse(result)
      s"${url.scheme}://${url.host}${url.path}" shouldBe commonDetails.authUrl.toString

      val params = url.query.paramMap.map { case (k, v) =>
        k -> v.head
      } // We do not have any param that has multiple values.
      params(OpenIDConnectParams.CLIENT_ID) shouldBe commonDetails.authCodeClientId
      params(OpenIDConnectParams.RESPONSE_TYPE) shouldBe "code"
      params(OpenIDConnectParams.REDIRECT_URI) shouldBe "http://localhost:8080/test/oidc/callback"
      params(OpenIDConnectParams.SCOPE) shouldBe "openid profile email"
      params(OpenIDConnectParams.STATE) shouldBe STATE
      params(OpenIDConnectParams.NONCE) shouldBe NONCE
      params(OpenIDConnectParams.CODE_CHALLENGE_METHOD) shouldBe "S256"
      params.get(OpenIDConnectParams.CODE_CHALLENGE) shouldBe a[Some[
        _
      ]] // Not checking the actual value, but it should exist in the param map.
    }
  }

  describe("Verify the callback request") {
    val CODE = "rEMTJgu2sdCHaoeK1t8URWbuGJSst"

    it("returns the callback details if the verification is successful") {
      Given("a callback request with the required params")
      val params = Map(
        OpenIDConnectParams.CODE  -> Array(CODE),
        OpenIDConnectParams.STATE -> Array(STATE)
      )

      And("the state details that can be found by the returned state")
      val mockStateDetails =
        OidcStateDetails(
          codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
          codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
          targetPage = None
        )
      when(mockStateService.getState(STATE)).thenReturn(Option(mockStateDetails))

      Then(
        "the result should contain the values of param 'code' and 'state', and the state details"
      )
      val result = authService.verifyCallbackRequest(params)
      result shouldBe Right(OidcCallbackDetails(CODE, STATE, mockStateDetails))
    }

    val invalidParams = Table(
      ("params", "missingParam", "error"),
      (Map(OpenIDConnectParams.CODE -> Array(CODE)), "state", "Missing required parameter 'state'"),
      (Map(OpenIDConnectParams.STATE -> Array(STATE)), "code", "Missing required parameter 'code'")
    )

    it("returns an error if any required param is missing") {
      forAll(invalidParams) { (params, missingParam, error) =>
        Given(s"a callback request without the required param $missingParam")
        Then("the result should be an error of InvalidRequest")
        val result = authService.verifyCallbackRequest(params)

        result shouldBe Left(InvalidRequest(error))
      }
    }

    it("returns an error of InvalidState if no state details can be found by the returned state") {
      Given("a callback request with a state")
      val invalidState = "abc"
      val params = Map(
        OpenIDConnectParams.CODE  -> Array(CODE),
        OpenIDConnectParams.STATE -> Array(invalidState)
      )

      When("the state details cannot be found by the returned state")
      when(mockStateService.getState(invalidState)).thenReturn(None)

      Then("the result should be an error of InvalidState")
      val result = authService.verifyCallbackRequest(params)
      result shouldBe Left(InvalidState(s"Invalid state provided: $invalidState"))
    }
  }

  describe("ID token verification") {
    val rawToken =
      "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InJUWExiWmRiYjljY3dOcFRMdkRBdyJ9.eyJpZHBSb2xlcyI6ImRldmVsb3BlciIsImN1c3RvbVVzZXJuYW1lIjoiQ2xheSBUaG9tcHNvbiIsIm5pY2tuYW1lIjoicGVuZ2hhaS56aGFuZyIsIm5hbWUiOiJwZW5naGFpLnpoYW5nQGVkYWxleC5jb20iLCJwaWN0dXJlIjoiaHR0cHM6Ly9zLmdyYXZhdGFyLmNvbS9hdmF0YXIvZWQ2NjkyZGNmZjEyNTY2N2I5Y2FlMDc2NjUwYzllM2Q_cz00ODAmcj1wZyZkPWh0dHBzJTNBJTJGJTJGY2RuLmF1dGgwLmNvbSUyRmF2YXRhcnMlMkZwZS5wbmciLCJ1cGRhdGVkX2F0IjoiMjAyNC0xMS0wOFQwMDozOTo1Mi42NzBaIiwiZW1haWwiOiJwZW5naGFpLnpoYW5nQGVkYWxleC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiaXNzIjoiaHR0cHM6Ly9kZXYtbXp4ajMxZ2hqNjI2bGRpNy51cy5hdXRoMC5jb20vIiwiYXVkIjoiTXA2VWQ1S3FGS1hPSVRYWFhyM3VxdFppNXVMaDdPUnMiLCJpYXQiOjE3MzEyOTgzMzcsImV4cCI6MTczMTMzNDMzNywic3ViIjoiYXV0aDB8NjY1NDE4OTc5NzFkZmE0MWQ0MmViMTgwIiwic2lkIjoiUUg1aVItd2xuM1d5bEltSHVZM1p0QjZVX2RpT1N0WEQiLCJub25jZSI6ImY3MjkyNTBlY2Q1OTI1Y2M5OTdiNmFkMjViYWUwZjliIn0.Z5luf-rsEBIW9ZGMn9MKWr-f7qoTizhd0ogEVhGJUYo-dEOP9Yeb35KYy-Hwdl5004coaSOk6lbR8EmeMG3WLwKvlH1M3yufzCX0wyaufb2UZjlYrjEmYZ_qddDLcyXn4VGaGdGWb4SOtGfcNsAk_hoHkT6QA5So_hMj1x630zd20Zjj_-2cWfoxejIPAmiE-AWNYih7-i-Eh60iSTmSK9O3M5oy_nyLCRs4jw6mliiBrGWpsZ6O7eE-Ul3FUNkFT8g0gCX4Nd-lD50D8r5Gu7532bjXnjr5e9O0FUysn8kjZbpHMVyEukdZ0TyF36uIqnRUkQ2-bloeuWph3fpgAA"
    val jwk = new Jwk(
      "rTXLbZdbb9ccwNpTLvDAw",
      "RSA",
      "RS256",
      "sig",
      List.empty[String].asJava,
      null,
      List(
        "MIIDHTCCAgWgAwIBAgIJFstmfjVyZxjhMA0GCSqGSIb3DQEBCwUAMCwxKjAoBgNVBAMTIWRldi1tenhqMzFnaGo2MjZsZGk3LnVzLmF1dGgwLmNvbTAeFw0yNDA1MjcwNTIxMzBaFw0zODAyMDMwNTIxMzBaMCwxKjAoBgNVBAMTIWRldi1tenhqMzFnaGo2MjZsZGk3LnVzLmF1dGgwLmNvbTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAM5usPSje+Y2rkPRyUG1naR+iq2LSwdfFGD4VP+7/0eYTIW/0ldgcq2HufufcGOHfQacH6YIVAoh6yUHe53bppuGDRnSBZz6QVXEsevPCxKOxuiv3gP8ik8n8vPAJ0HWXKpZOtIT8u04RBymI9qjJQiS3pTCL0aG+PQv8LxM9Jk1HT5n48BhR9YDQchlz7PQQBM/BFa+Gxmy9q8jVZzmRYQCMIWPQqEqX47ey64WIkdrvHC0wWGD5M+f2NbFzuN2gJwvM4dJ2e6Ok751hayx5zbufwcGgvvA/lX2z2nfP6cjQaYLIOwfFSbkOMrH4yACfx4yKYymDQOXLUnIfRpIcncCAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4EFgQUFfF/X2+Ob2+/aQUScp+mfakxUgkwDgYDVR0PAQH/BAQDAgKEMA0GCSqGSIb3DQEBCwUAA4IBAQAn9OThHCYen397Wp+bFWwzVPMz48+SwptZcYEusRTQHrDFIMEVqhBXaxo2Oo18IiH3eJGhnoSesvru9o1uugRDuYugG498a9oWA0pMoe1CiarEEY0A0xbu90KUu2rZVGZxd2vgbn5wAUisjXA/S8MyY09DQDlb09gOyaryx1Cjmb40CxM1lgxMmRc37vbo9VM2ekNixkby/MKddpOnsadV/pTlIxjycixFAJtFa2C48n3/8aqhOhQY93VhOpQLibhwlwB/bEsfDa7LWtjYdavLTVg2OkPJDidA0MmQy0/KJuzQWbiNtm5cte4kWtjk1FIp0y4VP28Bl7RZmmY6XcqL"
      ).asJava,
      "ahVtSgCswOouANtKH_nlrg3cH8Y",
      Map(
        "e" -> "AQAB".asInstanceOf[Object],
        "n" -> "zm6w9KN75jauQ9HJQbWdpH6KrYtLB18UYPhU_7v_R5hMhb_SV2ByrYe5-59wY4d9BpwfpghUCiHrJQd7ndumm4YNGdIFnPpBVcSx688LEo7G6K_eA_yKTyfy88AnQdZcqlk60hPy7ThEHKYj2qMlCJLelMIvRob49C_wvEz0mTUdPmfjwGFH1gNByGXPs9BAEz8EVr4bGbL2ryNVnOZFhAIwhY9CoSpfjt7LrhYiR2u8cLTBYYPkz5_Y1sXO43aAnC8zh0nZ7o6TvnWFrLHnNu5_BwaC-8D-VfbPad8_pyNBpgsg7B8VJuQ4ysfjIAJ_HjIpjKYNA5ctSch9Gkhydw"
          .asInstanceOf[Object]
      ).asJava
    )

    // Mock of system clock
    val mockedClock = mock(classOf[Clock])
    when(mockedClock.instant()).thenReturn(Instant.parse("2024-11-11T05:00:00Z"))
    mockStatic(classOf[Clock]).when(() => Clock.systemUTC()).thenReturn(mockedClock)

    // Mock of the JWK Provider and the function that returns a JWK Provider
    val jwkProvider: UrlJwkProvider = mock(classOf[UrlJwkProvider])
    when(jwkProvider.get(anyString)).thenReturn(jwk)
    when(mockJwkProvider.get(commonDetails.keysetUrl)).thenReturn(Right(jwkProvider))

    // Mock of the Nonce validation
    when(mockNonceService.validateNonce(anyString(), anyString())).thenReturn(Right(true))

    it("returns the decoded and verified token if the verification is successful") {
      When("a raw ID token which passes all the verifications")
      Then("the result should be the decoded and verified token")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe a[Right[GeneralError, DecodedJWT]]
    }

    it("returns an error if the verification of claim 'ISSUER' fails") {
      Given("a raw ID token whose issuer is different from the configured issuer")
      val okta = auth0.copy(commonDetails = auth0.commonDetails.copy(issuer = "okta"))

      Then("The result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, okta) shouldBe Left(
        InvalidJWT(
          "Provided JWT failed signature verification: The Claim 'iss' value doesn't match the required issuer."
        )
      )
    }

    it("returns an error if the verification of claim 'AUDIENCE' fails") {
      Given("a raw ID token whose audience is different from the configured audience")
      val clientA =
        auth0.copy(commonDetails = auth0.commonDetails.copy(authCodeClientId = "client A"))

      Then("The result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, clientA) shouldBe Left(
        InvalidJWT(
          "Provided JWT failed signature verification: The Claim 'aud' value doesn't contain the required audience."
        )
      )
    }

    it("returns an error if the verification of claim 'EXPIRES_AT' fails") {
      Given("a raw ID token that has expired")
      when(mockedClock.instant()).thenReturn(Instant.parse("2024-12-31T05:00:00Z"))

      Then("the result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(
        InvalidJWT(
          "Provided JWT failed signature verification: The Token has expired on 2024-11-11T14:12:17Z."
        )
      )
    }

    it("returns an error if the verification of claim 'ISSUED_AT' fails") {
      Given("a raw ID token that is supposed to be issued in the future")
      when(mockedClock.instant()).thenReturn(Instant.parse("2024-01-01T05:00:00Z"))

      Then("the result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(
        InvalidJWT(
          "Provided JWT failed signature verification: The Token can't be used before 2024-11-11T04:12:17Z."
        )
      )
    }

    it("returns an error if the verification of signing signature fails") {
      Given("an algorithm which uses a different public key")
      val mockPublicKey = mock(classOf[RSAPublicKey])
      val error         = new SignatureVerificationException(Algorithm.RSA256(mockPublicKey, null))

      // Mock of an algorithm and the static method that returns RSA256 algorithm.
      val mockRsaAlg = mock(classOf[Algorithm])
      when(mockRsaAlg.getName).thenReturn("RS256")
      mockStatic(classOf[Algorithm])
        .when(() => Algorithm.RSA256(any[RSAPublicKey], any[RSAPrivateKey]))
        .thenReturn(mockRsaAlg)

      When("the signature verification fails")
      when(mockRsaAlg.verify(any[DecodedJWT])).thenThrow(error)

      Then("the result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(
        InvalidJWT(
          "Provided JWT failed signature verification: The Token's Signature resulted invalid when verified using the Algorithm: SHA256withRSA"
        )
      )
    }

    it("returns an error if no JWK can be found") {
      When("There isn't any JWK found by the token's key ID")
      when(jwkProvider.get(anyString)).thenThrow(new JwkException("No JWK found"))

      Then("the result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(
        InvalidJWT("Failed to retrieve JWK by the obtained ID token's key ID")
      )
    }

    it("returns an error if a JWK Provider cannot be established") {
      val error = ServerError("Failed to establish JWK provider")

      When("a JWK Provider cannot be established with the configured keyset URL")
      when(mockJwkProvider.get(commonDetails.keysetUrl)).thenReturn(Left(error))

      Then("the result should be an error of ServerError")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(error)
    }

    it("returns an error if the token decoding fails") {
      Given("decoding a raw token fails")
      mockStatic(classOf[JWT])
      when(JWT.decode(rawToken)).thenThrow(new JWTDecodeException("The token is broken"))

      Then("The result should be an error of InvalidJWT")
      authService.verifyIdToken(rawToken, STATE, auth0) shouldBe Left(
        InvalidJWT("Failed to decode token: The token is broken")
      )
    }
  }
}
