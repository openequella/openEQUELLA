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

package com.tle.admin.rest

import com.tle.exceptions.BadCredentialsException
import io.circe.generic.auto._
import org.slf4j.{Logger, LoggerFactory}
import sttp.client3.basicRequest
import sttp.client3.circe._
import sttp.model.{StatusCode, Uri}

import java.net.URL

/** JSON request body for `POST api/auth/login`. Mirrors the server-side
  * `com.tle.web.api.auth.LoginRequest`.
  */
private final case class LoginRequest(username: String, password: String)

/** The `AuthApi` object provides a client for the /api/auth endpoints of the openEQUELLA REST API.
  */
object AuthApi {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(AuthApi.getClass)
  private val API_PATH                = "auth"

  /** The URI of an /api/auth endpoint — e.g. `authEndpoint("login")`. */
  private def authEndpoint(action: String)(implicit cfg: RestConfiguration): Uri =
    cfg.apiUrl().addPath(API_PATH, action)

  /** Establishes a JSESSIONID based session for the given user. The session cookie is captured
    * automatically by the shared backend's cookie handler, so on success all subsequent REST (and
    * GraphQL) requests are authenticated.
    *
    * @param username
    *   the username to authenticate as
    * @param password
    *   the user's password
    */
  def login(username: String, password: String)(implicit
      cfg: RestConfiguration
  ): Either[RestError, Unit] = {
    val request = basicRequest
      .post(authEndpoint("login"))
      .body(LoginRequest(username, password))
    handleResult(extractAction(request), send(request)) { _ =>
      Right(())
    }
  }

  /** Java friendly variant of `login` for the pre-JPF bootstrap code, preserving the contract of
    * the legacy `SessionLogin.postLogin`: returns normally on success, and throws on any failure.
    *
    * @param endpointUrl
    *   the institution URL to log in against
    * @param username
    *   the username to authenticate as
    * @param password
    *   the user's password
    * @throws BadCredentialsException
    *   if the server rejects the credentials (HTTP 401)
    * @throws RuntimeException
    *   on any other failure
    */
  def login(endpointUrl: URL, username: String, password: String): Unit = {
    implicit val cfg: RestConfiguration = RestConfiguration(Uri(endpointUrl.toURI))
    login(username, password) match {
      case Right(_)                                          => ()
      case Left(StatusCodeError(_, StatusCode.Unauthorized)) =>
        throw new BadCredentialsException("Bad credentials")
      case Left(error) =>
        throw new RuntimeException(s"Failed to log in to $endpointUrl: ${error.message}")
    }
  }

  /** Terminates the session for the currently authenticated user.
    */
  def logout(implicit cfg: RestConfiguration): Either[RestError, Unit] = {
    val request = basicRequest.put(authEndpoint("logout"))
    handleResult(extractAction(request), send(request)) { _ =>
      Right(())
    }
  }
}
