package com.tle.core.user;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
public class SystemUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("nls")
	public SystemUserState(Institution institution)
	{
		this(institution, new DefaultUserBean("system", "system", "system", "system", "system"));
	}

	@SuppressWarnings("nls")
	public SystemUserState(Institution institution, UserBean userBean)
	{
		setLoggedInUser(userBean);
		setInstitution(institution);
		setSessionID("SYSTEM");
		setAuthenticated(true);
	}

	@Override
	public boolean isSystem()
	{
		return true;
	}
}
