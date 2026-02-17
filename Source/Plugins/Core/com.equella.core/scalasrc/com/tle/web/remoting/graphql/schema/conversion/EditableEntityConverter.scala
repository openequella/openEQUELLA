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

package com.tle.web.remoting.graphql.schema.conversion

import com.tle.common.EntityPack
import com.tle.web.remoting.graphql.schema.conversion.TargetListEntryConverter.toTargetList
import com.tle.web.remoting.graphql.schema.types.EditableEntity

/** Converter for transforming GraphQL [[EditableEntity]] wrappers to [[EntityPack]] structures.
  *
  * This is a generic converter that works with any entity type, delegating the specific entity
  * conversion to a provided function while handling the common wrapper fields (staging ID, version,
  * and target list).
  */
object EditableEntityConverter {

  /** Converts a GraphQL editable entity wrapper to an [[EntityPack]].
    *
    * @param editableEntity
    *   the GraphQL wrapper containing the entity and metadata
    * @param toBaseEntity
    *   conversion function for the specific entity type
    * @tparam A
    *   the GraphQL entity type
    * @tparam E
    *   the target Hibernate entity type (must extend [[com.tle.beans.entity.BaseEntity]])
    * @return
    *   a new [[EntityPack]] containing the converted entity and access control list
    */
  def toEntityPack[A, E <: com.tle.beans.entity.BaseEntity](
      editableEntity: EditableEntity[A],
      toBaseEntity: A => E
  ): EntityPack[E] = {
    val pack = new EntityPack[E]()

    pack.setStagingID(editableEntity.stagingId)
    editableEntity.version.foreach(pack.setVersion)

    pack.setTargetList(editableEntity.targetList convert toTargetList)
    pack.setEntity(editableEntity.entity convert toBaseEntity)

    pack
  }
}
