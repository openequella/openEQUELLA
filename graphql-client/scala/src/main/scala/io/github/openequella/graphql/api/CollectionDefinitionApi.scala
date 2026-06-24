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
import io.github.openequella.graphql.api.views.{
  BaseEntityReferenceView,
  CollectionDefinitionEditView,
  CollectionDefinitionView
}
import io.github.openequella.graphql.api.views.conversions.{
  CollectionConversions,
  withConvertedInput
}
import io.github.openequella.graphql.client.{
  CollectionMutations,
  CollectionQueries,
  Mutations,
  Queries
}

/** Provides access to the openEQUELLA collection definition API.
  */
object CollectionDefinitionApi extends ZipImportExportApi[CollectionQueries, CollectionMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[CollectionQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.collection

  override protected def mutationWrapper[A]
      : SelectionBuilder[CollectionMutations, A] => SelectionBuilder[
        _root_.caliban.client.Operations.RootMutation,
        A
      ] = Mutations.collection

  /** Lists all collections available in the system.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a list of BaseEntityReferenceView representing the collections.
    *   If no collections are found, an empty list is returned.
    */
  def listCollections()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] = {
    val q = CollectionQueries.list {
      BaseEntityReferenceView.selector
    }

    query(q)
  }

  /** Retrieves the ID of a collection by its UUID.
    *
    * @param uuid
    *   The UUID of the collection.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or an Option containing the ID of the collection if found, None if
    *   not found.
    */
  def getIdByUuid(uuid: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Long]] = {
    val q = CollectionQueries.idForUuid(uuid)

    query(q)
  }

  /** Start editing a collection by its ID.
    *
    * @param id
    *   The ID of the collection to start editing.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a CollectionDefinitionEditView representing the editable
    *   collection.
    */
  def startEdit(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], CollectionDefinitionEditView] = {
    val mutation = CollectionMutations.startEdit(id) {
      CollectionDefinitionEditView.selector
    }

    mutate(mutation)
  }

  /** Exports a collection as a ZIP file, without security information.
    *
    * @param id
    *   The ID of the collection to export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Option[Array[Byte]] containing the exported collection as a
    *   zip file.
    * @see
    *   [[exportCollectionWithSecurity]] to include security ACLs in the export.
    */
  def exportCollection(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] =
    exportZip(CollectionQueries.export(id, withSecurity = false))

  /** Exports a collection as a ZIP file, including security ACL information.
    *
    * @param id
    *   The ID of the collection to export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Option[Array[Byte]] containing the exported collection as a
    *   zip file.
    * @see
    *   [[exportCollection]] to export without security ACLs.
    */
  def exportCollectionWithSecurity(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] =
    exportZip(CollectionQueries.export(id, withSecurity = true))

  /** Imports a collection from a ZIP file.
    *
    * This is the inverse of [[exportCollection]]. The entity is prepared from the zip file but is
    * NOT yet persisted — the caller must follow up with [[stopEdit]] to complete the import, or
    * [[cancelEdit]] to discard it.
    *
    * @param zip
    *   The ZIP file bytes to import.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or a CollectionDefinitionEditView ready for stopEdit.
    */
  def importCollection(zip: Array[Byte])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], CollectionDefinitionEditView] =
    importZip(zip)(CollectionMutations.`import`(_) { CollectionDefinitionEditView.selector })

  /** Cancel editing a collection, discarding any changes and unlocking the collection.
    *
    * @param id
    *   The ID of the collection to cancel editing.
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
    flatMutate(CollectionMutations.cancelEdit(id, None))

  /** Cancel editing a collection and force-unlock it.
    *
    * Like [[cancelEdit]] but forcefully unlocks the collection even if it is locked by another
    * user. Use this when you need to recover a collection that is stuck locked by a disconnected
    * session.
    *
    * @param id
    *   The ID of the collection to cancel editing.
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
    flatMutate(CollectionMutations.cancelEdit(id, Some(true)))

  /** Stop editing a collection, saving changes and keeping it locked.
    *
    * Use this when you want to persist changes but intend to continue editing. To save and release
    * the lock in one step, use [[stopEditAndUnlock]] instead.
    *
    * @param details
    *   The collection details to save, using the view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a CollectionDefinitionView containing the saved collection.
    * @see
    *   [[stopEditAndUnlock]] to save and release the lock in one step.
    */
  def stopEdit(details: CollectionDefinitionEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], CollectionDefinitionView] =
    withConvertedInput(details, CollectionConversions.toInput) { input =>
      flatMutate(CollectionMutations.stopEdit(input, unlock = false) {
        CollectionDefinitionView.selector
      })
    }

  /** Stop editing a collection, saving changes and releasing the lock.
    *
    * The standard way to finish an edit session. Use [[stopEdit]] instead if you want to save but
    * continue editing under the same lock.
    *
    * @param details
    *   The collection details to save, using the view type returned by `startEdit`.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a CollectionDefinitionView containing the saved collection.
    * @see
    *   [[stopEdit]] to save while keeping the lock.
    */
  def stopEditAndUnlock(details: CollectionDefinitionEditView)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], CollectionDefinitionView] =
    withConvertedInput(details, CollectionConversions.toInput) { input =>
      flatMutate(CollectionMutations.stopEdit(input, unlock = true) {
        CollectionDefinitionView.selector
      })
    }

  /** Delete a collection.
    *
    * @param id
    *   The ID of the collection to delete.
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
    flatMutate(CollectionMutations.delete(id, None))

  /** Delete a collection only if no references exist.
    *
    * Like [[delete]] but checks for references to the collection before deleting and fails if any
    * exist. Use this to prevent accidental deletion of collections that other items depend on.
    *
    * @param id
    *   The ID of the collection to delete.
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
    flatMutate(CollectionMutations.delete(id, Some(true)))

  /** Clones a collection, creating a copy with a new ID. The cloned collection's name will be
    * prefixed with "Copy of " in all language variants.
    *
    * @param id
    *   The ID of the collection to clone.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or a BaseEntityReferenceView for the newly created clone.
    */
  def clone(id: Long)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntityReferenceView] =
    flatMutate(
      CollectionMutations.clone$(id) {
        BaseEntityReferenceView.selector
      }
    )

}
