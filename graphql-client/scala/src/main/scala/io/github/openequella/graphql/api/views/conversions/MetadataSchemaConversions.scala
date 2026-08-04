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

package io.github.openequella.graphql.api.views.conversions

import io.github.openequella.graphql.api.views.{
  CitationView,
  MetadataSchemaEditView,
  MetadataSchemaTransformView,
  MetadataSchemaView
}
import io.github.openequella.graphql.client.{
  CitationInput,
  EditableEntityMetadataSchemaInput,
  MetadataSchemaInput,
  MetadataSchemaTransformInput
}
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl._

/** Provides Chimney transformers and conversion functions for metadata schema types.
  *
  * This object contains:
  *   - Implicit transformers for metadata schema specific types
  *   - Explicit conversion functions for use in the MetadataSchemaApi
  */
object MetadataSchemaConversions {

  /** Transformer for CitationView to CitationInput. */
  implicit val citationViewToInput: Transformer[CitationView, CitationInput] =
    Transformer.derive[CitationView, CitationInput]

  /** Transformer for MetadataSchemaTransformView to MetadataSchemaTransformInput. */
  implicit val metadataSchemaTransformViewToInput
      : Transformer[MetadataSchemaTransformView, MetadataSchemaTransformInput] =
    Transformer.derive[MetadataSchemaTransformView, MetadataSchemaTransformInput]

  /** Transformer for MetadataSchemaView to MetadataSchemaInput. */
  implicit val metadataSchemaViewToInput: Transformer[MetadataSchemaView, MetadataSchemaInput] =
    Transformer.derive[MetadataSchemaView, MetadataSchemaInput]

  /** Transformer for MetadataSchemaEditView to EditableEntityMetadataSchemaInput. */
  implicit val metadataSchemaEditViewToInput
      : Transformer[MetadataSchemaEditView, EditableEntityMetadataSchemaInput] =
    Transformer
      .define[MetadataSchemaEditView, EditableEntityMetadataSchemaInput]
      .withFieldRenamed(_.schema, _.entity)
      .buildTransformer

  /** Converts a MetadataSchemaEditView to an EditableEntityMetadataSchemaInput.
    *
    * This function provides an explicit conversion for use in the MetadataSchemaApi, encapsulating
    * the transformation logic and keeping the API code clean.
    *
    * @param view
    *   The MetadataSchemaEditView to convert.
    * @return
    *   The corresponding EditableEntityMetadataSchemaInput.
    */
  def toInput(view: MetadataSchemaEditView): EditableEntityMetadataSchemaInput =
    view.transformInto[EditableEntityMetadataSchemaInput]
}
