package com.tle.core.remoting;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.GroupTreeNode;
import com.tle.beans.user.TLEGroup;

public interface RemoteTLEGroupService
{
	String add(String parentID, String name);

	TLEGroup get(String id);

	TLEGroup getByName(String name);

	String edit(final TLEGroup group);

	void delete(String groupID, boolean deleteChildren);

	List<TLEGroup> search(String query);

	List<TLEGroup> search(String query, String parentId);

	List<TLEGroup> search(String query, String userId, boolean allParents);

	GroupTreeNode searchTree(String query);

	List<TLEGroup> getInformationForGroups(Collection<String> groups);
}