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

package com.tle.admin

import org.slf4j.Logger
import sttp.client3.{HttpClientSyncBackend, Identity, Request, Response, SttpBackend}

import java.net.CookieHandler
import scala.util.{Failure, Success, Try}

/** The `rest` package contains the REST API client for the TLE Admin Console to utilise the
  * openEQUELLA REST APIs. Support is only implemented for the parts for the API that the Admin
  * Console requires.
  */
package object rest {

  // Shared backend configured with the system CookieHandler for automatic cookie management.
  // Uses java.net.http.HttpClient under the hood, which handles cookie storage and deduplication
  // via the system CookieManager (set up in Bootstrap.java).
  private lazy val sharedBackend: SttpBackend[Identity, Any] = {
    val httpClient = java.net.http.HttpClient
      .newBuilder()
      .cookieHandler(CookieHandler.getDefault)
      .build()
    HttpClientSyncBackend.usingClient(httpClient)
  }

  /** Sends a request. Cookies are managed automatically by the system CookieHandler configured on
    * the underlying HttpClient — no manual cookie attachment is needed.
    *
    * @param request
    *   the request to send
    * @tparam T
    *   the type of the response body
    * @return
    *   the response from the server
    */
  def send[T](
      request: Request[T, Any]
  ): Either[RestError, Response[T]] =
    Try(sharedBackend.send(request)) match {
      case Failure(exception) =>
        Left(ClientError(s"Failed to send request to server: ${exception.getMessage}", exception))
      case Success(response) =>
        Right(response)
    }

  /** Handles the result of a REST API call, logging the success or failure of the action.
    *
    * @param action
    *   the action that was attempted for prefixing log calls
    * @param result
    *   the result of the REST API call
    * @param onSuccess
    *   the function to call if the REST API call was successful
    * @param LOGGER
    *   the logger to use for logging - so that logging can align with caller
    * @return
    *   the result of the onSuccess function if the REST API call was successful, or the error that
    *   occurred if the REST API call was not successful
    */
  def handleResult[T, R](action: String, result: Either[RestError, Response[R]])(
      onSuccess: Response[R] => Either[RestError, T]
  )(implicit LOGGER: Logger): Either[RestError, T] = {
    result match {
      case Right(response) if response.isSuccess =>
        LOGGER.debug(s"$action successful")
        onSuccess(response)
      case Right(response) =>
        LOGGER.error(s"$action failed with status code ${response.code}")
        Left(StatusCodeError(s"Request for $action results in non-OK status code", response.code))
      case Left(error) =>
        LOGGER.error(s"$action failed with error: ${error.message}")
        Left(error)
    }
  }

  /** Extracts the action from the given request, providing a useful string in logs.
    *
    * Similar to Request.showBasic, but more suitable for our usage.
    */
  def extractAction(request: Request[_, Any]): String =
    s"[${request.method} - /${request.uri.path.mkString("/")}]"

  /** Handles the result of a REST API call whose response body is decoded into `T` (e.g. via
    * circe's `asJson` or sttp's `asByteArray`), unwrapping the decoded body and converting a
    * decoding failure into an `UnexpectedResponseError`.
    *
    * @param request
    *   the request to send
    * @param describeError
    *   renders the body-level error `E` as a message for logging and the returned error
    * @tparam E
    *   the type of the body-level error produced by the response handler
    * @tparam T
    *   the type of the decoded response body
    * @return
    *   the decoded body, or the error that occurred
    */
  def handleDecodedResult[E, T](
      request: Request[Either[E, T], Any]
  )(describeError: E => String)(implicit LOGGER: Logger): Either[RestError, T] =
    handleResult(extractAction(request), send(request)) { response =>
      response.body match {
        case Left(error) =>
          val message = describeError(error)
          LOGGER.error(s"Failed to decode response body: $message")
          Left(UnexpectedResponseError(message))
        case Right(value) => Right(value)
      }
    }

  /** Splits a file path into its non-empty segments — the single definition of path splitting
    * shared by URI building and tree rebuilding, so the two can never disagree.
    */
  def splitPath(path: String): List[String] =
    path.split('/').filter(_.nonEmpty).toList

}
