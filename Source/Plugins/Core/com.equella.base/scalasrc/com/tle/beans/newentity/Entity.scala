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

package com.tle.beans.newentity

import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.CurrentUser
import java.util.{Objects, UUID}
import java.time.Instant
import javax.persistence.{Column, Embeddable, EmbeddedId, Index, Lob, Table}
import org.hibernate.annotations.{AttributeAccessor, NamedQuery, Type}

@Embeddable
class EntityID extends Serializable {
  @Column(length = 36)
  var uuid: String  = _
  var inst_id: Long = _

  override def equals(obj: Any): Boolean = obj match {
    case that: EntityID => this.uuid == that.uuid && this.inst_id == that.inst_id
    case _              => false
  }

  override def hashCode: Int = Objects.hash(inst_id: java.lang.Long, uuid: java.lang.String)
}

@javax.persistence.Entity
@AttributeAccessor("field")
@Table(
  name = "entities",
  indexes = Array {
    new Index(name = "entityTypeIdx", columnList = "inst_id, typeid")
  }
)
@NamedQuery(
  name = "deleteAllEntityByInst",
  query = "DELETE FROM Entity WHERE id.inst_id = :institutionId"
)
@NamedQuery(
  name = "getAllByType",
  query = "from Entity WHERE id.inst_id = :institutionId and typeid = :typeId"
)
class Entity {
  @EmbeddedId
  var id: EntityID = _
  @Column(length = 20)
  var typeid: String = _
  @Lob
  var name: String = _
  @Type(`type` = "json")
  var nameStrings: String = _
  @Lob
  var description: String = _
  @Type(`type` = "json")
  var descriptionStrings: String = _
  @Column
  var owner: String     = _
  var created: Instant  = _
  var modified: Instant = _
  @Type(`type` = "json")
  var data: String = _
}

object EntityID {
  def apply(uuid: String, inst_id: Long): EntityID = {
    val id = new EntityID
    id.uuid = uuid
    id.inst_id = inst_id
    id
  }
}

object Entity {
  def apply(
      uuid: String,
      inst_id: Long,
      typeId: String,
      name: String,
      nameStrings: String,
      description: String,
      descriptionStrings: String,
      owner: String,
      created: Instant,
      modified: Instant,
      data: String
  ): Entity = {
    val entity = new Entity
    entity.id = EntityID(uuid, inst_id)
    entity.typeid = typeId
    entity.name = name
    entity.nameStrings = nameStrings
    entity.description = description
    entity.descriptionStrings = descriptionStrings
    entity.owner = owner
    entity.created = created
    entity.modified = modified
    entity.data = data

    entity
  }

  def blankEntity(typeId: String): Entity =
    Entity(
      uuid = UUID.randomUUID().toString,
      inst_id = CurrentInstitution.get().getDatabaseId,
      typeId = typeId,
      name = "",
      nameStrings = "",
      description = "",
      descriptionStrings = "",
      owner = CurrentUser.getDetails.getUniqueID,
      created = Instant.now(),
      modified = Instant.now(),
      data = ""
    )
}
