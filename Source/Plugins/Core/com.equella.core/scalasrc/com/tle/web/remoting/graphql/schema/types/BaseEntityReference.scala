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
import com.tle.beans.entity.BaseEntityLabel

/** A reference to a base entity, which is used in various places in the GraphQL API. Includes the
  * essential values from the BaseEntityLabel, which is typically used for identifying lists of
  * BaseEntities. It should be possible to easily reconstruct the BaseEntityLabel from this on the
  * client side.
  *
  * @param id
  *   the database ID of the entity
  * @param uuid
  *   the UUID of the entity
  * @param bundleId
  *   the language bundle ID of the entity
  * @param owner
  *   the owner of the entity
  */
final case class BaseEntityReference(
    @GQLDescription("The database ID of the entity")
    id: Long,
    @GQLDescription("The UUID of the entity")
    uuid: String,
    @GQLDescription("The language bundle ID of the entity")
    bundleId: Long,
    @GQLDescription("The owner of the entity")
    owner: String,
    @GQLDescription("Whether this entity is a collection")
    forCollection: Boolean
)

object BaseEntityReference {
  def apply(label: BaseEntityLabel): BaseEntityReference = BaseEntityReference(
    id = label.getId,
    uuid = label.getUuid,
    bundleId = label.getBundleId,
    owner = label.getOwner,
    forCollection = label.isForCollection
  )
}
