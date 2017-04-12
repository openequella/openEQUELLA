package com.tle.core.user;

import java.util.Collection;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.Institution;
import com.tle.common.Triple;

/**
 * @author Nicholas Read
 */
public interface ModifiableUserState extends UserState
{
	void setSessionID(String sessionID);

	void setInstitution(Institution institution);

	void setAclExpressions(Triple<Collection<Long>, Collection<Long>, Collection<Long>> expression);

	void setIpAddress(String ipAddress);

	void setHostAddress(String hostAddress);

	void setHostReferrer(String hostReferrer);

	void setSharePassEmail(String email);

	void setToken(String token);

	void setTokenSecretId(String tokenSecretId);

	void setLoggedInUser(UserBean user);

}
