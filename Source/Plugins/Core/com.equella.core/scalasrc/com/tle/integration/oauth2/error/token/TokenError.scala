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

package com.tle.integration.oauth2.error.token

import com.tle.integration.oauth2.error.{HasCode, OAuth2Error}
import com.tle.integration.oauth2.error.token.TokenErrorResponseCode.Code
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

/**
  * Valid error codes for the error response of a token request as per section 5.2 (Error Response) of the OAuth2 spec.
  */
object TokenErrorResponseCode extends Enumeration {
  type Code = Value

  val invalid_request, invalid_client, invalid_grant, unauthorized_client, unsupported_grant_type,
  invalid_scope = Value

  implicit val tokenErrorCodeEncoder: Encoder[TokenErrorResponseCode.Value] =
    Encoder.encodeEnumeration(TokenErrorResponseCode)
  implicit val tokenErrorCodeDecoder: Decoder[TokenErrorResponseCode.Value] =
    Decoder.decodeEnumeration(TokenErrorResponseCode)
}

/**
  * Structure for the response of a failed ID token request as per section 5.2 (Error Response) of the OAuth2 spec.
  */
final case class TokenErrorResponse(error: TokenErrorResponseCode.Code,
                                    error_description: Option[String],
                                    error_uri: Option[String])
object TokenErrorResponse {
  implicit val tokenErrorResponseEncoder = deriveEncoder[TokenErrorResponse]
  implicit val tokenErrorResponseDecoder = deriveDecoder[TokenErrorResponse]
}

sealed abstract class TokenError(error: String) extends OAuth2Error with HasCode[Code] {
  val msg: Option[String] = Option(error)
}

final case class InvalidRequest(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.invalid_request
}

final case class InvalidClient(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.invalid_client
}

final case class InvalidGrant(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.invalid_grant
}

final case class UnauthorizedClient(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.unauthorized_client
}

final case class UnsupportedGrantType(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.unsupported_grant_type
}

final case class InvalidScope(error: String) extends TokenError(error) {
  override val code: Code = TokenErrorResponseCode.invalid_scope
}
