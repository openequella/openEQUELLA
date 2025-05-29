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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription
import com.tle.beans.entity.SchemaTransform

import scala.jdk.CollectionConverters._

@GQLDescription(
  "A metadata schema, which defines the structure and transforms for metadata items in openEQUELLA."
)
final case class MetadataSchema(
    @GQLDescription("Details of the metadata schema, including UUID and owner")
    details: EntityDetails,
    @GQLDescription("Transforms applied when exporting this schema between repositories")
    exportTransforms: List[MetadataSchemaTransform],
    @GQLDescription("Transforms applied when importing this schema between repositories")
    importTransforms: List[MetadataSchemaTransform],
    @GQLDescription("Path to the name attribute in the schema")
    itemNamePath: String,
    @GQLDescription("Path to the description attribute in the schema")
    itemDescriptionPath: String,
    @GQLDescription("XML definition of the schema")
    definition: String,
    @GQLDescription("List of citations associated with this schema")
    citations: List[Citation]
)
object MetadataSchema {

  /** Converts a `com.tle.beans.entity.Schema` to a `MetadataSchema` ready for use in GraphQL land.
    *
    * @param entity
    *   The schema entity to convert.
    * @return
    *   A `MetadataSchema` representation of the entity.
    */
  def apply(entity: com.tle.beans.entity.Schema): MetadataSchema = MetadataSchema(
    details = EntityDetails(entity),
    exportTransforms = convertSchemaTransforms(entity.getExportTransforms),
    importTransforms = convertSchemaTransforms(entity.getImportTransforms),
    itemNamePath = entity.getItemNamePath,
    itemDescriptionPath = entity.getItemDescriptionPath,
    definition = entity.getSerialisedDefinition,
    citations = convertCitations(entity.getCitations)
  )

  private def convertSchemaTransforms(
      transforms: java.util.List[SchemaTransform]
  ): List[MetadataSchemaTransform] =
    transforms.asScala.toList.map { MetadataSchemaTransform(_) }

  private def convertCitations(
      citations: java.util.List[com.tle.beans.entity.schema.Citation]
  ): List[Citation] =
    citations.asScala.toList.map { Citation(_) }
}

@GQLDescription(
  "A schema transform that defines how metadata is transformed during export or import operations with external systems."
)
final case class MetadataSchemaTransform(
    @GQLDescription(
      "Filename of the schema transform stored on the server - managed via staging area"
    )
    filename: String,
    @GQLDescription("Type of the schema transform, e.g., OAI_Identity, HARVESTER, OAI_DC")
    schemaType: String
)
object MetadataSchemaTransform {
  def apply(transform: com.tle.beans.entity.SchemaTransform): MetadataSchemaTransform =
    MetadataSchemaTransform(
      filename = transform.getFilename,
      schemaType = transform.getType
    )
}
