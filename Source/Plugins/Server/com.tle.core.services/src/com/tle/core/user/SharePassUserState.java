package com.tle.core.user;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.tle.beans.Institution;

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
