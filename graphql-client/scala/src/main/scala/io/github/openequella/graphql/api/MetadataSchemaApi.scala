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

package io.github.openequella.graphql.api

import caliban.client.Operations.RootQuery
import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.conversions.{
  MetadataSchemaConversions,
  withConvertedInput
}
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  EntitySkeletonView,
  MetadataSchemaEditView,
  MetadataSchemaView
}
import io.github.openequella.graphql.client.{
  MetadataSchemaMutations,
  MetadataSchemaQueries,
  Mutations,
  Queries
}

/** Provides access to the openEQUELLA metadata schema API.
  */
object MetadataSchemaApi
    extends ZipImportExportApi[MetadataSchemaQueries, MetadataSchemaMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[MetadataSchemaQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.metadataSchema

  override protected def mutationWrapper[A]
      : SelectionBuilder[MetadataSchemaMutations, A] => SelectionBuilder[
        _root_.caliban.client.Operations.RootMutation,
        A
      ] = Mutations.metadataSchema

  /** Lists all metadata schemas available in the system, excluding 'system type' schemas.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of BaseEntityReferenceView representing the metadata schemas or a list of
    *   ApiError if the operation failed. If no schemas are found, an empty list is returned.
    * @see
    *   [[listSchemasIncludingSystem]] to also include system schemas.
    */
  def listSchemas()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] =
    listSchemas(includeSystem = false)

  /** Lists all metadata schemas available in the system, including 'system type' schemas such as
    * the "My Content" schema backing the Scrapbook. Needed by callers assigning ACLs, such as the
    * Admin Console Security Manager.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of BaseEntityReferenceView representing the metadata schemas or a list of
    *   ApiError if the operation failed. If no schemas are found, an empty list is returned.
    * @see
    *   [[listSchemas]] to exclude system schemas.
    */
  def listSchemasIncludingSystem()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] =
    listSchemas(includeSystem = true)

  private def listSchemas(includeSystem: Boolean)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] = {
    val q = MetadataSchemaQueries.list(Some(includeSystem)) {
      BaseEntityReferenceView.selector
    }

    query(q)
  }

  /** Retrieves a metadata schema by its ID.
    *
    * @param id
    *   The ID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or an Option containing the MetadataSchemaView if found, None if
    *   not found.
    */
  def getById(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[MetadataSchemaView]] = {
    val q = MetadataSchemaQueries.byId(id) {
      MetadataSchemaView.selector
    }

    query(q)
  }

  /** Retrieves the ID of a metadata schema by its UUID.
    *
    * @param uuid
    *   The UUID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or an Option containing the ID of the metadata schema if found,
    *   None if not found.
    */
  def getIdByUuid(uuid: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Long]] = {
    val q = MetadataSchemaQueries.idForUuid(uuid)

    query(q)
  }

  /** Get the uses of a Metadata Schema
    *
    * @param id
    *   The ID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a list of BaseEntityReferenceView representing the uses of the
    *   metadata schema.
    */
  def getUses(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] = {
    val q = MetadataSchemaQueries.uses(id) {
      BaseEntityReferenceView.selector
    }

    query(q)
  }

  /** Checks if a metadata schema has any referencing entities.
    *
    * @param id
    *   The ID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a Boolean indicating whether the metadata schema has
    *   references.
    */
  def hasReferences(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Boolean] = {
    val q = MetadataSchemaQueries.hasReferences(id)
    query(q)
  }

  /** Retrieves the import schema types for a metadata schema.
    *
    * @param id
    *   The ID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a list of strings representing the import types for the
    *   metadata schema.
    */
  def getImportTypes(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[String]] = {
    val q = MetadataSchemaQueries.importTypes(id)
    query(q)
  }

  /** Exports a metadata schema as a ZIP file, without security information.
    *
    * @param id
    *   The ID of the metadata schema to export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Array[Byte] containing the exported schema as a zip file.
    * @see
    *   [[exportSchemaWithSecurity]] to include security ACLs in the export.
    */
  def exportSchema(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] =
    exportZip(MetadataSchemaQueries.export(id, withSecurity = false))

  /** Exports a metadata schema as a ZIP file, including security ACL information.
    *
    * @param id
    *   The ID of the metadata schema to export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Array[Byte] containing the exported schema as a zip file.
    * @see
    *   [[exportSchema]] to export without security ACLs.
    */
  def exportSchemaWithSecurity(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] =
    exportZip(MetadataSchemaQueries.export(id, withSecurity = true))

  /** Imports a metadata schema from a ZIP file.
    *
    * This is the inverse of [[exportSchema]]. The entity is prepared from the zip file but is NOT
    * yet persisted — the caller must follow up with [[stopEdit]] to complete the import, or
    * [[cancelEdit]] to discard it.
    *
    * @param zip
    *   The ZIP file bytes to import.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or a MetadataSchemaEditView ready for stopEdit.
    */
  def importSchema(zip: Array[Byte])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], MetadataSchemaEditView] =
    importZip(zip)(MetadataSchemaMutations.`import`(_) { MetadataSchemaEditView.selector })

  /** Start editing a metadata schema by its ID.
    *
    * @param id
    *   The ID of the metadata schema to start editing.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a MetadataSchemaEditView representing the editable schema.
    */
  def startEdit(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], MetadataSchemaEditView] = {
    val mutation = MetadataSchemaMutations.startEdit(id) {
      MetadataSchemaEditView.selector
    }

    mutate(mutation)
  }

  /** Start creating a new metadata schema.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or an EntitySkeletonView representing the skeleton for the new
    *   schema.
    */
  def startCreate()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], EntitySkeletonView] = {
    val mutation = MetadataSchemaMutations.startCreate {
      EntitySkeletonView.selector
    }

    mutate(mutation)
  }

  /** Cancel editing a metadata schema, discarding any changes and unlocking the schema.
    *
    * @param id
    *   The ID of the metadata schema to cancel editing.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    * @see
    *   [[cancelEditForced]] to force-unlock even if locked by another user.
    */
  def cancelEdit(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(MetadataSchemaMutations.cancelEdit(id, None))

  /** Cancel editing a metadata schema and force-unlock it.
    *
    * Like [[cancelEdit]] but forcefully unlocks the schema even if it is locked by another user.
    * Use this when you need to recover a schema that is stuck locked by a disconnected session.
    *
    * @param id
    *   The ID of the metadata schema to cancel editing.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    * @see
    *   [[cancelEdit]] to cancel normally without forcing the unlock.
    */
  def cancelEditForced(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(MetadataSchemaMutations.cancelEdit(id, Some(true)))

  /** Add a new metadata schema.
    *
    * Typically called after a `startCreate` operation, with the details for the new schema
    * populated. The schema is not locked after creation — use [[addAndLock]] to keep it locked.
    *
    * @param details
    *   The metadata schema details to add, using the same view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a BaseEntityReferenceView for the newly created schema.
    * @see
    *   [[addAndLock]] to keep the schema locked after creation.
    */
  def add(details: MetadataSchemaEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntityReferenceView] =
    withConvertedInput(details, MetadataSchemaConversions.toInput) { input =>
      flatMutate(MetadataSchemaMutations.add(input, lockAfterwards = false) {
        BaseEntityReferenceView.selector
      })
    }

  /** Add a new metadata schema, keeping it locked for further editing.
    *
    * Like [[add]] but keeps the schema locked after creation, allowing the caller to continue
    * editing without a separate lock acquisition step.
    *
    * @param details
    *   The metadata schema details to add, using the same view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a BaseEntityReferenceView for the newly created schema.
    * @see
    *   [[add]] to create without keeping it locked.
    */
  def addAndLock(details: MetadataSchemaEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntityReferenceView] =
    withConvertedInput(details, MetadataSchemaConversions.toInput) { input =>
      flatMutate(MetadataSchemaMutations.add(input, lockAfterwards = true) {
        BaseEntityReferenceView.selector
      })
    }

  /** Stop editing a metadata schema, saving changes and keeping it locked.
    *
    * Use this when you want to persist changes but intend to continue editing. To save and release
    * the lock in one step, use [[stopEditAndUnlock]] instead.
    *
    * @param details
    *   The metadata schema details to save, using the view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a MetadataSchemaView containing the saved schema.
    * @see
    *   [[stopEditAndUnlock]] to save and release the lock in one step.
    */
  def stopEdit(details: MetadataSchemaEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], MetadataSchemaView] =
    withConvertedInput(details, MetadataSchemaConversions.toInput) { input =>
      flatMutate(MetadataSchemaMutations.stopEdit(input, unlock = false) {
        MetadataSchemaView.selector
      })
    }

  /** Stop editing a metadata schema, saving changes and releasing the lock.
    *
    * The standard way to finish an edit session. Use [[stopEdit]] instead if you want to save but
    * continue editing under the same lock.
    *
    * @param details
    *   The metadata schema details to save, using the view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a MetadataSchemaView containing the saved schema.
    * @see
    *   [[stopEdit]] to save while keeping the lock.
    */
  def stopEditAndUnlock(details: MetadataSchemaEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], MetadataSchemaView] =
    withConvertedInput(details, MetadataSchemaConversions.toInput) { input =>
      flatMutate(MetadataSchemaMutations.stopEdit(input, unlock = true) {
        MetadataSchemaView.selector
      })
    }

  /** Delete a metadata schema.
    *
    * @param id
    *   The ID of the metadata schema to delete.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    * @see
    *   [[deleteWithReferenceCheck]] to check for references before deleting.
    */
  def delete(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(MetadataSchemaMutations.delete(id, None))

  /** Delete a metadata schema only if no references exist.
    *
    * Like [[delete]] but checks for references to the schema before deleting and fails if any
    * exist. Use this to prevent accidental deletion of schemas that other items depend on.
    *
    * @param id
    *   The ID of the metadata schema to delete.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful, or an error if references
    *   exist.
    * @see
    *   [[delete]] to delete without checking references.
    */
  def deleteWithReferenceCheck(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] =
    flatMutate(MetadataSchemaMutations.delete(id, Some(true)))

  /** Clones a metadata schema, creating a copy with a new ID. The cloned schema's name will be
    * prefixed with "Copy of " in all language variants.
    *
    * @param id
    *   The ID of the metadata schema to clone.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a BaseEntityReferenceView for the newly created clone.
    */
  def clone(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntityReferenceView] =
    flatMutate(
      MetadataSchemaMutations.clone$(id) {
        BaseEntityReferenceView.selector
      }
    )

}
