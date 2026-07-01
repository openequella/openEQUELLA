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

import com.tle.admin.graphql.conversion.CollectionHtmlMappingViewConverter.{
  fromHtmlMapping,
  toHtmlMapping
}
import com.tle.admin.graphql.conversion.CollectionImsMappingViewConverter.{
  fromImsMapping,
  toImsMapping
}
import com.tle.admin.graphql.conversion.CollectionLiteralMappingViewConverter.{
  fromLiteralMapping,
  toLiteralMapping
}
import com.tle.beans.entity.itemdef.MetadataMapping
import io.github.openequella.graphql.api.views.CollectionMetadataMappingView

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

object CollectionMetadataMappingViewConverter {
  def toMetadataMapping(view: CollectionMetadataMappingView): MetadataMapping =
    new MetadataMapping().tap { mm =>
      // MetadataMapping fields are Collections initialized to empty ArrayLists, so addAll works
      mm.getImsMapping.addAll(view.imsMapping.map(toImsMapping).asArrayList)
      mm.getHtmlMapping.addAll(view.htmlMapping.map(toHtmlMapping).asArrayList)
      mm.getLiteralMapping.addAll(view.literalMapping.map(toLiteralMapping).asArrayList)
    }

  def fromMetadataMapping(mapping: MetadataMapping): CollectionMetadataMappingView =
    CollectionMetadataMappingView(
      imsMapping = mapping.getImsMapping.asScala.map(fromImsMapping).toList,
      htmlMapping = mapping.getHtmlMapping.asScala.map(fromHtmlMapping).toList,
      literalMapping = mapping.getLiteralMapping.asScala.map(fromLiteralMapping).toList
    )
}
