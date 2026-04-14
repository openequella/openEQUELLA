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

/** A placeholder type for a collection definition (what was historically called an Item Definition
  * in openEQUELLA). This type intentionally contains only the entity details for now, and will be
  * expanded with collection-specific fields as the GraphQL API matures.
  */
@GQLDescription(
  "A collection definition, defining the structure and configuration for a collection of items in openEQUELLA."
)
final case class CollectionDefinition(
    @GQLDescription("Common details of the collection definition, including UUID and owner.")
    details: EntityDetails
)

object CollectionDefinition {

  /** Converts an `ItemDefinition` entity to a `CollectionDefinition` for use in GraphQL.
    *
    * @param entity
    *   The ItemDefinition entity to convert.
    * @return
    *   A `CollectionDefinition` representation of the entity.
    */
  def apply(entity: com.tle.beans.entity.itemdef.ItemDefinition): CollectionDefinition =
    CollectionDefinition(details = EntityDetails(entity))
}
