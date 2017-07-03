package com.tle.beans.usermanagement.standard.user;

import com.tle.beans.Institution;
import com.tle.common.usermanagement.user.AbstractUserState;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;

/**
 * @author Nicholas Read
 */
public class SharePassUserState extends AbstractUserState
{
	private static final long serialVersionUID = 1L;

	public SharePassUserState(Institution institution, String email, String token)
	{
		setInstitution(institution);
		setSharePassEmail(email);
		setToken(token);
		setLoggedInUser(new DefaultUserBean(email, email, email, email, email));
		setAuthenticated(true);
	}
}
