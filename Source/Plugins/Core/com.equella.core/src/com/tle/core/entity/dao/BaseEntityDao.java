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

package com.tle.core.entity.dao;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BaseEntityDao {
  LanguageBundle getEntityNameForId(long id);

  /**
   * Retrieve any base entity of the current institution by its ID, regardless of its concrete
   * subtype.
   *
   * <p>Because {@code BaseEntity} is the root of a JOINED inheritance hierarchy, this performs a
   * polymorphic query and returns an instance of the concrete subclass (e.g. {@code Schema}, {@code
   * ItemDefinition}). That resolution outer-joins every subclass table, so this is intended for
   * one-off lookups rather than being called in a loop. Callers reflecting on the runtime type
   * should use {@code Hibernate.getClass()}, since a proxy may be returned if one is already in the
   * session.
   *
   * <p>NOTE: unlike the by-id convention elsewhere (e.g. {@code getEntityNameForId} above, and
   * {@code GenericDaoImpl.findById}), this <em>is</em> institution filtered. Entity IDs are
   * globally unique, so an ID originating from another institution would otherwise resolve here.
   * Filtering within the query keeps the guard atomic with the lookup, and avoids having to
   * initialise the entity's lazy institution in order to compare it afterwards.
   *
   * @param id the identity of the entity
   * @return the entity, or empty if the current institution has no entity with that ID - including
   *     when there is no current institution
   */
  Optional<BaseEntity> getEntityInCurrentInstitution(long id);

  Map<Long, String> getUuids(Set<Long> ids);

  List<Long> getIdsFromUuids(Set<String> uuids);
}
