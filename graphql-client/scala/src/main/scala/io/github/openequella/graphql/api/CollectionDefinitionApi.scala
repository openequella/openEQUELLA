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
  CollectionDefinitionEditView
}
import io.github.openequella.graphql.client.{
  CollectionMutations,
  CollectionQueries,
  Mutations,
  Queries
}

import java.util.Base64

/** Provides access to the openEQUELLA collection definition API.
  */
object CollectionDefinitionApi extends NestedApi[CollectionQueries, CollectionMutations] {

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

  /** Exports a collection as a ZIP file.
    *
    * @param id
    *   The ID of the collection to export.
    * @param withSecurity
    *   Whether to include security information in the export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Option[Array[Byte]] containing the exported collection as a
    *   zip file.
    */
  @SuppressWarnings(Array("BooleanParameter"))
  def exportCollection(id: Long, withSecurity: Boolean)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] = {
    val q = CollectionQueries.export(id, withSecurity)

    // The query returns a base64 encoded string (representing a zip file), which we need to decode
    // into an Array[Byte]. Returning Array[Byte] removes the need for the client to be aware
    // of the base64 encoding and decoding process.
    query(q).map(_.map(base64ToBytes))
  }

  /** Imports a collection from a ZIP file.
    *
    * This is the inverse of [[exportCollection]]. The entity is prepared from the zip file but is
    * NOT yet persisted — no stopEdit operation is currently available for collections on the server
    * side.
    *
    * @param zip
    *   The ZIP file bytes to import.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or a CollectionDefinitionEditView.
    */
  def importCollection(zip: Array[Byte])(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], CollectionDefinitionEditView] = {
    val zipBase64 = Base64.getEncoder.encodeToString(zip)
    flattenResult {
      mutate(
        CollectionMutations.`import`(zipBase64) {
          CollectionDefinitionEditView.selector
        }
      )
    }
  }

  /** Cancel editing a collection, discarding any changes and unlocking the collection.
    *
    * @param id
    *   The ID of the collection to cancel editing.
    * @param force
    *   If true, forcefully unlocks the collection even if locked by another user. Defaults to None.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    */
  def cancelEdit(id: Long, force: Option[Boolean] = None)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] = {
    val mutation = CollectionMutations.cancelEdit(id, force)

    flattenResult {
      mutate(mutation)
    }
  }

  /** Delete a collection.
    *
    * @param id
    *   The ID of the collection to delete.
    * @param checkReferences
    *   If true, checks for references before deleting and fails if any exist. If false or None,
    *   deletes without checking references.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    */
  def delete(id: Long, checkReferences: Option[Boolean] = None)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] = {
    val mutation = CollectionMutations.delete(id, checkReferences)

    flattenResult {
      mutate(mutation)
    }
  }

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
    flattenResult {
      mutate(
        CollectionMutations.clone$(id) {
          BaseEntityReferenceView.selector
        }
      )
    }

  private def base64ToBytes(base64Zip: String): Array[Byte] =
    Base64.getDecoder.decode(base64Zip)
}
