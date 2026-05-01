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

package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.schema.Annotations.{GQLDescription, GQLName}
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.CollectionProvider
import com.tle.web.remoting.graphql.schema.types.{
  BaseEntityReference,
  CollectionDefinition,
  EditableEntity
}

import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class CollectionSchema @Inject() (collectionProvider: CollectionProvider) extends SchemaProvider {

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries,
      mutations
    )
  )

  private val queries = Queries(
    collection = CollectionQueryOps(
      list = () => collectionProvider.listCollections(),
      export = args => collectionProvider.exportCollection(args.id, args.withSecurity),
      idForUuid = uuid => collectionProvider.collectionIdForUuid(uuid)
    )
  )

  private val mutations = Mutations(
    collection = CollectionMutationOps(
      startEdit = args => collectionProvider.startEdit(args.id),
      cloneCollection = args => collectionProvider.cloneCollection(args.id),
      delete =
        args => collectionProvider.deleteCollection(args.id, args.checkReferences.getOrElse(true)),
      importCollection = args => collectionProvider.importCollection(args.zipBase64),
      cancelEdit = args => collectionProvider.cancelEdit(args.id, args.force.getOrElse(false))
    )
  )

  case class Queries(
      @GQLDescription("Queries for Collections")
      collection: CollectionQueryOps
  )

  @GQLName("CollectionQueries")
  case class CollectionQueryOps(
      @GQLDescription("List all collections")
      list: () => List[BaseEntityReference],
      @GQLDescription("Export a collection, returning a base64 encoded zip file")
      export: CollectionExportArgs => Option[String],
      @GQLDescription("Get the collection ID for a given UUID")
      idForUuid: String => Option[Long]
  )

  case class CollectionExportArgs(
      @GQLDescription("ID of the collection to export")
      id: Long,
      @GQLDescription("Whether to include security information in the export")
      withSecurity: Boolean
  )

  case class Mutations(
      @GQLDescription("Operations for managing Collections")
      collection: CollectionMutationOps
  )

  @GQLName("CollectionMutations")
  case class CollectionMutationOps(
      @GQLDescription(
        "Start editing an existing collection. Expected to be followed by a stopEdit or cancelEdit operation."
      )
      startEdit: CollectionStartEditArgs => EditableEntity[CollectionDefinition],
      @GQLName("clone")
      @GQLDescription(
        "Clone a collection - creating a copy of the collection with a new ID."
      )
      cloneCollection: CollectionCloneArgs => ResultWithErrors[BaseEntityReference],
      @GQLDescription(
        "Delete a collection - with consideration to references controllable by args."
      )
      delete: CollectionDeleteArgs => ResultWithErrors[Unit],
      @GQLName("import")
      @GQLDescription(
        "Import a collection from a base64-encoded zip file."
      )
      importCollection: CollectionImportArgs => ResultWithErrors[
        EditableEntity[CollectionDefinition]
      ],
      @GQLDescription(
        "Cancel editing a collection - discarding any changes made and unlocking the collection."
      )
      cancelEdit: CollectionCancelEditArgs => ResultWithErrors[Unit]
  )

  case class CollectionStartEditArgs(
      @GQLDescription("ID of the collection to edit")
      id: Long
  )

  case class CollectionCloneArgs(
      @GQLDescription("ID of the collection to clone")
      id: Long
  )

  case class CollectionDeleteArgs(
      @GQLDescription("ID of the collection to delete")
      id: Long,
      @GQLDescription(
        "Whether to check for referencing entities before deletion, if true, the deletion will only proceed if there are no referencing entities; if false, will attempt to delete the collection regardless of references which may fail. Defaults to true if not provided."
      )
      checkReferences: Option[Boolean] = Some(true)
  )

  case class CollectionImportArgs(
      @GQLDescription("Base64-encoded zip file of the collection to import")
      zipBase64: String
  )

  case class CollectionCancelEditArgs(
      @GQLDescription("ID of the collection edit session to cancel")
      id: Long,
      @GQLDescription(
        "Whether to force cancel the edit session, if true, removes the lock regardless of which session owns it (forced unlock); if false, only removes the lock if the current session owns it. Defaults to false if not provided."
      )
      force: Option[Boolean] = None
  )
}
