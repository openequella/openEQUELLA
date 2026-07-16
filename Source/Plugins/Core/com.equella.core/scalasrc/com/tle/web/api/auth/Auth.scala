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
import com.tle.core.guice.Bind
import com.tle.core.institution.InstitutionService
import com.tle.core.services.user.{UserService, UserSessionService}
import com.tle.exceptions.AuthenticationException
import com.tle.web.resources.{PluginResourceHelper, ResourcesService}
import io.swagger.annotations.{Api, ApiOperation}

import java.net.URI
import javax.inject.{Inject, Singleton}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{Context, MediaType, Response}
import javax.ws.rs.{Consumes, POST, PUT, Path}
import scala.util.{Failure, Success, Try}

/** JSON request body for `POST auth/login`. Credentials are carried in the body (never the query
  * string) so they cannot leak into server, proxy or browser logs.
  */
final case class LoginRequest(username: String, password: String)

object Auth {

  /** A rejected login attempt: the HTTP status to return and the i18n key of the generic message
    * for the response body.
    */
  private case class LoginFailure(status: Response.Status, messageKey: String)

  /** The components of a web origin as compared for the CSRF check — scheme, host and effective
    * port — modelled as a value type so two origins can be compared by simple equality.
    */
  private case class WebOrigin(scheme: String, host: String, port: Int)

  private object WebOrigin {

    /** Extracts the origin components of a URI. `None` when the URI has no scheme or host — which
      * covers the literal `Origin: null` sent for opaque origins.
      */
    def from(uri: URI): Option[WebOrigin] =
      for {
        scheme <- Option(uri.getScheme)
        host   <- Option(uri.getHost)
      } yield WebOrigin(scheme.toLowerCase, host.toLowerCase, effectivePort(uri))

    /** As per `from`, but starting with a raw header value; `None` if it isn't a valid URI. */
    def parse(value: String): Option[WebOrigin] =
      Try(URI.create(value)).toOption.flatMap(from)

    /** The URI's explicit port, or the default port for its scheme. Only http/https defaults are
      * needed: institution URLs are always web URLs, and the scheme equality in `WebOrigin`
      * comparison rejects any other combination before the port could matter.
      */
    private def effectivePort(uri: URI): Int =
      uri.getPort match {
        case -1 => if ("https".equalsIgnoreCase(uri.getScheme)) 443 else 80
        case p  => p
      }
  }

  /** Validates the browser-supplied `Origin` and `Sec-Fetch-Site` headers against the institution
    * URL as a defence-in-depth login CSRF control. Browsers attach these headers automatically and
    * cross-site pages can neither forge nor strip them, whereas non-browser clients (Admin Console,
    * scripts) typically send neither — so absent headers are deliberately allowed (fail-open). The
    * primary CSRF control is the JSON content-type requirement on the endpoint; any browser-borne
    * cross-site request necessarily carries an `Origin`.
    *
    * @param origin
    *   value of the `Origin` header, if present
    * @param secFetchSite
    *   value of the `Sec-Fetch-Site` header, if present
    * @param institutionUrl
    *   the institution URL resolved for the request
    * @return
    *   `true` if the request should be allowed to attempt authentication
    */
  private[auth] def isTrustedOrigin(
      origin: Option[String],
      secFetchSite: Option[String],
      institutionUrl: URI
  ): Boolean = {
    val institutionOrigin = WebOrigin.from(institutionUrl)

    def matchesInstitution(originValue: String): Boolean =
      institutionOrigin.isDefined && WebOrigin.parse(originValue) == institutionOrigin

    val originAbsentOrMatchesInstitution = origin.forall(matchesInstitution)
    val declaresCrossSite                = secFetchSite.exists(_.equalsIgnoreCase("cross-site"))

    originAbsentOrMatchesInstitution && !declaresCrossSite
  }
}

@Bind
@Singleton
@Api("Authentication")
@Path("auth")
class Auth @Inject() (
    userService: UserService,
    userSessionService: UserSessionService,
    auditLogService: AuditLogService,
    institutionService: InstitutionService
) {
  private val resourceHelper: PluginResourceHelper =
    ResourcesService.getResourceHelper(classOf[Auth])

  /** Provide simple username / password login as per a legacy oEQ form based authentication but for
    * use with REST APIs - possible the start of an authenticated Single Page App. This basically
    * mimics the existing form based login logic.
    *
    * @see
    *   com.tle.web.login.LogonSection#authenticate(SectionInfo)
    */
  @POST
  @Path("login")
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @ApiOperation(
    value = "Login as a normal user.",
    notes =
      "Provides a means to establish a simple cookie based (JSESSIONID) session, for easy use of the REST API for user based operations. Credentials are supplied as a JSON body. All authentication failures produce the same generic 401 response; requests with a cross-site Origin are rejected with 403.",
    response = classOf[String]
  )
  def login(@Context req: HttpServletRequest, credentials: LoginRequest): Response = {
    val wad = userService.getWebAuthenticationDetails(req)

    val outcome = for {
      _ <- validateOrigin(req)
      _ <- authenticate(credentials, wad)
    } yield ()

    outcome.fold(loginFailedResponse(_, credentials.username, wad), _ => Response.ok().build())
  }

  @PUT
  @Path("logout")
  @ApiOperation(
    value = "Logout the current session.",
    notes =
      "This is to logout sessions which were setup with the /api/auth/login endpoint, and will do so based on the JSESSIONID cookie."
  )
  def logout(@Context req: HttpServletRequest): Response = {
    userSessionService.reenableSessionUse()
    userService.logoutToGuest(userService.getWebAuthenticationDetails(req), false)
    Response.ok().build()
  }

  /** Applies the CSRF origin check (see `Auth.isTrustedOrigin`) to the given request. */
  private def validateOrigin(req: HttpServletRequest): Either[Auth.LoginFailure, Unit] =
    Either.cond(
      Auth.isTrustedOrigin(
        Option(req.getHeader("Origin")),
        Option(req.getHeader("Sec-Fetch-Site")),
        institutionService.getInstitutionUrl.toURI
      ),
      (),
      Auth.LoginFailure(Response.Status.FORBIDDEN, "logon.problems")
    )

  /** Attempts to establish a session for the given credentials. Every authentication failure (bad
    * credentials, expired or suspended account, ...) collapses to a single generic 401 so the
    * response does not disclose account state; non-authentication failures propagate to the
    * exception mapper as today.
    */
  private def authenticate(
      credentials: LoginRequest,
      wad: WebAuthenticationDetails
  ): Either[Auth.LoginFailure, Unit] = {
    userSessionService.reenableSessionUse()
    Try(userService.login(credentials.username, credentials.password, wad, true)) match {
      case Success(_)                          => Right(())
      case Failure(_: AuthenticationException) =>
        Left(Auth.LoginFailure(Response.Status.UNAUTHORIZED, "logon.invalid"))
      case Failure(other) => throw other
    }
  }

  /** Builds the response for a rejected login attempt, recording it in the audit log. */
  private def loginFailedResponse(
      failure: Auth.LoginFailure,
      username: String,
      wad: WebAuthenticationDetails
  ): Response = {
    auditLogService.logUserFailedAuthentication(username, wad)
    val message = CurrentLocale.get(resourceHelper.key(failure.messageKey))
    Response.status(failure.status).entity(message).build()
  }
}
