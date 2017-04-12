package com.tle.core.services.user;

import java.util.List;

import com.tle.beans.user.TLEGroup;
import com.tle.core.remoting.RemoteTLEGroupService;

public interface TLEGroupService extends RemoteTLEGroupService
{
	String add(TLEGroup group);

	TLEGroup createGroup(String groupID, String name);

	List<String> getUsersInGroup(String parentGroupID, boolean recurse);

	List<TLEGroup> getGroupsContainingUser(String userID, boolean recursive);

	void addUserToGroup(String groupUuid, String userUuid);

	void removeUserFromGroup(String groupUuid, String userUuid);

	void removeAllUsersFromGroup(String groupUuid);

	String prepareQuery(String searchString);
}