/*
 * Created on Oct 26, 2005
 */
package com.tle.core.dao;

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
