package com.tle.core.services.user;

import java.util.Collection;
import java.util.List;

import com.tle.beans.user.TLEUser;
import com.tle.core.remoting.RemoteTLEUserService;

public interface TLEUserService extends RemoteTLEUserService
{
	boolean checkPasswordMatch(TLEUser user, String password);

	void validatePassword(String password, boolean passwordNotHashed);

	List<TLEUser> getInformationForUsers(Collection<String> ids);

	String edit(String uuid, String username, String password, String first, String last, String email);

	String editSelf(TLEUser user, boolean passwordNotHashed);

	void validate(TLEUser user, boolean passwordNotHashed);

	String prepareQuery(String query);
}