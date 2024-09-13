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

import cats.implicits._
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.guice.Bind
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.IdentityProvider
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}

@Singleton
@Bind
class OidcConfigurationServiceImpl extends OidcConfigurationService {
  private val PROPERTY_NAME                              = "OIDC_IDENTITY_PROVIDER"
  private var configurationService: ConfigurationService = _
  private var logger: Logger                             = LoggerFactory.getLogger(classOf[OidcConfigurationServiceImpl])

  @Inject
  def this(configurationService: ConfigurationService) {
    this()
    this.configurationService = configurationService
  }

  def save[T <: IdentityProvider: Encoder](idp: T): Either[String, Unit] = {
    def saveAsJson(validated: T): Either[String, Unit] =
      Either
        .catchNonFatal(configurationService.setProperty(PROPERTY_NAME, validated.asJson.noSpaces))
        .leftMap(_.getMessage)

    logger.info(s"Saving OIDC configuration ${idp.name} by user ${CurrentUser.getUserID} ")
    idp.validate.toEither.bimap(_.mkString_(","), saveAsJson).flatten
  }

  def get[T <: IdentityProvider: Decoder]: Either[String, T] = {
    Option(configurationService.getProperty(PROPERTY_NAME))
      .toRight(new Error("No Identity Provider configured"))
      .flatMap(parse)
      .flatMap(_.as[T])
      .leftMap(_.getMessage)
  }
}
