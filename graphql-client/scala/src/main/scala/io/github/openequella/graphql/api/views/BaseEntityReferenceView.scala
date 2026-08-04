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
import io.github.openequella.graphql.client.BaseEntityReference

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
  * @param forCollection
  *   Whether this entity is a collection
  */
final case class BaseEntityReferenceView(
    id: Long,
    uuid: String,
    bundleId: Long,
    owner: String,
    forCollection: Boolean
)
object BaseEntityReferenceView {

  /** The selection builder for BaseEntityReferenceView.
    */
  val selector: SelectionBuilder[BaseEntityReference, BaseEntityReferenceView] =
    (BaseEntityReference.id ~ BaseEntityReference.uuid ~ BaseEntityReference.bundleId ~ BaseEntityReference.owner ~ BaseEntityReference.forCollection)
      .mapN(BaseEntityReferenceView(_, _, _, _, _))
}
