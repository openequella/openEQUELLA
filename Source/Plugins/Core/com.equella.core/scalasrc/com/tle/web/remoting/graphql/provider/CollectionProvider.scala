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

package com.tle.web.remoting.graphql.provider

import com.tle.common.security.SecurityConstants
import com.tle.core.collection.service.ItemDefinitionService
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.guice.Bind
import com.tle.core.remoting.RemoteItemDefinitionService
import com.tle.core.security.impl.{RequiresPrivilege, SecureEntity}
import com.tle.web.remoting.graphql.schema.types.BaseEntityReference
import org.slf4j.LoggerFactory

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/** The provider for collections (Item Definitions) in the GraphQL API. Methods are secured with the
  * `RequiresPrivilege` annotation to ensure that only users with the appropriate privileges can
  * access them. Although most of the methods called in ItemDefinitionService are secured, not all
  * are so here we are explicit on every method.
  *
  * Note that the use of the `_VIRTUAL_BASE` privilege is a continuation of how the existing
  * BaseEntity services work. The value for this privilege is defined by @SecureEntity.
  *
  * @param itemDefinitionService
  *   the item definition service used to interact with collections.
  * @param stagingService
  *   the staging service for import/export operations.
  */
@Bind
@Singleton
@SecureEntity(RemoteItemDefinitionService.ENTITY_TYPE)
class CollectionProvider @Inject() (
    itemDefinitionService: ItemDefinitionService,
    stagingService: StagingService
) {
  private val LOGGER = LoggerFactory.getLogger(classOf[CollectionProvider])

  /** List all collections.
    *
    * @return
    *   a list of `BaseEntityReference` objects representing the collections.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def listCollections(): List[BaseEntityReference] = {
    LOGGER.debug("Listing all collections")
    itemDefinitionService.listEditable().asScala.map(BaseEntityReference(_)).toList
  }

  /** Export a collection as a base64 String representing the contents of a zip file. This can then
    * be decoded as a byte array and saved to a file. The resulting file can be imported into
    * another system.
    *
    * @param id
    *   the ID of the collection to export.
    * @param withSecurity
    *   whether to include security information in the export.
    * @return
    *   a base64 encoded string representing the exported zip file.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def exportCollection(id: Long, withSecurity: Boolean): Option[String] = {
    LOGGER.debug(s"Exporting collection with id $id")
    noneIfNotFound {
      itemDefinitionService.exportEntity(id, withSecurity)
    }.map(zipFile => Base64.getEncoder.encodeToString(zipFile))
  }

  /** Import a collection from a base64-encoded zip file. This is the inverse of
    * [[exportCollection]]. The entity is extracted into a staging area and prepared for editing but
    * is NOT yet persisted — the caller must follow up with [[stopEdit]] to complete the import, or
    * [[cancelEdit]] to discard it.
    *
    * @param zipBase64
    *   a base64-encoded string representing the zip file to import.
    * @return
    *   Either a ProviderError if the operation fails, or a BaseEntityReference for the imported
    *   collection.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def importCollection(zipBase64: String): Either[ProviderError, BaseEntityReference] = {
    LOGGER.debug("Importing collection from base64 zip")
    ProviderError.Try("Failed to import collection: ") {
      val zipBytes   = Base64.getDecoder.decode(zipBase64)
      val entityPack = itemDefinitionService.importEntity(zipBytes)
      val entity     = entityPack.getEntity
      BaseEntityReference(
        id = entity.getId,
        uuid = entity.getUuid,
        bundleId = entity.getName.getId,
        owner = entity.getOwner,
        forCollection = true
      )
    }
  }

  /** Get the collection ID for a given UUID.
    *
    * @param uuid
    *   the UUID of the collection.
    * @return
    *   the ID of the collection.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def collectionIdForUuid(uuid: String): Option[Long] = {
    LOGGER.debug(s"Getting collection ID for UUID $uuid")
    Option(itemDefinitionService.identifyByUuid(uuid)).filterNot(_ == 0L)
  }

  /** Clone a collection, creating a copy with a new ID.
    *
    * @param id
    *   the ID of the collection to clone.
    * @return
    *   Either a ProviderError if the operation fails, or a BaseEntityReference to the cloned
    *   collection on success.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def cloneCollection(id: Long): Either[ProviderError, BaseEntityReference] = {
    LOGGER.debug(s"Cloning collection with id $id")
    ProviderError.Try(s"Failed to clone collection with id $id: ") {
      BaseEntityReference(itemDefinitionService.clone(id))
    }
  }

  /** Delete a collection, with consideration to references controllable by the checkReferences
    * argument.
    *
    * @param id
    *   the ID of the collection to delete.
    * @param checkReferences
    *   if true, the deletion will only proceed if there are no references to the collection; if
    *   false, the collection will be deleted regardless of references (use with caution).
    * @return
    *   Either a ProviderError if the operation fails, or Unit on success.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def deleteCollection(id: Long, checkReferences: Boolean = true): Either[ProviderError, Unit] = {
    LOGGER.debug(s"Deleting collection with id $id, checkReferences: $checkReferences")
    ProviderError.Try(s"Failed to delete collection with id $id: ") {
      itemDefinitionService.delete(id, checkReferences)
    }
  }

  /** Cancel an editing session for a collection and remove its lock. This is typically used to
    * cancel an import operation that is in progress.
    *
    * @param id
    *   the ID of the collection being edited.
    * @param force
    *   if true, removes the lock regardless of which session owns it (forced unlock); if false,
    *   only removes the lock if the current session owns it.
    * @return
    *   Either a ProviderError if the operation fails, or Unit on success.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def cancelEdit(id: Long, force: Boolean = false): Either[ProviderError, Unit] = {
    LOGGER.debug(s"Cancelling edit of collection with id $id")
    ProviderError.Try(s"Failed to cancel edit of collection with id $id: ") {
      itemDefinitionService.cancelEdit(id, force)
    }
  }
}
