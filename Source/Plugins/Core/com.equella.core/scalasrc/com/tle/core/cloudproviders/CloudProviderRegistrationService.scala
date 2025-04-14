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

package com.tle.core.cloudproviders

import cats.data.{Validated, ValidatedNec}
import cats.implicits._
import sttp.client._
import sttp.client.circe._
import com.tle.beans.newentity.Entity
import com.tle.common.i18n.CurrentLocale
import com.tle.core.cloudproviders.CloudProviderHelper._
import com.tle.core.guice.Bind
import com.tle.core.newentity.service.EntityService
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.core.validation.{EntityStdEdits, EntityValidation}
import com.tle.legacy.LegacyGuice
import com.tle.web.DebugSettings
import io.circe.syntax._
import org.slf4j.LoggerFactory

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Bind
@Singleton
class CloudProviderRegistrationService {
  @Inject var entityService: EntityService = _

  def tokenCache: ReplicatedCacheService.ReplicatedCache[String] =
    LegacyGuice.replicatedCacheService.getCache[String]("cloudRegTokens", 100, 1, TimeUnit.HOURS)
  val FieldVendorId    = "vendorId"
  val RefreshServiceId = "refresh"
  val typeId           = "cloudprovider"
  val Logger           = LoggerFactory.getLogger(getClass)

  // Apply the provided Cloud provider details to the provided Entity.
  private def applyValues(
      entity: Entity,
      edits: EntityStdEdits,
      data: CloudProviderData
  ): Entity = {
    entity.data = data.asJson.noSpaces
    entity.modified = Instant.now
    entity.name = edits.name
    entity.nameStrings = edits.nameStrings.asJson.noSpaces
    entity.description = edits.description.orNull
    entity.descriptionStrings = edits.descriptionStrings.map(_.asJson.noSpaces).orNull

    entity
  }

  /** Check whether a registration token is valid. If yes, return the token. If no, return an
    * EntityValidation indicating the failure.
    *
    * @param regToken
    *   The token to be checked.
    */
  def validToken(regToken: String): ValidatedNec[EntityValidation, String] =
    if (tokenCache.get(regToken).isPresent)
      regToken.validNec
    else {
      EntityValidation("token", "invalid").invalidNec
    }

  /** Create a new token for Cloud provider registration.
    */
  def createRegistrationToken: String = {
    val newToken = UUID.randomUUID().toString
    tokenCache.put(newToken, newToken)
    newToken
  }

  /** Check whether a registration is valid. For example, check if the vendor ID is blank.
    *
    * @param reg
    *   A Cloud provider registration to be validated.
    * @return
    *   ValidatedNec where left is all the accumulated validation errors and right is the validated
    *   standard registration data.
    */
  def validateRegistrationFields(
      reg: CloudProviderRegistration
  ): ValidatedNec[EntityValidation, EntityStdEdits] = {
    EntityValidation.nonBlankString(FieldVendorId, reg.vendorId) *>
      EntityValidation.standardValidation(
        EntityStdEdits(name = reg.name, description = reg.description),
        CurrentLocale.getLocale
      )
  }

  /** Register a Cloud provider with provided token and registration information.
    *
    * @param regToken
    *   Token created for a specific registration.
    * @param registration
    *   Cloud provider registration data to be used to create a Cloud provider instance.
    * @return
    *   ValidatedNec where left is all the errors accumulated during the registration and right is
    *   the details of the registered Cloud provider.
    */
  def register(
      regToken: String,
      registration: CloudProviderRegistration
  ): ValidatedNec[EntityValidation, CloudProviderInstance] =
    (validToken(regToken), validateRegistrationFields(registration))
      .mapN((token, fields) => {
        val newData = CloudProviderData(
          baseUrl = registration.baseUrl,
          iconUrl = registration.iconUrl,
          vendorId = registration.vendorId,
          providerAuth = registration.providerAuth,
          oeqAuth = CloudOAuthCredentials.random(),
          serviceUrls = registration.serviceUrls,
          viewers = registration.viewers
        )
        val entity = applyValues(Entity.blankEntity(typeId), fields, newData)

        tokenCache.invalidate(token)         // Invalidate the token so it can't be reused.
        entityService.createOrUpdate(entity) // Persist the new Entity.
        buildCloudProviderInstance(
          entity,
          newData
        ) // Return the CloudProviderInstance representing the new Entity.
      })

  /** Update details of a Cloud provider.
    *
    * @param entity
    *   Entity representing a Cloud provider instance.
    * @param reg
    *   Cloud provider registration data to be used to update a Cloud provider instance.
    * @return
    *   ValidatedNec where left is all the errors accumulated during the update and right is the
    *   details of the updated Cloud provider.
    */
  def editRegistered(
      entity: Entity,
      reg: CloudProviderRegistration
  ): ValidatedNec[EntityValidation, CloudProviderInstance] =
    (extractData(entity.data), validateRegistrationFields(reg))
      .mapN((data, fields) => {
        val validatedData = CloudProviderData(
          baseUrl = reg.baseUrl,
          iconUrl = reg.iconUrl,
          vendorId = reg.vendorId,
          providerAuth = reg.providerAuth,
          oeqAuth = data.oeqAuth,
          serviceUrls = reg.serviceUrls,
          viewers = reg.viewers
        )
        applyValues(entity, fields, validatedData)

        entityService.createOrUpdate(entity)
        buildCloudProviderInstance(entity, validatedData)
      })

  /** Get all the registered Cloud providers.
    *
    * @return
    *   ValidatedNec where left is errors accumulated during the retrieval and right is a list of
    *   Cloud provider details.
    */
  def getAllProviders: ValidatedNec[EntityValidation, List[CloudProviderDetails]] = {
    entityService
      .getAllByType(typeId)
      .asScala
      .map { entity =>
        extractData(entity.data).map(data =>
          CloudProviderDetails(
            id = UUID.fromString(entity.id.uuid),
            name = entity.name,
            description = Option(entity.description),
            vendorId = data.vendorId,
            iconUrl = data.iconUrl,
            canRefresh = DebugSettings.isDevMode && data.serviceUrls.contains(RefreshServiceId)
          )
        )
      }
      .toList
      .sequence
  }

  /** Refresh and update a registered Cloud provider.
    *
    * @param entity
    *   Entity where the type must be 'cloudprovider'.
    * @return
    *   Validated where left is an error message and right is an option of CloudProviderInstance.
    */
  def refreshRegistration(
      entity: Entity
  ): Validated[List[String], Option[CloudProviderInstance]] = {
    def refresh(
        provider: CloudProviderInstance
    ): Validated[List[String], Option[CloudProviderInstance]] = {
      provider.serviceUrls
        .get(RefreshServiceId)
        .map(refreshService =>
          CloudProviderService
            .serviceRequest(
              refreshService,
              provider,
              Map.empty,
              uri =>
                basicRequest
                  .post(uri)
                  .body(CloudProviderRefreshRequest(provider.id))
                  .response(asJson[CloudProviderRegistration])
            )
            .map(
              _.body match {
                case Right(registration) =>
                  // Update with the new registration. If errors happen during the update, combine all errors into one string.
                  editRegistered(entity, registration).leftMap(EntityValidation.collectErrors)
                case Left(error) => List(error.getMessage).invalid
              }
            )
            .unsafeRunSync
        )
        .sequence
    }

    refresh(entity) // Entity is implicitly converted CloudProviderInstance here!
  }
}
