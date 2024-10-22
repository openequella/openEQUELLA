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

package com.tle.integration.oauth2

import com.tle.integration.oauth2.ErrorResponseCode.Code
import com.tle.integration.util.{getParam, getUriParam}

import java.net.URI
import scala.util.Try

/**
  * Valid error codes for Error Responses as per section 4.1.2.1 (Error Response) of the RFC 6749
  * (OAuth 2).
  */
object ErrorResponseCode extends Enumeration {
  type Code = Value

  val invalid_request, unauthorized_client, access_denied, unsupported_response_type, invalid_scope,
  server_error, temporarily_unavailable = Value
}

/**
  * An Error Responses as per section 4.1.2.1 (Error Response) of the RFC 6749 (OAuth 2).
  *
  * @param error             REQUIRED.  A single ASCII [USASCII] error code from [[ErrorResponseCode]]
  * @param error_description OPTIONAL.  Human-readable ASCII [USASCII] text providing additional
  *                          information, used to assist the client developer in understanding the
  *                          error that occurred. Values for the "error_description" parameter
  *                          MUST NOT include characters outside the set %x20-21 / %x23-5B / %x5D-7E.
  * @param error_uri         OPTIONAL.  A URI identifying a human-readable web page with information about
  *                          the error, used to provide the client developer with additional information
  *                          about the error. Values for the "error_uri" parameter MUST conform to the
  *                          URI-reference syntax and thus MUST NOT include characters outside the set
  *                          %x21 / %x23-5B / %x5D-7E.
  * @param state             REQUIRED if a "state" parameter was present in the client authorization request.
  *                          The exact value received from the client.
  */
case class ErrorResponse(error: ErrorResponseCode.Code,
                         error_description: Option[String],
                         error_uri: Option[URI],
                         state: String)

object ErrorResponse {
  val PARAM_ERRORCODE   = "error"
  val PARAM_DESCRIPTION = "error_description"
  val PARAM_URI         = "error_uri"
  val PARAM_STATE       = "state"

  def apply(params: Map[String, Array[String]]): Option[ErrorResponse] = {
    val param    = getParam(params)
    val uriParam = getUriParam(param)

    for {
      error <- param(PARAM_ERRORCODE).flatMap(asString =>
        Try(ErrorResponseCode.withName(asString)).toOption)
      error_description = param(PARAM_DESCRIPTION)
      error_uri         = uriParam(PARAM_URI)
      state <- param(PARAM_STATE)
    } yield ErrorResponse(error, error_description, error_uri, state)
  }
}

abstract class OAuth2Error {
  val code: Code
}

trait HasMessage {
  val msg: String
}

case class InvalidState(msg: String) extends OAuth2Error with HasMessage {
  override val code: Code = ErrorResponseCode.invalid_request
}

case class InvalidJWT(msg: String) extends OAuth2Error with HasMessage {
  override val code: Code = ErrorResponseCode.invalid_request
}

/**
  * Indicates a generic server error that can't be better classified with one of the other errors.
  */
case class ServerError(msg: String) extends OAuth2Error with HasMessage {
  override val code: Code = ErrorResponseCode.server_error
}

case class NotAuthorized(msg: String) extends OAuth2Error with HasMessage {
  override val code: Code = ErrorResponseCode.unauthorized_client
}

case class AccessDenied(msg: String) extends OAuth2Error with HasMessage {
  override val code: Code = ErrorResponseCode.access_denied
}
