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

import com.tle.beans.entity.BaseEntity
import com.tle.common.EntityPack
import io.github.openequella.graphql.api.views.EntitySkeletonView

object EntitySkeletonViewConverter {

  /** Converts an `EntitySkeletonView` to an `EntityPack` by creating and populating a fresh entity
    * instance.
    *
    * This converter uses a by-name parameter factory to create a new entity internally, avoiding
    * mutation of externally-created objects and maintaining functional programming principles.
    *
    * @param entityFactory
    *   A by-name parameter that creates a fresh entity instance. This is evaluated inside the
    *   method to instantiate the entity, which is then populated with data from the view. Example:
    *   `toEntityPack(new Schema)` (note: no parentheses after Schema, making it by-name).
    * @param view
    *   The entity skeleton view containing basic entity metadata (uuid, owner, stagingId).
    * @tparam T
    *   The specific type of `BaseEntity` being created.
    * @return
    *   An `EntityPack` containing the newly created and populated entity along with the staging ID.
    */
  def toEntityPack[T <: BaseEntity](
      entityFactory: => T
  )(view: EntitySkeletonView): EntityPack[T] = {
    val entity = entityFactory
    entity.setOwner(view.owner)
    entity.setUuid(view.uuid)

    new EntityPack[T](entity, view.stagingId)
  }
}
