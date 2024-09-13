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

  /**
    * Retrieve an Identity Provider configuration from the OEQ standard configuration.
    *
    * @tparam T Type of the Identity Provider which must be a subtype of [[IdentityProvider]] and
    *           requires a [[Decoder]] for the subtype
    * @return Either the configuration or a message describing why failed to get the configuration
    */
  def get[T <: IdentityProvider: Decoder]: Either[String, T]

  /**
    * Validate and save an Identity Provider configuration.  If the validation fails, returns a message
    * listing all the invalid values. If the validation passes, save the string representation of the
    * configuration in the OEQ standard configuration.
    *
    * @param idp Configuration of an Identity Provider to be validated and then saved in the OEQ standard configuration
    * @tparam T Type of the Identity Provider which must be a subtype of [[IdentityProvider]] and
    *           requires a [[Encoder]] for the subtype
    * @return Either a message describing why failed to save or nothing if the save is successful
    */
  def save[T <: IdentityProvider: Encoder](idp: T): Either[String, Unit]
}
