package com.tle.core.services.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public interface UserSessionService
{
	@Nullable
	<T> T getAttribute(String key);

	/**
	 * @param attribute WARNING: This object should be immutable!
	 */
	void setAttribute(String key, Object attribute);

	void removeAttribute(String key);

	String createUniqueKey();

	boolean isSessionPrevented();

	void preventSessionUse();

	void bindRequest(HttpServletRequest request);

	void unbind();

	void nudgeSession();

	void forceSession();

	Iterable<UserSessionTimestamp> getInstitutionSessions();

	@Nullable
	<T> T getAttributeFromSession(HttpSession session, Institution institution, String attribute);

	HttpServletRequest getAssociatedRequest();

	boolean isSessionAvailable();

	Object getSessionLock();
}
