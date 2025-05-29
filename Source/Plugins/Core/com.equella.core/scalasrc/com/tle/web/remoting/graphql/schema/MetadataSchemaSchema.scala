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
import caliban.schema.Annotations.GQLDescription
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
    metadataSchemas = () => schemaProvider.listSchemas(),
    metadataSchemaExport = args => schemaProvider.exportSchema(args.id, args.withSecurity),
    metadataSchemaIdForUuid = uuid => schemaProvider.schemaIdForUuid(uuid)
  )

  private val mutations = Mutations(
    metadataSchemaStartEdit = args => schemaProvider.startEdit(args.id),
    metadataSchemaStartCreate = () => schemaProvider.startCreate()
  )

  case class Queries(
      @GQLDescription("List all metadata schemas")
      metadataSchemas: () => List[BaseEntityReference],
      @GQLDescription("Export a metadata schema, returning a base64 encoded zip file")
      metadataSchemaExport: SchemaExportArgs => String,
      @GQLDescription("Get the metadata schema ID for a given UUID")
      metadataSchemaIdForUuid: String => Long
  )

  case class SchemaExportArgs(
      @GQLDescription("ID of the metadata schema to export")
      id: Long,
      @GQLDescription("Whether to include security information in the export")
      withSecurity: Boolean
  )

  case class Mutations(
      @GQLDescription(
        "Start editing an existing metadata schema. Expected that it will be followed by a metadataSchemaStopEdit or metadataSchemaCancelEdit operation."
      )
      metadataSchemaStartEdit: MetadataSchemaStartEditArgs => EditableEntity[MetadataSchema],
      @GQLDescription(
        "Start creating a new metadata schema. Expected that it will be followed by a metadataSchemaStopEdit or metadataSchemaCancelEdit operation."
      )
      metadataSchemaStartCreate: () => EditableEntitySkeleton
  )

  case class MetadataSchemaStartEditArgs(
      @GQLDescription("ID of the metadata schema to edit, or none to create a new one")
      id: Long
  )
}
