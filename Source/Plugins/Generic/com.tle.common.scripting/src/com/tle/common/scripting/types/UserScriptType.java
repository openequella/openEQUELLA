package com.tle.common.scripting.types;

import com.dytech.edge.common.valuebean.UserBean;

/**
 * User object for usage in scripts.
 */
public interface UserScriptType extends UserBean
{
	/**
	 * @return A unique, unchanging ID for the user
	 */
	@Override
	String getUniqueID();

	/**
	 * @return The login name of the user
	 */
	@Override
	String getUsername();

	/**
	 * @return The first name of the user
	 */
	@Override
	String getFirstName();

	/**
	 * @return The last name of the user
	 */
	@Override
	String getLastName();

	/**
	 * @return The email address of the user
	 */
	@Override
	String getEmailAddress();
}
