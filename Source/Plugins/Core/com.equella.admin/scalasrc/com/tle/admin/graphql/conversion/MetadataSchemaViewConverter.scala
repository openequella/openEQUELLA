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

import com.tle.admin.graphql.conversion.CitationViewConverter.toCitation
import com.tle.beans.entity.{Schema, SchemaTransform}
import io.github.openequella.graphql.api.views.{MetadataSchemaTransformView, MetadataSchemaView}

import scala.jdk.CollectionConverters._

object MetadataSchemaViewConverter {
  def toSchema(view: MetadataSchemaView): Schema = {
    val s = new Schema(view.details.id)
    s.setUuid(view.details.uuid)
    // We only have the bundleId, so skipping trying to set the Language Bundle
    s.setOwner(view.details.owner)
    s.setItemNamePath(view.itemNamePath)
    s.setItemDescriptionPath(view.itemDescriptionPath)
    s.setSerialisedDefinition(view.definition)
    s.setExportTransforms(view.exportTransforms.map(toSchemaTransform).asJava)
    s.setImportTransforms(view.importTransforms.map(toSchemaTransform).asJava)
    s.setCitations(view.citations.map(toCitation).asJava)

    s
  }

  private def toSchemaTransform(view: MetadataSchemaTransformView): SchemaTransform = {
    val st = new SchemaTransform()
    st.setFilename(view.filename)
    st.setType(view.schemaType)

    st
  }
}
