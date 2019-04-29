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

package com.tle.core.searches

import java.time.Instant
import java.util.{Date, UUID}

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonSubTypes, JsonTypeInfo}
import com.tle.core.validation.OEQEntityEdits
import io.circe.{Decoder, Encoder}

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new Type(value = classOf[SortControl], name = "sort"),
    new Type(value = classOf[OwnerControl], name = "owner"),
    new Type(value = classOf[ModifiedWithinControl], name = "modifiedWithin"),
    new Type(value = classOf[FacetControl], name = "facet"),
    new Type(value = classOf[CollectionsControl], name = "collections")
  )
)
sealed trait SearchControl

case class SortControl(default: String, editable: Boolean)           extends SearchControl
case class OwnerControl(default: Option[String], editable: Boolean)  extends SearchControl
case class ModifiedWithinControl(default: Double, editable: Boolean) extends SearchControl
case class FacetControl(title: String, node: String)                 extends SearchControl
case class CollectionsControl(collections: Option[Iterable[UUID]], editable: Boolean)
    extends SearchControl

object SearchControl {

  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto._

  implicit val customConfig: Configuration = Configuration.default
    .withDiscriminator("type")
    .copy(transformConstructorNames = {
      case "SortControl"           => "sort"
      case "OwnerControl"          => "owner"
      case "ModifiedWithinControl" => "modifiedWithin"
      case "FacetControl"          => "facet"
      case "CollectionsControl"    => "collections"
    })
  implicit val sctrlEncoder: Encoder[SearchControl] = deriveEncoder
  implicit val sctrlDecoder: Decoder[SearchControl] = deriveDecoder
}

case class SearchConfig(
    id: UUID,
    index: String,
    name: String,
    nameStrings: Option[Map[String, String]],
    description: Option[String],
    descriptionStrings: Option[Map[String, String]],
    created: Date,
    modified: Date,
    sections: Map[String, Iterable[SearchControl]]
)

@JsonIgnoreProperties(ignoreUnknown = true)
case class SearchConfigEdit(
    id: Option[UUID],
    index: String,
    name: String,
    nameStrings: Option[Map[String, String]],
    description: Option[String],
    descriptionStrings: Option[Map[String, String]],
    sections: Map[String, Iterable[SearchControl]]
) extends OEQEntityEdits

case class SearchPageConfig(configId: UUID)

object SearchPageConfig {

  import io.circe.generic.semiauto._

  implicit val spcEncoder: Encoder[SearchPageConfig] = deriveEncoder
  implicit val spcDecoder: Decoder[SearchPageConfig] = deriveDecoder
}
