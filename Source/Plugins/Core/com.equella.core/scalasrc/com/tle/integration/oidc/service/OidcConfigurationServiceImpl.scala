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
import com.tle.common.beans.exception.NotFoundException
import com.tle.core.auditlog.AuditLogService
import com.tle.core.encryption.EncryptionService
import com.tle.core.guice.Bind
import com.tle.core.services.user.UserService
import com.tle.core.settings.service.ConfigurationService
import com.tle.integration.oidc.idp.{
  CommonDetails,
  GenericIdentityProviderDetails,
  IdentityProvider,
  IdentityProviderDetails
}
import io.circe.parser._
import io.circe.syntax._
import com.tle.integration.oidc.idp.IdentityProviderCodec._
import javax.inject.{Inject, Singleton}

@Singleton
@Bind(classOf[OidcConfigurationService])
class OidcConfigurationServiceImpl @Inject()(
    configurationService: ConfigurationService,
    auditLogService: AuditLogService,
    userService: UserService)(implicit val encryptionService: EncryptionService)
    extends OidcConfigurationService {
  private val PROPERTY_NAME = "OIDC_IDENTITY_PROVIDER"

  def save(idp: IdentityProvider): Either[Throwable, Unit] = {
    def saveAsJson(validated: IdentityProvider): Either[Throwable, Unit] =
      for {
        details <- IdentityProviderDetails(validated, get.toOption)
        jsonRepr = details.asJson.noSpaces
        _ <- Either.catchNonFatal {
          auditLogService.logGeneric("OIDC", "update IdP", null, null, null, jsonRepr)
          configurationService.setProperty(PROPERTY_NAME, jsonRepr)
          userService.refreshSettings()
        }
      } yield ()

    idp.validate.toEither
      .bimap(
        errors => new IllegalArgumentException(errors.mkString_(",")),
        saveAsJson
      )
      .flatten
  }

  def get: Either[Throwable, IdentityProviderDetails] = {
    def decryptCommonDetails(commonDetails: CommonDetails) = {
      val decryptedSecret = encryptionService.decrypt(commonDetails.authCodeClientSecret)
      commonDetails.copy(authCodeClientSecret = decryptedSecret)
    }

    Option(configurationService.getProperty(PROPERTY_NAME))
      .toRight(new NotFoundException("No Identity Provider configured"))
      .flatMap(parse)
      .flatMap(_.as[IdentityProviderDetails])
      .map {
        case GenericIdentityProviderDetails(commonDetails, apiUrl, apiClientId, apiClientSecret) =>
          GenericIdentityProviderDetails(
            commonDetails = decryptCommonDetails(commonDetails),
            apiUrl = apiUrl,
            apiClientId = apiClientId,
            apiClientSecret = encryptionService.decrypt(apiClientSecret),
          )
        case other => other
      }
  }
}
