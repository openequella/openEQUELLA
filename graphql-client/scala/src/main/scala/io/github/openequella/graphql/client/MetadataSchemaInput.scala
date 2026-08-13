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

package io.github.openequella.graphql.client

import caliban.client._
import caliban.client.__Value._

final case class MetadataSchemaInput(
    details: EntityDetailsInput,
    exportTransforms: List[MetadataSchemaTransformInput] = Nil,
    importTransforms: List[MetadataSchemaTransformInput] = Nil,
    itemNamePath: String,
    itemDescriptionPath: String,
    definition: String,
    citations: List[CitationInput] = Nil
)
object MetadataSchemaInput {
  implicit val encoder: ArgEncoder[MetadataSchemaInput] = new ArgEncoder[MetadataSchemaInput] {
    override def encode(value: MetadataSchemaInput): __Value =
      __ObjectValue(
        List(
          "details"          -> implicitly[ArgEncoder[EntityDetailsInput]].encode(value.details),
          "exportTransforms" -> __ListValue(
            value.exportTransforms.map(value =>
              implicitly[ArgEncoder[MetadataSchemaTransformInput]].encode(value)
            )
          ),
          "importTransforms" -> __ListValue(
            value.importTransforms.map(value =>
              implicitly[ArgEncoder[MetadataSchemaTransformInput]].encode(value)
            )
          ),
          "itemNamePath"        -> implicitly[ArgEncoder[String]].encode(value.itemNamePath),
          "itemDescriptionPath" -> implicitly[ArgEncoder[String]].encode(value.itemDescriptionPath),
          "definition"          -> implicitly[ArgEncoder[String]].encode(value.definition),
          "citations"           -> __ListValue(
            value.citations.map(value => implicitly[ArgEncoder[CitationInput]].encode(value))
          )
        )
      )
  }
}
