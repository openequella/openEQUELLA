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
import io.github.openequella.graphql.client.CollectionImsMapping

/** View model for IMS/LOM metadata mapping configuration.
  *
  * @param ims
  *   IMS/LOM source field path.
  * @param itemdef
  *   Collection metadata target path.
  * @param mappingType
  *   Mapping type.
  * @param replace
  *   Whether to replace existing values at the target path.
  */
final case class CollectionImsMappingView(
    ims: String,
    itemdef: String,
    mappingType: String,
    replace: Boolean
)

object CollectionImsMappingView {
  val selector: SelectionBuilder[CollectionImsMapping, CollectionImsMappingView] =
    (
      CollectionImsMapping.ims ~
        CollectionImsMapping.itemdef ~
        CollectionImsMapping.mappingType ~
        CollectionImsMapping.replace
    ).mapN(CollectionImsMappingView.apply _)
}
