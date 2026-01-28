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
import io.github.openequella.graphql.client.EditableEntitySkeleton

/** View model for an entity skeleton, providing basic identifying information.
  *
  * @param uuid
  *   Universally unique identifier for referencing the entity.
  * @param owner
  *   Owner of the entity.
  * @param stagingId
  *   Staging identifier associated with the entity.
  */
final case class EntitySkeletonView(
    uuid: String,
    owner: String,
    stagingId: String
)

object EntitySkeletonView {
  val selector: SelectionBuilder[EditableEntitySkeleton, EntitySkeletonView] =
    (
      EditableEntitySkeleton.uuid ~
        EditableEntitySkeleton.owner ~
        EditableEntitySkeleton.stagingId
    ).mapN(EntitySkeletonView.apply _)
}
