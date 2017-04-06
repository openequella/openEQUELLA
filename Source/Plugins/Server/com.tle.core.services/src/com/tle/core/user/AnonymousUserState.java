package com.tle.core.user;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.tle.common.security.SecurityConstants;

/**
 * @author Nicholas Read
 */
public class AnonymousUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("nls")
	public AnonymousUserState()
	{
		setLoggedInUser(new DefaultUserBean("guest", "guest", "guest", "guest", "guest"));
		getUsersRoles().add(SecurityConstants.GUEST_USER_ROLE_ID);
	}

	@Override
	public boolean isGuest()
	{
		return true;
	}

	@Override
	public boolean isAuditable()
	{
		return false;
	}
}
