package com.tle.core.institution;

import java.util.concurrent.Callable;

import com.tle.beans.Institution;
import com.tle.core.user.UserState;

/**
 * @author Nicholas Read
 */
public interface RunAsInstitution
{
	<V> V execute(UserState userState, Callable<V> callable);

	void executeAsSystem(Institution institution, Runnable runnable);

	<V> V executeAsSystem(Institution institution, Callable<V> callable);
}
