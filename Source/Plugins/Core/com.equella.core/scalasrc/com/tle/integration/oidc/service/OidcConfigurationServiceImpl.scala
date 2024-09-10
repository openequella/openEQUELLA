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
import cats.implicits._
import com.tle.core.guice.Bind
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.IdentityProvider.validateCommonFields
import com.tle.integration.oidc.idp.{GenericIdentityProvider, IdentityProvider}
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import javax.inject.{Inject, Singleton}

@Singleton
@Bind
class OidcConfigurationServiceImpl extends OidcConfigurationService {
  private var configurationService: ConfigurationService = _

  @Inject
  def this(configurationService: ConfigurationService) {
    this()
    this.configurationService = configurationService
  }

  def save[T <: IdentityProvider: Encoder](idp: T): Unit =
    configurationService.setProperty(PROPERTY_NAME, idp.asJson.noSpaces)

  def get[T <: IdentityProvider: Decoder]: Either[String, T] = {
    Option(configurationService.getProperty(PROPERTY_NAME))
      .toRight(new Error("No Identity Provider configured"))
      .flatMap(parse)
      .flatMap(_.as[T])
      .leftMap(err => s"Failed to get Identity Provider: ${err.getMessage}")
  }

  def validate[T <: IdentityProvider](idp: T): Validated[List[String], T] = {
    def validateSpecialFields =
      idp match {
        case p: GenericIdentityProvider => GenericIdentityProvider.validate(p)
        case _                          => Validated.invalidNel("Unknown Identity Provider")
      }

    (validateCommonFields(idp), validateSpecialFields)
      .mapN((_, _) => idp)
      .leftMap(_.toList)
  }
}
