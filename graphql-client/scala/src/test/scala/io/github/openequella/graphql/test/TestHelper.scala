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

package io.github.openequella.graphql.test

import io.github.openequella.graphql.api._
import io.github.openequella.graphql.{Client, ClientConfiguration}
import org.scalatest.Assertions.fail
import org.scalatest.matchers.must.Matchers.have
import org.scalatest.matchers.should.Matchers.{a, convertToAnyShouldWrapper}
import org.scalatest.prop.Tables.Table
import sttp.model.Uri

import java.util.Properties
import scala.util.{Failure, Success, Try, Using}

object TestHelper {
  val CREDENTIALS_AUTOTEST: (String, String) = ("AutoTest", "automated")
  val CREDENTIALS_ADMIN: (String, String)    = ("TLE_ADMINISTRATOR", "autotestpassword")
  val INSTITUTION_REST: String               = "rest"

  /** An entity ID guaranteed not to exist, for exercising not-found behaviour. */
  val INVALID_ENTITY_ID: Long = -1L

  /** An entity UUID guaranteed not to match any entity, for exercising not-found behaviour. */
  val INVALID_ENTITY_UUID: String = "invalid-uuid"

  /** Load the test server port from configuration with CLI -D override support.
    *
    * Precedence order:
    *   1. Default value: 8080
    *   2. Value from test.properties (loaded from classpath if present)
    *   3. CLI -D system property (highest priority)
    *
    * Users can copy test.properties.sample to test.properties and customize local settings.
    * test.properties is gitignored — do not commit it.
    */
  private def loadServerPort(): Int =
    cfgOption("oeq.test.port")
      .map { portStr =>
        Try(portStr.toInt) match {
          case Success(port) => port
          case Failure(_)    =>
            throw new IllegalArgumentException(
              s"Invalid oeq.test.port value '$portStr': must be a number. Check test.properties or -Doeq.test.port."
            )
        }
      }
      .getOrElse(8080)

  private def cfgOption(configKey: String): Option[String] =
    Option(System.getProperty(configKey)).orElse(loadFromPropertiesFile(configKey))

  private def loadFromPropertiesFile(propertyKey: String): Option[String] =
    Option(getClass.getClassLoader.getResourceAsStream("test.properties"))
      .flatMap { inputStream =>
        Using(inputStream) { stream =>
          val props = new Properties()
          props.load(stream)

          Option(props.getProperty(propertyKey))
        }.toOption.flatten
      }

  /** The port the test server is running on.
    *
    * Priority order:
    *   1. Default: 8080 (for CI)
    *   2. Value from test.properties file
    *   3. CLI -D option (e.g., `-Doeq.test.port=9090`)
    */
  private val serverPort: Int = loadServerPort()

  /** Login to the REST institution with the automated test user.
    *
    * @return
    *   the client configuration if successful, or fail the test with an error message if not
    */
  def loginToRestInstitution(): ClientConfiguration =
    loginToInstitution(INSTITUTION_REST, CREDENTIALS_AUTOTEST)

  /** Login to the specified institution with the given credentials.
    *
    * @param institution
    *   the institution to log in to, which will be used to build an institution URL of the form
    *   http://localhost:8080/institution
    * @param credentials
    *   the credentials to use (typically one of the constants defined in this object)
    * @return
    *   the client configuration if successful, or fail the test with an error message if not
    */
  def loginToInstitution(
      institution: String,
      credentials: (String, String)
  ): ClientConfiguration = {
    val instUrl                           = Uri("localhost").port(serverPort).withPath(institution)
    implicit val cfg: ClientConfiguration =
      ClientConfiguration(instUrl, new java.net.CookieManager())
    login(credentials)
  }

