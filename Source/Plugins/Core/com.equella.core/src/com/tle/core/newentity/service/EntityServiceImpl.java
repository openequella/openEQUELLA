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

package com.tle.core.newentity.service;

import com.tle.beans.Institution;
import com.tle.beans.newentity.Entity;
import com.tle.beans.newentity.EntityID;
import com.tle.core.guice.Bind;
import com.tle.core.newentity.dao.EntityDao;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(EntityService.class)
@Singleton
public class EntityServiceImpl implements EntityService {
  @Inject private EntityDao entityDao;

  @Override
  public List<Entity> getAllEntity(Institution institution) {
    return entityDao.getAll(institution);
  }

  @Override
  public void setEntity(
      String uuid,
      String typeId,
      String name,
      String nameStrings,
      String description,
      String descriptionStrings,
      Instant created,
      Instant modified,
      String owner,
      String data,
      Institution institution) {
    EntityID id = new EntityID();
    id.uuid_$eq(uuid);
    id.inst_id_$eq(institution.getDatabaseId());

    Entity entity = new Entity();
    entity.id_$eq(id);
    entity.name_$eq(name);
    entity.nameStrings_$eq(nameStrings);
    entity.description_$eq(description);
    entity.descriptionStrings_$eq(descriptionStrings);
    entity.created_$eq(created);
    entity.modified_$eq(modified);
    entity.owner_$eq(owner);
    entity.typeid_$eq(typeId);
    entity.data_$eq(data);

    entityDao.saveOrUpdate(entity);
  }

  @Override
  public void deleteAllEntity(Institution institution) {
    entityDao.deleteAll(institution);
  }
}
