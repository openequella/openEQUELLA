package com.tle.core.dao.user;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.TLEGroup;
import com.tle.core.dao.AbstractTreeDao;

/**
 * @author Nicholas Read
 */
public interface TLEGroupDao extends AbstractTreeDao<TLEGroup>
{
	List<TLEGroup> listAllGroups();

	List<TLEGroup> getGroupsContainingUser(String userID);

	List<TLEGroup> searchGroups(String query, String parentId);

	List<String> getUsersInGroup(String parentGroupID, boolean recurse);

	TLEGroup findByUuid(String id);

	List<TLEGroup> getInformationForGroups(Collection<String> groups);

	boolean addUserToGroup(String groupUuid, String userUuid);

	boolean removeUserFromGroup(String groupUuid, String userUuid);
}
