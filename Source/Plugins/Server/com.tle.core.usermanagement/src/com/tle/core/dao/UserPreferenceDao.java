package com.tle.core.dao;

import java.util.List;
import java.util.Set;

import com.tle.beans.UserPreference;
import com.tle.core.hibernate.dao.GenericDao;

public interface UserPreferenceDao extends GenericDao<UserPreference, UserPreference.UserPrefKey>
{
	void deleteAll();

	List<UserPreference> enumerateAll();

	Set<String> getReferencedUsers();

	void transferUserId(final String fromUserId, final String toUserId);
}
