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

package com.tle.integration.oauth2.error.general

import com.tle.integration.oauth2.error.OAuth2Error

/**
  * Represent any error other than the standard OAuth2 errors that can occur during the integration of OAuth2.
  */
sealed abstract class GeneralError(error: String) extends OAuth2Error {
  override val msg: Option[String] = Option(error)
}

/**
  * Typically used for an error related to the use or verification of a JWT.
  */
final case class InvalidJWT(error: String) extends GeneralError(error)

/**
  * Typically used for an error which cannot be better classified with one of the other errors.
  */
final case class ServerError(error: String) extends GeneralError(error)
