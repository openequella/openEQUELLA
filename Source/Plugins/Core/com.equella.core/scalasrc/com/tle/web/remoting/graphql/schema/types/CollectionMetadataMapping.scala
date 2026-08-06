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
import com.tle.beans.entity.itemdef.MetadataMapping
import com.tle.beans.entity.itemdef.mapping.{HTMLMapping, IMSMapping, Literal, LiteralMapping}

/** GraphQL representation of `com.tle.beans.entity.itemdef.MetadataMapping`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.MetadataMapping]]
  */
@GQLDescription("Configuration for mapping external metadata formats to collection metadata.")
final case class CollectionMetadataMapping(
    @GQLDescription("IMS/LOM metadata mappings")
    imsMapping: List[CollectionImsMapping],
    @GQLDescription("HTML metadata mappings")
    htmlMapping: List[CollectionHtmlMapping],
    @GQLDescription("Literal value metadata mappings")
    literalMapping: List[CollectionLiteralMapping]
)

object CollectionMetadataMapping {
  def apply(mapping: MetadataMapping): CollectionMetadataMapping =
    CollectionMetadataMapping(
      imsMapping = convertJavaList(mapping.getImsMapping)(CollectionImsMapping(_)),
      htmlMapping = convertJavaList(mapping.getHtmlMapping)(CollectionHtmlMapping(_)),
      literalMapping = convertJavaList(mapping.getLiteralMapping)(CollectionLiteralMapping(_))
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.mapping.IMSMapping`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.mapping.IMSMapping]]
  */
@GQLDescription("An IMS/LOM metadata field mapping.")
final case class CollectionImsMapping(
    @GQLDescription("IMS/LOM source field path")
    ims: String,
    @GQLDescription("Collection metadata target path")
    itemdef: String,
    @GQLDescription("Mapping type")
    mappingType: String,
    @GQLDescription("Whether to replace existing values at the target path")
    replace: Boolean
)

object CollectionImsMapping {
  def apply(mapping: IMSMapping): CollectionImsMapping =
    CollectionImsMapping(
      ims = mapping.getIms,
      itemdef = mapping.getItemdef,
      mappingType = mapping.getType,
      replace = mapping.isReplace
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.mapping.HTMLMapping`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.mapping.HTMLMapping]]
  */
@GQLDescription("An HTML metadata field mapping.")
final case class CollectionHtmlMapping(
    @GQLDescription("HTML source field name")
    html: String,
    @GQLDescription("Collection metadata target path")
    itemdef: String
)

object CollectionHtmlMapping {
  def apply(mapping: HTMLMapping): CollectionHtmlMapping =
    CollectionHtmlMapping(
      html = mapping.getHtml,
      itemdef = mapping.getItemdef
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.mapping.LiteralMapping`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.mapping.LiteralMapping]]
  */
@GQLDescription("A literal value metadata mapping.")
final case class CollectionLiteralMapping(
    @GQLDescription("Collection metadata target path")
    value: Option[String],
    @GQLDescription("Literal values that trigger this mapping")
    literals: List[CollectionLiteral]
)

object CollectionLiteralMapping {
  def apply(mapping: LiteralMapping): CollectionLiteralMapping =
    CollectionLiteralMapping(
      value = Option(mapping.getValue),
      literals = convertJavaList(mapping.getLiterals)(CollectionLiteral(_))
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.mapping.Literal`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.mapping.Literal]]
  */
@GQLDescription("A literal value used to trigger a metadata mapping.")
final case class CollectionLiteral(
    @GQLDescription("The literal value to match")
    value: Option[String],
    @GQLDescription("A script to compute the mapping value")
    script: Option[String]
)

object CollectionLiteral {
  def apply(literal: Literal): CollectionLiteral =
    CollectionLiteral(
      value = Option(literal.getValue),
      script = Option(literal.getScript)
    )
}
