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

package com.tle.integration.oidc.service

import cats.data.Validated
import com.tle.integration.oidc.idp.IdentityProvider
import io.circe.{Decoder, Encoder}

trait OidcConfigurationService {

  final val PROPERTY_NAME = "OIDC_IDENTITY_PROVIDER"

  /**
    * Retrieve an Identity Provider configuration from the OEQ standard configuration.
    *
    * @tparam T Type of the Identity Provider which must be a subtype of [[IdentityProvider]] and
    *           requires a [[Decoder]] for the subtype
    * @return Either the configuration or a message describing why failed to get the configuration
    */
  def get[T <: IdentityProvider: Decoder]: Either[String, T]

  /**
    * Save the string representation of an Identity Provider configuration in the OEQ standard configuration.
    *
    * @param idp Configuration of an Identity Provider to be transformed to JSON string and then saved in the
    *            OEQ standard configuration
    * @tparam T Type of the Identity Provider which must be a subtype of [[IdentityProvider]] and
    *           requires a [[Encoder]] for the subtype
    */
  def save[T <: IdentityProvider: Encoder](idp: T): Unit

  /**
    * Validate an Identity Provider configuration. Both common fields and platform specific fields
    * will be validated.
    *
    * @param idp Configuration of an Identity Provider to be validated
    * @tparam T Type of the Identity Provider which must be a subtype of [[IdentityProvider]]
    * @return The Identity Provider itself if the validation succeeds, or a list of errors captured
    *         during the validation
    */
  def validate[T <: IdentityProvider](idp: T): Validated[List[String], T]
}
