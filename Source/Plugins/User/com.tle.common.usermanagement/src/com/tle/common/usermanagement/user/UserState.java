/*
 * Created on Mar 17, 2005
 */
package com.tle.common.usermanagement.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import com.tle.beans.Institution;
import com.tle.common.usermanagement.user.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public interface UserState extends Cloneable, Serializable
{
	String getSessionID();

	UserBean getUserBean();

	Institution getInstitution();

	Set<String> getUsersGroups();

	Set<String> getUsersRoles();

	String getIpAddress();

	String getHostAddress();

	String getHostReferrer();

	String getSharePassEmail();

	String getToken();

	String getTokenSecretId();

	Collection<Long> getCommonAclExpressions();

	Collection<Long> getOwnerAclExpressions();

	Collection<Long> getNotOwnerAclExpressions();

	boolean isGuest();

	boolean isSystem();

	boolean isInternal();

	boolean wasAutoLoggedIn();

	void setWasAutoLoggedIn(boolean b);

	boolean isAuthenticated();

	void setAuthenticated(boolean b);

	boolean isAuditable();

	void setAuditable(boolean b);

	boolean isNeedsSessionUpdate();

	UserState clone(); // NOSONAR - instance class attends to exception

	void updatedInSession();

	<T> T getCachedAttribute(Object key);

	void setCachedAttribute(Object key, Object value);

	void removeCachedAttribute(Object key);
}
