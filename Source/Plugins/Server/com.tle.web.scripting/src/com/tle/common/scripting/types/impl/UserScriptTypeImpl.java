package com.tle.common.scripting.types.impl;

import com.dytech.edge.common.valuebean.DefaultUserBean;
import com.dytech.edge.common.valuebean.UserBean;
import com.tle.common.scripting.types.UserScriptType;

/**
 * @author aholland
 */
public class UserScriptTypeImpl extends DefaultUserBean implements UserScriptType
{
	public UserScriptTypeImpl(UserBean user)
	{
		super(user.getUniqueID(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmailAddress());
	}
}
