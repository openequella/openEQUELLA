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

package io.github.openequella.graphql.api.views

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.client.{MetadataSchema, MetadataSchemaTransform}

/** View model for a metadata schema, including details, transforms, and citations.
  *
  * @param details
  *   Entity details for the schema.
  * @param exportTransforms
  *   List of export transformation configurations.
  * @param importTransforms
  *   List of import transformation configurations.
  * @param itemNamePath
  *   Path to the name attribute in the schema.
  * @param itemDescriptionPath
  *   Path to the description attribute in the schema.
  * @param definition
  *   XML definition of the schema.
  * @param citations
  *   List of citations associated with the schema.
  */
final case class MetadataSchemaView(
    details: EntityDetailsView,
    exportTransforms: List[MetadataSchemaTransformView],
    importTransforms: List[MetadataSchemaTransformView],
    itemNamePath: String,
    itemDescriptionPath: String,
    definition: String,
    citations: List[CitationView]
)
object MetadataSchemaView {
  val selector: SelectionBuilder[MetadataSchema, MetadataSchemaView] =
    (
      MetadataSchema.details { EntityDetailsView.selector } ~
        MetadataSchema.exportTransforms { MetadataSchemaTransformView.selector } ~
        MetadataSchema.importTransforms { MetadataSchemaTransformView.selector } ~
        MetadataSchema.itemNamePath ~
        MetadataSchema.itemDescriptionPath ~
        MetadataSchema.definition ~
        MetadataSchema.citations(CitationView.selector)
    ).mapN(MetadataSchemaView.apply _)
}

/** Represents a metadata schema transformation, including the filename and schema type.
  *
  * @param filename
  *   Name of the transformation file.
  * @param schemaType
  *   Type of the schema transformation.
  */
final case class MetadataSchemaTransformView(filename: String, schemaType: String)
object MetadataSchemaTransformView {
  val selector: SelectionBuilder[MetadataSchemaTransform, MetadataSchemaTransformView] = (
    MetadataSchemaTransform.filename ~ MetadataSchemaTransform.schemaType
  ).mapN(MetadataSchemaTransformView.apply _)
}
