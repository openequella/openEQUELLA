package com.tle.core.dao.user;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.TLEUser;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface TLEUserDao extends GenericDao<TLEUser, Long>
{
	long totalExistingUsers();

	List<TLEUser> listAllUsers();

	List<TLEUser> searchUsersInGroup(String likeQuery, String parentGroupID, boolean recurse);

	void deleteAll();

	TLEUser findByUuid(String uuid);

	/**
	 * Warning - this method will return a user for any username case -
	 * upper/lower/mixed/whatever.
	 */
	TLEUser findByUsername(String username);

	boolean doesOtherUsernameSameSpellingExist(String username, String userUuid);

	List<TLEUser> getInformationForUsers(Collection<String> ids);
}
