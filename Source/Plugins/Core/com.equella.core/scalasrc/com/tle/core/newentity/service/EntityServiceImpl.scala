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

package com.tle.core.newentity.service

import com.tle.beans.Institution
import com.tle.beans.newentity.{Entity, EntityID}
import com.tle.common.institution.CurrentInstitution
import com.tle.core.guice.Bind
import com.tle.core.newentity.dao.EntityDao
import org.springframework.transaction.annotation.Transactional
import javax.inject.Singleton
import java.time.Instant
import java.util
import javax.inject.Inject

@Bind(classOf[EntityService])
@Singleton
class EntityServiceImpl extends EntityService {
  @Inject var entityDao: EntityDao = _

  override def getByUuid(uuid: String): Entity =
    entityDao.findById(EntityID(uuid, CurrentInstitution.get().getDatabaseId))

  override def getAllByInstitution(institution: Institution): java.util.List[Entity] =
    entityDao.getAllByInstitution(institution)

  override def getAllByType(typeId: String): util.List[Entity] = {
    entityDao.getAllByType(typeId)
  }

  @Transactional
  override def createOrUpdate(
      uuid: String,
      typeId: String,
      name: String,
      nameStrings: String,
      description: String,
      descriptionStrings: String,
      created: Instant,
      modified: Instant,
      owner: String,
      data: String,
      institution: Institution
  ): Unit = {
    val id = new EntityID
    id.uuid = uuid
    id.inst_id = institution.getDatabaseId

    val entity = new Entity
    entity.id = id
    entity.name = name
    entity.nameStrings = nameStrings
    entity.description = description
    entity.descriptionStrings = descriptionStrings
    entity.created = created
    entity.modified = modified
    entity.owner = owner
    entity.typeid = typeId
    entity.data = data
    createOrUpdate(entity)
  }

  @Transactional
  override def createOrUpdate(entity: Entity): Unit = {
    entityDao.saveOrUpdate(entity)
  }

  @Transactional
  override def delete(entity: Entity): Unit = {
    entityDao.delete(entity)
  }

  @Transactional
  override def deleteAllEntity(institution: Institution): Unit = entityDao.deleteAll(institution)
}
