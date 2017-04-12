package com.tle.core.dao;

import java.util.Collection;
import java.util.List;

import com.tle.beans.Institution;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.beans.security.AccessEntry;
import com.tle.common.security.TargetListEntry;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface AclDao extends GenericDao<AccessEntry, Long>
{
	List<Object[]> getPrivileges(Collection<String> privileges, Collection<Long> expressions);

	List<Object[]> getPrivilegesForTargets(Collection<String> privileges, Collection<String> targets,
		Collection<Long> expressions);

	void delete(String target, String privilege, Institution institution);

	void deleteAll(String target, boolean targetIsPartial, List<Integer> priorities);

	List<TargetListEntry> getTargetListEntries(String target, Collection<Integer> priorities);

	List<ACLEntryMapping> getAllEntries(Collection<String> privilege, Collection<String> targets);

	List<AccessEntry> listAll();

	void deleteAll();

	void remapExpressionId(long oldId, long newId);

	List<AccessEntry> getVirtualAccessEntries(Collection<Integer> priorities);
}