  /** Login to the same institution as the given client configuration, but with different
    * credentials.
    *
    * @param credentials
    *   the credentials to use (typically one of the constants defined in this object)
    * @param cfg
    *   the client configuration to use for the institution URL
    * @return
    *   the new client configuration if successful, or fail the test with an error message if not
    */
  def loginSameInstitutionWithDifferentUser(credentials: (String, String))(implicit
      cfg: ClientConfiguration
  ): ClientConfiguration = {
    // Create a fresh CookieManager so this user gets its own session.
    val newCfg: ClientConfiguration =
      ClientConfiguration(cfg.institutionUrl, new java.net.CookieManager())
    login(credentials)(newCfg)
  }

  /** A wrapper around the standard Client.login specifically for testing. It will return the client
    * configuration if the login is successful, or fail the test with an error message if not.
    */
  private def login(credentials: (String, String))(implicit cfg: ClientConfiguration) =
    Client.login(credentials._1, credentials._2) match {
      case Right(_)  => cfg
      case Left(err) => fail(err._2)
    }

  /** Run the given action with an unauthenticated client configuration. This is useful for testing
    * access control.
    *
    * @param action
    *   the action to run with the unauthenticated client configuration
    * @tparam T
    *   the return type of the action
    * @return
    *   the result of the action
    */
  def asUnauthenticatedUser[T](
      action: ClientConfiguration => T
  )(implicit cfg: ClientConfiguration): T = {
    // Create a fresh CookieManager so this config has no session cookies.
    val unAuthenticatedCfg: ClientConfiguration =
      ClientConfiguration(cfg.institutionUrl, new java.net.CookieManager())
    action(unAuthenticatedCfg)
  }

  /** Assert that an API call is rejected for unauthenticated users.
    *
    * @param apiCall
    *   the API call to run with an unauthenticated client configuration
    */
  def assertAccessDeniedError[T](
      apiCall: ClientConfiguration => Either[List[ApiError], T]
  )(implicit cfg: ClientConfiguration): Unit = {
    val response = asUnauthenticatedUser(apiCall)
    checkApiError(response) shouldBe a[AccessDeniedError]
  }

  /** Assert that an API response failed because the requested resource was not found.
    *
    * @param response
    *   the API response to check
    */
  def assertNotFoundError(response: Either[List[ApiError], _]): Unit =
    checkApiError(response) shouldBe a[NotFoundError]

  /** Assert that an API response failed because the request was invalid (e.g. malformed content).
    *
    * @param response
    *   the API response to check
    */
  def assertBadRequestError(response: Either[List[ApiError], _]): Unit =
    checkApiError(response) shouldBe a[BadRequestError]

  /** Run the given action with a client configuration that is logged in with different credentials
    * to the given client configuration. This is useful for testing access control and multi-user
    * scenarios.
    *
    * @param credentials
    *   the credentials to use for the new client configuration (typically one of the constants
    *   defined in this object)
    * @param action
    *   the action to run with the new client configuration
    * @tparam T
    *   the return type of the action
    * @return
    *   the result of the action
    */
  def withUser[T](
      credentials: (String, String)
  )(action: ClientConfiguration => T)(implicit cfg: ClientConfiguration): T = {
    val otherSession = loginSameInstitutionWithDifferentUser(credentials)
    action(otherSession)
  }

  /** Check that the response is an error response, and return the error. This can then be used to
    * check the specific error type.
    *
    * @param response
    *   the response to check
    * @return
    *   the error if available, otherwise `fail()`
    */
  def checkApiError(response: Either[List[ApiError], _]): ApiError = {
    response shouldBe a[Left[List[ApiError], _]]
    response.swap.foreach { errors =>
      errors should have length 1
    }
    response match {
      case Left(errors) => errors.head
      case Right(_)     => fail("Expected an error response")
    }
  }

  /** Special characters to test in queries.
    */
  val specialCharacters = Table(
    "char",
    "!",
    "@",
    "#",
    "$",
    "%",
    "^",
    "&",
    "*",
    "(",
    ")",
    "-",
    "_",
    "=",
    "+",
    "[",
    "]",
    "{",
    "}",
    "|",
    "\\",
    ":",
    ";",
    "\"",
    "'",
    "<",
    ">",
    ",",
    ".",
    "?",
    "/"
  )
}
