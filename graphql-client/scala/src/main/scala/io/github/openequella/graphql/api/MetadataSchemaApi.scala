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
import cats.implicits._
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.views.conversions.MetadataSchemaConversions
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

import java.util.Base64

/** Provides access to the openEQUELLA metadata schema API.
  */
object MetadataSchemaApi extends NestedApi[MetadataSchemaQueries, MetadataSchemaMutations] {

  override protected def queryWrapper[A]
      : SelectionBuilder[MetadataSchemaQueries, A] => SelectionBuilder[RootQuery, A] =
    Queries.metadataSchema

  override protected def mutationWrapper[A]
      : SelectionBuilder[MetadataSchemaMutations, A] => SelectionBuilder[
        _root_.caliban.client.Operations.RootMutation,
        A
      ] = Mutations.metadataSchema

  /** Lists all metadata schemas available in the system.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of BaseEntityReferenceView representing the metadata schemas or a list of
    *   ApiError if the operation failed. If no schemas are found, an empty list is returned.
    */
  def listSchemas()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] = {
    val q = MetadataSchemaQueries.list {
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

  /** Exports a metadata schema as a ZIP file.
    *
    * @param id
    *   The ID of the metadata schema to export.
    * @param withSecurity
    *   Whether to include security information in the export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Array[Byte] containing the exported schema as a zip file.
    */
  def exportSchema(id: Long, withSecurity: Boolean)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] = {
    val q = MetadataSchemaQueries.export(id, withSecurity)

    // The query returns a base64 encoded string (representing a zip file), which we need to decode
    // into an Array[Byte]. Returning Array[Byte] removes the need for the client to be aware
    // of the base64 encoding and decoding process.
    query(q).map(_.map(base64ToBytes))
  }

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
    * @param force
    *   If true, forcefully unlocks the schema even if locked by another user. Defaults to None.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    */
  def cancelEdit(id: Long, force: Option[Boolean] = None)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Unit] = {
    val mutation = MetadataSchemaMutations.cancelEdit(id, force)

    flattenResult {
      mutate(mutation)
    }
  }

  /** Add a new metadata schema.
    *
    * Typically called after a `startCreate` operation, with the details for the new schema
    * populated. The `MetadataSchemaEditView` type is used as input to maintain consistency with the
    * view returned by `startEdit`, allowing the same type to be used throughout the edit lifecycle.
    *
    * @param details
    *   The metadata schema details to add, using the same view type returned by `startEdit`.
    * @param lockAfterwards
    *   If true, keeps the schema locked after creation for further editing.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or Unit if the operation was successful.
    */
  def add(details: MetadataSchemaEditView, lockAfterwards: Boolean)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], BaseEntityReferenceView] =
    for {
      input <- Either
        .catchNonFatal(MetadataSchemaConversions.toInput(details))
        .leftMap(e => List(UnknownError(s"Failed to convert details to input: ${e.getMessage}")))
      mutation = MetadataSchemaMutations.add(input, lockAfterwards) {
        BaseEntityReferenceView.selector
      }
      result <- flattenResult {
        mutate(mutation)
      }
    } yield result

  /** Delete a metadata schema.
    *
    * @param id
    *   The ID of the metadata schema to delete.
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
    val mutation = MetadataSchemaMutations.delete(id, checkReferences)

    flattenResult {
      mutate(mutation)
    }
  }

  private def base64ToBytes(base64Zip: String): Array[Byte] =
    Base64.getDecoder.decode(base64Zip)
}
