/*
 * Created on Jun 1, 2005
 */
package com.tle.core.services.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.core.remoting.RemoteBaseEntityService;

/**
 * @author Nicholas Read
 */
public interface BaseEntityService extends RemoteBaseEntityService
{
	List<Long> getIdsFromUuids(Set<String> uuids);

	Map<Long, String> getUuids(Set<Long> ids);

	/**
	 * A list of edit privileges for entities where the user either does not
	 * have permission, or does have permission but there are no actual entities
	 * that can be edited (there are none, or revokes are on the entities
	 * themselves).
	 */
	List<String> getEditPrivilegeForEntitiesIHaveNoneToEdit();
}
