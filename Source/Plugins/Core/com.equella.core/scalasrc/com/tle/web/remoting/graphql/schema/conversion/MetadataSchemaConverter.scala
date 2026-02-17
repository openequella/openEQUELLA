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

package com.tle.web.remoting.graphql.schema.conversion

import com.tle.beans.entity.{Schema, SchemaTransform}
import com.tle.web.remoting.graphql.schema.conversion.CitationConverter.toCitation
import com.tle.web.remoting.graphql.schema.conversion.EntityDetailsConverter.populateBaseEntity
import com.tle.web.remoting.graphql.schema.types.{MetadataSchema, MetadataSchemaTransform}

import scala.jdk.CollectionConverters._

/** Converter for transforming GraphQL [[MetadataSchema]] to the Hibernate [[Schema]] entity.
  *
  * This converter handles the complete metadata schema structure including base entity details,
  * import/export transforms, item paths, schema definition, and citations.
  */
object MetadataSchemaConverter {

  /** Converts a GraphQL metadata schema to its Hibernate entity representation.
    *
    * @param metadataSchema
    *   the GraphQL metadata schema containing all schema configuration
    * @return
    *   a new [[Schema]] entity populated with the provided values
    */
  def toSchema(metadataSchema: MetadataSchema): Schema = {
    val schema = new Schema()

    populateBaseEntity(metadataSchema.details, schema)
    schema.setExportTransforms(
      metadataSchema.exportTransforms.map(toSchemaTransform).asJava
    )
    schema.setImportTransforms(
      metadataSchema.importTransforms.map(toSchemaTransform).asJava
    )
    schema.setItemNamePath(metadataSchema.itemNamePath)
    schema.setItemDescriptionPath(metadataSchema.itemDescriptionPath)
    schema.setSerialisedDefinition(metadataSchema.definition)
    schema.setCitations(
      metadataSchema.citations.map(toCitation).asJava
    )

    schema
  }

  private def toSchemaTransform(
      transform: MetadataSchemaTransform
  ): SchemaTransform = {
    val st = new SchemaTransform()
    st.setFilename(transform.filename)
    st.setType(transform.schemaType)

    st
  }
}
