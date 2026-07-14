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

package com.tle.integration.oidc.service

import com.tle.core.services.user.UserService
import com.tle.integration.jwk.JwkProvider
import com.tle.integration.oauth2.error.authorisation.{AccessDenied, NotAuthorized}
import com.tle.integration.oauth2.error.general.ServerError
import com.tle.integration.oauth2.error.token.InvalidClient
import io.circe.ParsingFailure
import org.mockito.Mockito.mock
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import sttp.client3.circe._
import sttp.client3.{DeserializationException, HttpError}
import sttp.model.StatusCode

class OidcAuthServiceTokenErrorTest extends AnyFunSpec with Matchers with GivenWhenThen {
  implicit val mockNonceService: OidcNonceService = mock(classOf[OidcNonceService])
  val authService: OidcAuthService                = new OidcAuthService(
    mock(classOf[OidcStateService]),
    mock(classOf[UserService]),
    mock(classOf[OidcConfigurationService]),
    mock(classOf[JwkProvider]),
    java.util.Collections.emptyMap()
  )

  val tokenErrorBody =
    """{"error": "invalid_client", "error_description": "Client authentication failed"}"""

  describe("handleTokenError") {
    it("maps HTTP client errors with a standard OAuth2 error body to the matching error type") {
      val expectations = Table(
        ("status", "expected error"),
        (StatusCode.BadRequest, InvalidClient("Client authentication failed")),
        (StatusCode.Unauthorized, NotAuthorized("Client authentication failed")),
        (StatusCode.Forbidden, AccessDenied("Client authentication failed"))
      )

      forAll(expectations) { (status, expected) =>
        authService.handleTokenError(HttpError(tokenErrorBody, status)) shouldBe expected
      }
    }

    it("maps a client error with an unexpected body to a ServerError") {
      Given("a 400 response whose body is not a standard OAuth2 error")
      val result = authService.handleTokenError(HttpError("not json", StatusCode.BadRequest))

      Then("the failure is reported as a ServerError")
      result shouldBe ServerError(
        "Failed to request an ID token, but the error is unknown due to unexpected response format."
      )
    }

    it("maps an HTTP server error to a ServerError") {
      authService.handleTokenError(
        HttpError("Auth server error", StatusCode.InternalServerError)
      ) shouldBe ServerError("Failed to request an ID token: Auth server error")
    }

    it("maps any other HTTP error status (e.g. a redirect) to a ServerError") {
      Given("a token endpoint response that is neither a client error nor a server error")
      val error = HttpError("Moved", StatusCode.PermanentRedirect)

      Then("the failure is still reported as a ServerError rather than throwing a MatchError")
      authService.handleTokenError(error) shouldBe ServerError(
        "Failed to request an ID token: Moved"
      )
    }

    it("maps a deserialisation failure to a ServerError") {
      val error = DeserializationException(
        "unexpected body",
        ParsingFailure("bad json", new RuntimeException("bad json"))
      )

      authService.handleTokenError(error) shouldBe ServerError(
        "An ID Token has been issued but can't be retrieved from an unexpected response format: bad json"
      )
    }
  }
}
