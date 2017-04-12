package com.tle.core.security;

import java.util.concurrent.Callable;

import com.tle.beans.Institution;
import com.tle.core.user.UserState;
import com.tle.core.user.WebAuthenticationDetails;

/**
 * @author Nicholas Read
 */
public interface RunAsUser
{
	<V> V execute(Institution institution, String userID, Callable<V> callable);

	void execute(Institution institution, String userID, Runnable runnable);

	void execute(Institution institution, UserState userState, Runnable runnable);

	void executeAsGuest(Institution institution, Runnable runnable, WebAuthenticationDetails details);
}
