/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

public interface AbstractEntityDao<T extends BaseEntity> extends GenericInstitutionalDao<T, Long>
{
	List<BaseEntityLabel> listAll(String resolveVirtualTo);

	List<BaseEntityLabel> listEnabled(final String resolveVirtualTo);

	List<BaseEntityLabel> listAllIncludingSystem(final String resolveVirtualTo);

	List<T> enumerateAllIncludingSystem();

	List<T> enumerateEnabled();

	List<Long> enumerateAllIdsIncludingSystem();

	List<T> getByIds(Collection<Long> ids);

	List<T> getByUuids(Collection<String> ids);

	T getByUuid(String uuid);

	@Override
	Class<T> getPersistentClass();

	Set<String> getReferencedUsers();

	String getUuidForId(long id);

	List<T> search(String freetext, boolean archived, int offset, int perPage);

	void removeOrphanedOwners(String owner);

	void changeOwnerId(final String fromOwnerId, final String toOwnerId);
}
