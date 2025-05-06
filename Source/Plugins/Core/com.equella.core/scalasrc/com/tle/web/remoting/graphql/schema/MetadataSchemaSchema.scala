package com.tle.web.remoting.graphql.schema

import caliban._
import caliban.schema.Annotations.GQLDescription
import caliban.schema.ArgBuilder.auto._
import caliban.schema.Schema.auto._
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.provider.MetadataSchemaProvider
import com.tle.web.remoting.graphql.schema.types.BaseEntityReference

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

  private val mutations = Mutations()

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

  case class Mutations()
}
