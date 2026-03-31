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

import scala.annotation.tailrec

object TestHelper {
  val CREDENTIALS_AUTOTEST: (String, String) = ("AutoTest", "automated")
  val CREDENTIALS_ADMIN: (String, String)    = ("TLE_ADMINISTRATOR", "autotestpassword")
  val INSTITUTION_REST: String               = "rest"

  /** The port the test server is running on. Defaults to 8080 (CI), but can be overridden via the
    * system property `oeq.test.port` for local development (e.g. `-Doeq.test.port=9090`).
    */
  private val serverPort: Int =
    Option(System.getProperty("oeq.test.port")).map(_.toInt).getOrElse(8080)

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

  /** Test pagination using the given query function. Both forward and backward pagination are
    * tested in the one test. This is because they should both result in the same order of items. So
    * the result from one can be used to validate the other.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param totalExpectedItems
    *   the total number of items expected to be retrieved
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    */
  def testPagination[T](pageSize: Int, totalExpectedItems: Int)(
      queryFn: Pagination => Either[List[ApiError], PaginationResult[T]]
  ): Unit = {
    val itemsForward  = TestHelper.paginateForward(pageSize) { queryFn }
    val itemsBackward = TestHelper.paginateBackward(pageSize) { queryFn }

    itemsBackward shouldBe itemsForward
    itemsForward.size shouldBe totalExpectedItems
    itemsBackward.size shouldBe totalExpectedItems
  }

  /** Paginate through a list of items using forward pagination via the given query function.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    * @return
    *   the list of items retrieved
    */
  def paginateForward[T](
      pageSize: Int = Integer.MAX_VALUE
  )(queryFn: ForwardPagination => Either[List[ApiError], PaginationResult[T]]): List[T] = {
    @tailrec
    def retrieveItems(
        pagination: ForwardPagination,
        items: List[T] = List.empty
    ): List[T] = queryFn(pagination) match {
      case Left(errors)                              => fail(s"Failed to get items: $errors")
      case Right(result) if result.continue.nonEmpty =>
        retrieveItems(result.continue.get.asInstanceOf[ForwardPagination], items ++ result.items)
      case Right(result) => items ++ result.items
    }

    retrieveItems(ForwardPagination(pageSize))
  }

  /** Paginate through a list of items using backward pagination via the given query function.
    *
    * This pretty well identical to `paginateForward`, but with the way items are appended to the
    * list reversed. This is to ensure that the order of users is the same as the forward
    * pagination.
    *
    * There is value in having these two implementations stand-alone for reference purposes.
    *
    * @param pageSize
    *   the number of items to retrieve per page
    * @param queryFn
    *   the function to call to retrieve the next page of items
    * @tparam T
    *   the type of item to retrieve
    * @return
    *   the list of items retrieved
    */
  def paginateBackward[T](
      pageSize: Int = Integer.MAX_VALUE
  )(queryFn: BackwardPagination => Either[List[ApiError], PaginationResult[T]]): List[T] = {
    @tailrec
    def retrieveItems(
        pagination: BackwardPagination,
        items: List[T] = List.empty
    ): List[T] = queryFn(pagination) match {
      case Left(errors)                              => fail(s"Failed to get items: $errors")
      case Right(result) if result.continue.nonEmpty =>
        retrieveItems(result.continue.get.asInstanceOf[BackwardPagination], result.items ++ items)
      case Right(result) => result.items ++ items
    }

    retrieveItems(BackwardPagination(pageSize))
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
