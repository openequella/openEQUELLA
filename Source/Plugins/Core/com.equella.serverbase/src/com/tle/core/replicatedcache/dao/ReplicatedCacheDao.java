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

package com.tle.core.replicatedcache.dao;

import java.util.Collection;
import java.util.Date;

import com.tle.beans.Institution;
import com.tle.core.hibernate.dao.GenericDao;

public interface ReplicatedCacheDao extends GenericDao<CachedValue, Long>
{
	CachedValue get(String cacheId, String key);

	void put(String cacheId, String key, Date ttl, byte[] value);

	void invalidate(String cacheId, String... keys);

	void invalidateAllForInstitution(Institution inst);

	Collection<CachedValue> getBatch(String cacheId, String keyPrefixFilter, long startId, int batchSize);

	void invalidateExpiredEntries();
}
