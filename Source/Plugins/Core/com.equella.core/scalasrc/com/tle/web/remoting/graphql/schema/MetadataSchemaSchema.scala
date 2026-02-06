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
import com.tle.web.remoting.graphql.provider.MetadataSchemaProvider
import com.tle.web.remoting.graphql.schema.types.{
  BaseEntityReference,
  EditableEntity,
  EditableEntitySkeleton,
  MetadataSchema
}

import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class MetadataSchemaSchema extends SchemaProvider {
  private var schemaProvider: MetadataSchemaProvider = _

  @Inject def this(metadataSchemaProvider: MetadataSchemaProvider) {
    this()
    this.schemaProvider = metadataSchemaProvider
  }

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries,
      mutations
    )
  )

  private val queries = Queries(
    metadataSchema = MetadataSchemaQueryOps(
      list = () => schemaProvider.listSchemas(),
      export = args => schemaProvider.exportSchema(args.id, args.withSecurity),
      idForUuid = uuid => schemaProvider.schemaIdForUuid(uuid),
      byId = args => schemaProvider.schemaById(args.id),
      uses = args => schemaProvider.getUses(args.id),
      importTypes = args => schemaProvider.getImportTypes(args.id),
      hasReferences = args => schemaProvider.hasReferences(args.id)
    )
  )

  private val mutations = Mutations(
    metadataSchema = MetadataSchemaMutationOps(
      startEdit = args => schemaProvider.startEdit(args.id),
      startCreate = () => schemaProvider.startCreate(),
      cancelEdit = args => schemaProvider.cancelEdit(args.id, args.force)
    )
  )

  case class Queries(
      @GQLDescription("Queries for Metadata Schemas")
      metadataSchema: MetadataSchemaQueryOps
  )

  @GQLName("MetadataSchemaQueries")
  case class MetadataSchemaQueryOps(
      @GQLDescription("List all metadata schemas")
      list: () => List[BaseEntityReference],
      @GQLDescription("Export a metadata schema, returning a base64 encoded zip file")
      export: SchemaExportArgs => Option[String],
      @GQLDescription("Get the metadata schema ID for a given UUID")
      idForUuid: String => Option[Long],
      @GQLDescription("Get a metadata schema by ID")
      byId: SchemaByIdArgs => Option[MetadataSchema],
      @GQLDescription("Get the uses of a metadata schema by ID")
      uses: SchemaByIdArgs => List[BaseEntityReference],
      @GQLDescription("Get the types of schema import transformations for a metadata schema by ID")
      importTypes: SchemaByIdArgs => List[String],
      @GQLDescription("Check if a metadata schema has an referencing entities")
      hasReferences: SchemaByIdArgs => Boolean
  )

  case class SchemaExportArgs(
      @GQLDescription("ID of the metadata schema to export")
      id: Long,
      @GQLDescription("Whether to include security information in the export")
      withSecurity: Boolean
  )

  case class SchemaByIdArgs(
      @GQLDescription("ID of the metadata schema")
      id: Long
  )

  case class Mutations(
      @GQLDescription("Operations for managing Metadata Schemas")
      metadataSchema: MetadataSchemaMutationOps
  )

  @GQLName("MetadataSchemaMutations")
  case class MetadataSchemaMutationOps(
      @GQLDescription(
        "Start editing an existing metadata schema. Expected that it will be followed by a stopEdit or cancelEdit operation."
      )
      startEdit: MetadataSchemaStartEditArgs => EditableEntity[MetadataSchema],
      @GQLDescription(
        "Start creating a new metadata schema. Typically followed by an add operation with details for new schema."
      )
      startCreate: () => EditableEntitySkeleton,
      @GQLDescription(
        "Cancel editing a metadata schema - discarding any changes made and unlocking the schema."
      )
      cancelEdit: MetadataSchemaCancelEditArgs => ResultWithErrors[Unit]
  )

  case class MetadataSchemaStartEditArgs(
      @GQLDescription("ID of the metadata schema to edit, or none to create a new one")
      id: Long
  )

  case class MetadataSchemaCancelEditArgs(
      @GQLDescription("ID of the metadata schema edit session to cancel")
      id: Long,
      @GQLDescription(
        "Whether to force cancel the edit session, if true, removes the lock regardless of which session owns it (forced unlock); if false, only removes the lock if the current session owns it."
      )
      force: Option[Boolean] = None
  )
}
