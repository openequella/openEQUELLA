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

package com.tle.core.entity.dao.impl;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.EntityLock;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.dao.EntityLockingDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Bind(EntityLockingDao.class)
@Singleton
@SuppressWarnings("nls")
public class EntityLockingDaoImpl extends GenericDaoImpl<EntityLock, String>
    implements EntityLockingDao {
  public EntityLockingDaoImpl() {
    super(EntityLock.class);
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteForEntity(BaseEntity entity) {
    getHibernateTemplate().bulkUpdate("DELETE FROM EntityLock WHERE entity = ?0", entity);
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteAll() {
    getHibernateTemplate()
        .bulkUpdate("DELETE FROM ItemLock WHERE institution = ?0", CurrentInstitution.get());
  }
}
