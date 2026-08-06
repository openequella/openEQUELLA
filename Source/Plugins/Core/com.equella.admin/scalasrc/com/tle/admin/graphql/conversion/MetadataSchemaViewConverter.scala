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

package com.tle.admin.graphql.conversion

import com.tle.admin.graphql.conversion.CitationViewConverter.{fromCitation, toCitation}
import com.tle.admin.graphql.conversion.EntityDetailsViewConverter.{
  applyToBaseEntity,
  fromBaseEntity
}
import com.tle.beans.entity.{Schema, SchemaTransform}
import io.github.openequella.graphql.api.views.{MetadataSchemaTransformView, MetadataSchemaView}

import scala.util.chaining.scalaUtilChainingOps

object MetadataSchemaViewConverter {
  def toSchema(view: MetadataSchemaView): Schema = new Schema(view.details.id).tap { s =>
    applyToBaseEntity(s, view.details)

    s.setItemNamePath(view.itemNamePath)
    s.setItemDescriptionPath(view.itemDescriptionPath)
    s.setSerialisedDefinition(view.definition)
    s.setExportTransforms(view.exportTransforms.map(toSchemaTransform).asArrayList)
    s.setImportTransforms(view.importTransforms.map(toSchemaTransform).asArrayList)
    s.setCitations(view.citations.map(toCitation).asArrayList)
  }

  private def toSchemaTransform(view: MetadataSchemaTransformView): SchemaTransform =
    new SchemaTransform().tap { st =>
      st.setFilename(view.filename)
      st.setType(view.schemaType)
    }

  def fromSchema(schema: Schema): MetadataSchemaView = {
    val exportTransforms = NullSafeList(schema.getExportTransforms) convert fromSchemaTransform
    val importTransforms = NullSafeList(schema.getImportTransforms) convert fromSchemaTransform
    val citations        = NullSafeList(schema.getCitations) convert fromCitation

    MetadataSchemaView(
      details = fromBaseEntity(schema),
      exportTransforms = exportTransforms,
      importTransforms = importTransforms,
      itemNamePath = schema.getItemNamePath,
      itemDescriptionPath = schema.getItemDescriptionPath,
      definition = schema.getSerialisedDefinition,
      citations = citations
    )
  }

  private def fromSchemaTransform(transform: SchemaTransform): MetadataSchemaTransformView =
    MetadataSchemaTransformView(
      filename = transform.getFilename,
      schemaType = transform.getType
    )
}
