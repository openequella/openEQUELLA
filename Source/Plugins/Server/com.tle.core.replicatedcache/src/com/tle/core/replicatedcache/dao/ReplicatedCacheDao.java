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
