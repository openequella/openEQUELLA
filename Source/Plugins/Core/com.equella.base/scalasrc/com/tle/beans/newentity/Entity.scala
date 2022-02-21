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

import java.time.Instant
import javax.persistence.{Column, Embeddable, EmbeddedId, Index, Lob, Table}
import org.hibernate.annotations.{AttributeAccessor, NamedQuery, Type}

@Embeddable
class EntityID extends Serializable {
  @Column(length = 36)
  var uuid: String  = _
  var inst_id: Long = _
}

@javax.persistence.Entity
@AttributeAccessor("field")
@Table(name = "entities", indexes = Array {
  new Index(name = "entityTypeIdx", columnList = "inst_id, typeid")
})
@NamedQuery(name = "deleteAllEntityByInst",
            query = "DELETE FROM Entity WHERE id.inst_id = :institutionId")
class Entity {
  @EmbeddedId
  var id: EntityID = _
  @Column(length = 20)
  var typeid: String = _
  @Lob
  var name: String = _
  @Type(`type` = "json")
  var name_strings: String = _
  @Lob
  var description: String = _
  @Type(`type` = "json")
  var description_strings: String = _
  @Column
  var owner: String     = _
  var created: Instant  = _
  var modified: Instant = _
  @Type(`type` = "json")
  var data: String = _
}
