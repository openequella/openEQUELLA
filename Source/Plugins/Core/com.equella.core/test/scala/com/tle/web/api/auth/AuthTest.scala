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

package com.tle.web.api.auth

import com.tle.common.i18n.CurrentLocale
import com.tle.common.usermanagement.user.WebAuthenticationDetails
import com.tle.core.auditlog.AuditLogService
import com.tle.core.institution.InstitutionService
import com.tle.core.services.user.{UserService, UserSessionService}
import com.tle.exceptions.{
  AccountExpiredException,
  AuthenticationException,
  BadCredentialsException
}
import com.tle.web.resources.{PluginResourceHelper, ResourcesService}
import org.mockito.ArgumentMatchers.{any, anyBoolean, anyString}
import org.mockito.Mockito._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table

import java.net.URI
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

class AuthTest extends AnyFunSpec with Matchers {

  val institutionUrl = "http://oeq.example.edu:8080/inst/"
  val credentials    = LoginRequest("jsmith", "secret")

  /** The origin of `institutionUrl` — scheme://host:port with no path — as a browser at the
    * institution would send in an `Origin` header.
    */
  val institutionOrigin: String = {
    val uri = URI.create(institutionUrl)
    s"${uri.getScheme}://${uri.getAuthority}"
  }

  // Message lookups go through static helpers which require the plugin framework at runtime;
  // stub them to echo the lookup key so assertions can target the keys directly.
  private val resourceHelper = mock(classOf[PluginResourceHelper])
  when(resourceHelper.key(anyString())).thenAnswer(invocation => invocation.getArgument[String](0))

  mockStatic(classOf[ResourcesService])
    .when(() => ResourcesService.getResourceHelper(any[Object]()))
    .thenReturn(resourceHelper)

  mockStatic(classOf[CurrentLocale])
    .when(() => CurrentLocale.get(any[String]()))
    .thenAnswer(invocation => invocation.getArgument[String](0))

  class Fixture {
    val userService: UserService               = mock(classOf[UserService])
    val userSessionService: UserSessionService = mock(classOf[UserSessionService])
    val auditLogService: AuditLogService       = mock(classOf[AuditLogService])
    val institutionService: InstitutionService = mock(classOf[InstitutionService])
    val req: HttpServletRequest                = mock(classOf[HttpServletRequest])
    val wad: WebAuthenticationDetails          = mock(classOf[WebAuthenticationDetails])

    when(userService.getWebAuthenticationDetails(req)).thenReturn(wad)
    when(institutionService.getInstitutionUrl).thenReturn(URI.create(institutionUrl).toURL)

    val auth = new Auth(userService, userSessionService, auditLogService, institutionService)

    def login(): Response = auth.login(req, credentials)

    def givenHeader(name: String, value: String): Unit =
      when(req.getHeader(name)).thenReturn(value)
  }

  def fixture = new Fixture

  /** Asserts the response has the given HTTP status. */
  private def assertStatus(response: Response, expected: Status): Unit =
    response.getStatus shouldBe expected.getStatusCode

  describe("login") {

    it("returns 200 and establishes the session for valid credentials") {
      val f = fixture

      assertStatus(f.login(), Status.OK)

      verify(f.userSessionService).reenableSessionUse()
      verify(f.userService).login(credentials.username, credentials.password, f.wad, true)
      verify(f.auditLogService, never()).logUserFailedAuthentication(anyString(), any())
    }

    it("returns an identical generic 401 for every kind of authentication failure") {
      val failures = Table(
        "authentication failure",
        new BadCredentialsException("Bad credentials"),
        new AccountExpiredException("Your account has expired - contact your system admin"),
        new AuthenticationException("Some other authentication problem")
      )

      // Every row is asserted against the same fixed status and body, so the response cannot
      // vary with the failure cause (no account-state disclosure).
      forAll(failures) { failure =>
        val f = fixture
        when(f.userService.login(anyString(), anyString(), any(), anyBoolean()))
          .thenThrow(failure)

        val response = f.login()

        assertStatus(response, Status.UNAUTHORIZED)
        response.getEntity shouldBe "logon.invalid"
        verify(f.auditLogService).logUserFailedAuthentication(credentials.username, f.wad)
      }
    }
  }

  describe("login origin enforcement") {

    it("rejects a cross-site Origin with 403 without attempting authentication") {
      val f = fixture
      f.givenHeader("Origin", "https://evil.example.com")

      val response = f.login()

      assertStatus(response, Status.FORBIDDEN)
      response.getEntity shouldBe "logon.problems"
      verify(f.auditLogService).logUserFailedAuthentication(credentials.username, f.wad)
      verify(f.userService, never()).login(anyString(), anyString(), any(), anyBoolean())
      verify(f.userSessionService, never()).reenableSessionUse()
    }

    it("allows a matching Origin") {
      val f = fixture
      f.givenHeader("Origin", institutionOrigin)

      assertStatus(f.login(), Status.OK)
    }

    it("rejects a cross-site Sec-Fetch-Site with 403") {
      val f = fixture
      f.givenHeader("Sec-Fetch-Site", "cross-site")

      assertStatus(f.login(), Status.FORBIDDEN)
    }
  }

  describe("isTrustedOrigin") {
    val institution = URI.create(institutionUrl)

    it("trusts or rejects a request based on its Origin and Sec-Fetch-Site headers") {
      val cases = Table(
        ("request", "origin", "secFetchSite", "trusted"),
        ("neither header (non-browser client)", None, None, true),
        ("Origin matching the institution scheme/host/port", Some(institutionOrigin), None, true),
        ("matching Origin differing only in case", Some(institutionOrigin.toUpperCase), None, true),
        ("Origin with a different host", Some("http://evil.example.com:8080"), None, false),
        ("Origin with a different scheme", Some("https://oeq.example.edu:8080"), None, false),
        ("Origin with a different port", Some("http://oeq.example.edu:9090"), None, false),
        ("the literal Origin value 'null'", Some("null"), None, false),
        ("an unparseable Origin", Some("not a uri://"), None, false),
        ("Sec-Fetch-Site: cross-site without an Origin", None, Some("cross-site"), false),
        ("Sec-Fetch-Site: same-origin", None, Some("same-origin"), true),
        (
          "matching Origin but Sec-Fetch-Site: cross-site — both checks must pass",
          Some(institutionOrigin),
          Some("cross-site"),
          false
        )
      )

      forAll(cases) { (_, origin, secFetchSite, expected) =>
        Auth.isTrustedOrigin(origin, secFetchSite, institution) shouldBe expected
      }
    }

    it("applies default ports when the Origin omits them") {
      // Same host as the institution, but https on both sides: the Origin omits the port while
      // the institution URL carries an explicit :443 — the two must still be considered equal.
      val host = institution.getHost
      Auth.isTrustedOrigin(
        Some(s"https://$host"),
        None,
        URI.create(s"https://$host:443/inst/")
      ) shouldBe true
    }
  }
}
