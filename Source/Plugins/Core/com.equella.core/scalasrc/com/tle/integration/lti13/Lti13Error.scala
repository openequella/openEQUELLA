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

package com.tle.integration.lti13

import com.tle.integration.oauth2.error.authorisation.AuthorisationError
import com.tle.integration.oauth2.error.general.GeneralError
import com.tle.integration.util.NO_FURTHER_INFO

/** Represent all the possible errors occurred during the LTI 1.3 integration, including all the
  * standard OAuth2 errors and LTI 1.3 specific errors.
  */
sealed abstract class Lti13Error {
  val msg: String
}

object Lti13Error {
  // A couple implicit functions to help transform GeneralError to Lti13Error.
  implicit def fromGeneralError(error: GeneralError): Lti13Error = new Lti13Error {
    override val msg: String = error.msg.getOrElse(NO_FURTHER_INFO)
  }
  implicit def fromEither[T](result: Either[GeneralError, T]): Either[Lti13Error, T] =
    result.left.map(fromGeneralError)
}

/** Typically used for an error related to an LTI 1.3 platform configuration.
  */
final case class PlatformDetailsError(msg: String) extends Lti13Error

/** Due to not having the feature of union types in Scala v2, this case class is created as a
  * wrapper of the standard OAuth2 errors to help reduce the complexity of error handling.
  */
final case class OAuth2LayerError(error: AuthorisationError) extends Lti13Error {
  val code: String         = error.code.toString
  override val msg: String = error.msg.getOrElse(NO_FURTHER_INFO)
}

object OAuth2LayerError {
  // A couple implicit functions to help transform AuthorisationError to OAuth2LayerError.
  implicit def fromOAuth2Error(error: AuthorisationError): OAuth2LayerError =
    OAuth2LayerError(error)
  implicit def fromEither[T](result: Either[AuthorisationError, T]): Either[OAuth2LayerError, T] =
    result.left.map(fromOAuth2Error)
}
