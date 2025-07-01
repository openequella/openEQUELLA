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
import com.tle.common.usermanagement.user.CurrentUser
import com.tle.core.filesystem.staging.service.StagingService
import com.tle.core.guice.Bind
import com.tle.core.schema.service.SchemaService
import com.tle.core.security.impl.{RequiresPrivilege, SecureEntity}
import com.tle.web.remoting.graphql.schema.types._
import org.slf4j.LoggerFactory

import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

/** The provider for metadata schemas in the GraphQL API. Methods are secured with the
  * `RequiresPrivilege` annotation to ensure that only users with the appropriate privileges can
  * access them. Although most of the methods called in SchemaService are secured, not all are so
  * here were are explicit on every method.
  *
  * Note that the use of the `_VIRTUAL_BASE` privilege is a continuation of how the existing
  * BaseEntity services work. The value for this privilege is defined by @SecureEntity.
  *
  * @param schemaService
  *   the schema service used to interact with metadata schemas.
  */
@Bind
@Singleton
@SecureEntity(SchemaService.ENTITY_TYPE)
class MetadataSchemaProvider @Inject() (
    schemaService: SchemaService,
    stagingService: StagingService
) {
  private val LOGGER = LoggerFactory.getLogger(classOf[MetadataSchemaProvider])

  /** List all metadata schemas.
    *
    * @return
    *   a list of `BaseEntityReference` objects representing the metadata schemas.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def listSchemas(): List[BaseEntityReference] = {
    LOGGER.debug("Listing all metadata schemas")
    schemaService.listEditable().asScala.map(BaseEntityReference(_)).toList
  }

  /** Export a metadata schema as a base64 String representing the contents of a zip file. This can
    * then be decoded as a byte array and saved to a file. The resulting file can be imported into
    * another system.
    *
    * @param id
    *   the ID of the metadata schema to export.
    * @param withSecurity
    *   whether to include security information in the export.
    * @return
    *   a base64 encoded string representing the exported zip file.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def exportSchema(id: Long, withSecurity: Boolean): Option[String] = {
    LOGGER.debug(s"Exporting metadata schema with id $id")
    noneIfNotFound {
      schemaService.exportEntity(id, withSecurity)
    }.map(zipFile => Base64.getEncoder.encodeToString(zipFile))
  }

  /** Get the metadata schema ID for a given UUID.
    *
    * @param uuid
    *   the UUID of the metadata schema.
    * @return
    *   the ID of the metadata schema.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def schemaIdForUuid(uuid: String): Option[Long] = {
    LOGGER.debug(s"Getting metadata schema ID for UUID $uuid")
    Option(schemaService.identifyByUuid(uuid)).filterNot(_ == 0L)
  }

  /** Start editing an existing metadata schema. This method returns an `EditableBaseEntity` that
    * contains the metadata schema and its associated staging area. It is expected that it will be
    * followed with a cancel or stop edit operation.
    *
    * @param id
    *   the ID of the metadata schema to edit.
    * @return
    *   an `EditableBaseEntity` containing the metadata schema and staging information.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def startEdit(id: Long): EditableEntity[MetadataSchema] = {
    LOGGER.debug(s"Editing metadata schema with id $id")
    EditableEntity(schemaService.startEdit(id), MetadataSchema.apply)
  }

  /** Start creating a new metadata schema. This method returns an `EditableBaseEntitySkeleton` that
    * contains the necessary information to start creating a new metadata schema. It is expected
    * that it will be followed by further stop or cancel edit operation.
    *
    * @return
    *   an `EditableBaseEntitySkeleton` ready for editing.
    */
  @RequiresPrivilege(priv = SecurityConstants.EDIT_VIRTUAL_BASE)
  def startCreate(): EditableEntitySkeleton = {
    LOGGER.debug("Creating new metadata schema, ready for editing")
    EditableEntitySkeleton(
      owner = CurrentUser.getUserID,
      stagingId = stagingService.createStagingArea().getUuid
    )
  }
}
