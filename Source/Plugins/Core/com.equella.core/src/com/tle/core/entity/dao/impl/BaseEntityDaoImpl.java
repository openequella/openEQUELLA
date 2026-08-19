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
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.entity.dao.BaseEntityDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.AbstractHibernateDao;
import java.util.Optional;
import javax.inject.Singleton;

@SuppressWarnings("nls")
@Bind(BaseEntityDao.class)
@Singleton
public class BaseEntityDaoImpl extends AbstractHibernateDao implements BaseEntityDao {
  @Override
  public Optional<BaseEntity> getEntityInCurrentInstitution(final long id) {
    return findUnique(
        BaseEntity.class,
        (builder, entity) ->
            builder.and(
                builder.equal(entity.get("id"), id),
                CurrentInstitution.equalityPredicate(builder, entity)));
  }

  @Override
  public LanguageBundle getEntityNameForId(final long id) {
    // A projection rather than an entity load, so this stays HQL - the criteriaQuery/findUnique
    // helpers select instances of a class, which cannot express "SELECT name". The alias is not
    // optional: selecting the association makes it the query's primary entity, so an unqualified
    // "institution" resolves against the language bundle rather than the base entity.
    return withSession(
            session ->
                session
                    .createQuery(
                        "SELECT be.name FROM BaseEntity be"
                            + " WHERE be.institution = :institution AND be.id = :id",
                        LanguageBundle.class)
                    .setParameter("institution", CurrentInstitution.get())
                    .setParameter("id", id)
                    .uniqueResultOptional())
        .orElse(null);
  }
}
