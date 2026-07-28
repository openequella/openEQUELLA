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

package com.tle.core.entity.service;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.EntityPack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BaseEntityService {

  /**
   * Get the language bundle for the name of an entity.
   *
   * @param id the identity of the entity.
   */
  LanguageBundle getNameForId(long id);

  /**
   * Retrieve the read-only pack - the entity plus its access control lists - for any base entity,
   * regardless of its type.
   *
   * <p>This is the entity type agnostic counterpart of {@link
   * com.tle.core.remoting.RemoteAbstractEntityService#getReadOnlyPack(long)}. The concrete entity
   * service is resolved from the entity's runtime class via the {@link
   * com.tle.core.entity.registry.EntityRegistry} and the call is delegated to it, so the ACL check
   * for the relevant {@code EDIT_<TYPE>} privilege, and any type specific sub-entity target lists,
   * are those of that service.
   *
   * @param id the identity of the entity
   * @return the pack, or empty if there is no entity with that ID in the current institution
   * @throws UnsupportedOperationException if the entity exists but its type has no registered
   *     entity service, and hence its access control lists cannot be determined
   * @throws com.tle.exceptions.AccessDeniedException if the current user may not edit the entity
   */
  Optional<EntityPack<BaseEntity>> getReadOnlyPack(long id);

  List<Long> getIdsFromUuids(Set<String> uuids);

  Map<Long, String> getUuids(Set<Long> ids);

  /**
   * A list of edit privileges for entities where the user either does not have permission, or does
   * have permission but there are no actual entities that can be edited (there are none, or revokes
   * are on the entities themselves).
   */
  List<String> getEditPrivilegeForEntitiesIHaveNoneToEdit();
}
