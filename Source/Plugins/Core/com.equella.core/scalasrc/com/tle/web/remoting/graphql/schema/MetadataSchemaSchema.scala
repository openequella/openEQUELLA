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
class MetadataSchemaSchema @Inject() (metadataSchemaProvider: MetadataSchemaProvider)
    extends SchemaProvider {

  override def getApi: GraphQL[Any] = graphQL(
    RootResolver(
      queries,
      mutations
    )
  )

  private val queries = Queries(
    metadataSchema = MetadataSchemaQueryOps(
      list = args => metadataSchemaProvider.listSchemas(args.includeSystem.getOrElse(false)),
      export = args => metadataSchemaProvider.exportSchema(args.id, args.withSecurity),
      idForUuid = uuid => metadataSchemaProvider.schemaIdForUuid(uuid),
      byId = args => metadataSchemaProvider.schemaById(args.id),
      uses = args => metadataSchemaProvider.getUses(args.id),
      importTypes = args => metadataSchemaProvider.getImportTypes(args.id),
      hasReferences = args => metadataSchemaProvider.hasReferences(args.id)
    )
  )

  private val mutations = Mutations(
    metadataSchema = MetadataSchemaMutationOps(
      startEdit = args => metadataSchemaProvider.startEdit(args.id),
      startCreate = () => metadataSchemaProvider.startCreate(),
      cancelEdit = args => metadataSchemaProvider.cancelEdit(args.id, args.force.getOrElse(false)),
      add = args => metadataSchemaProvider.add(args.details, args.lockAfterwards),
      stopEdit = args => metadataSchemaProvider.stopEdit(args.details, args.unlock),
      delete = args => metadataSchemaProvider.delete(args.id, args.checkReferences.getOrElse(true)),
      cloneSchema = args => metadataSchemaProvider.clone(args.id),
      importSchema = args => metadataSchemaProvider.importSchema(args.zipBase64)
    )
  )

  case class Queries(
      @GQLDescription("Queries for Metadata Schemas")
      metadataSchema: MetadataSchemaQueryOps
  )

  @GQLName("MetadataSchemaQueries")
  case class MetadataSchemaQueryOps(
      @GQLDescription("List all metadata schemas")
      list: SchemaListArgs => List[BaseEntityReference],
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

  case class SchemaListArgs(
      @GQLDescription(
        "Whether to include 'system type' metadata schemas such as the \"My Content\" schema backing the Scrapbook. Defaults to false if not provided."
      )
      includeSystem: Option[Boolean] = Some(false)
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
      cancelEdit: MetadataSchemaCancelEditArgs => ResultWithErrors[Unit],
      @GQLDescription(
        "Add a new metadata schema - typically after a startCreate operation, with details for the new schema."
      )
      add: MetadataSchemaAddArgs => ResultWithErrors[BaseEntityReference],
      @GQLDescription(
        "Stop editing a metadata schema - saving changes and optionally unlocking."
      )
      stopEdit: MetadataSchemaStopEditArgs => ResultWithErrors[MetadataSchema],
      @GQLDescription(
        "Delete a metadata schema - with consideration to references controllable by args."
      )
      delete: MetadataSchemaDeleteArgs => ResultWithErrors[Unit],
      @GQLName("clone")
      @GQLDescription(
        "Clone a metadata schema - creating a copy of the schema with a new ID."
      )
      cloneSchema: MetadataSchemaCloneArgs => ResultWithErrors[BaseEntityReference],
      @GQLName("import")
      @GQLDescription(
        "Import a metadata schema from a base64-encoded zip file. Returns an editable entity ready for stopEdit to complete the import."
      )
      importSchema: MetadataSchemaImportArgs => ResultWithErrors[EditableEntity[MetadataSchema]]
  )

  case class MetadataSchemaStartEditArgs(
      @GQLDescription("ID of the metadata schema to edit, or none to create a new one")
      id: Long
  )

  case class MetadataSchemaCancelEditArgs(
      @GQLDescription("ID of the metadata schema edit session to cancel")
      id: Long,
      @GQLDescription(
        "Whether to force cancel the edit session, if true, removes the lock regardless of which session owns it (forced unlock); if false, only removes the lock if the current session owns it. Defaults to false if not provided."
      )
      force: Option[Boolean] = None
  )

  case class MetadataSchemaDeleteArgs(
      @GQLDescription("ID of the metadata schema to delete")
      id: Long,
      @GQLDescription(
        "Whether to check for referencing entities before deletion, if true, the deletion will only proceed if there are no referencing entities; if false, will attempt to delete the schema regardless of references which may fail. Defaults to true if not provided."
      )
      checkReferences: Option[Boolean] = Some(true)
  )

  case class MetadataSchemaAddArgs(
      @GQLDescription("Details of the metadata schema to add")
      details: EditableEntity[MetadataSchema],
      @GQLDescription("Whether the newly added schema should be locked for editing after creation")
      lockAfterwards: Boolean
  )

  case class MetadataSchemaStopEditArgs(
      @GQLDescription("Details of the metadata schema to save")
      details: EditableEntity[MetadataSchema],
      @GQLDescription(
        "Whether to unlock the schema after saving, if false, keeps it locked for continued editing"
      )
      unlock: Boolean
  )

  case class MetadataSchemaCloneArgs(
      @GQLDescription("ID of the metadata schema to clone")
      id: Long
  )

  case class MetadataSchemaImportArgs(
      @GQLDescription("Base64-encoded zip file of the metadata schema to import")
      zipBase64: String
  )
}
