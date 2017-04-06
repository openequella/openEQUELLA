package com.tle.core.remoting;

import java.util.List;

import com.tle.beans.user.TLEUser;

public interface RemoteTLEUserService
{
	String add(TLEUser newUser);

	String add(TLEUser newUser, boolean passwordNotHashed);

	String add(TLEUser newUser, List<String> groups);

	String add(String username, List<String> groups);

	TLEUser get(String id);

	TLEUser getByUsername(String username);

	String edit(TLEUser user, boolean passwordNotHashed);

	void delete(String uuid);

	List<TLEUser> searchUsers(String query, String parentGroupID, boolean recursive);
}