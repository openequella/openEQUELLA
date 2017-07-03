/*
 * Created on Feb 17, 2005
 */
package com.tle.common.usermanagement.user.valuebean;

import java.io.Serializable;

/**
 * @author adame
 */
public interface UserBean extends Serializable
{
	/**
	 * @return a unique, unchanging ID for the user
	 */
	String getUniqueID();

	/**
	 * @return a username for the user
	 */
	String getUsername();

	/**
	 * @return the first name for the user
	 */
	String getFirstName();

	/**
	 * @return the last name for the user
	 */
	String getLastName();

	/**
	 * @return the email address for the user
	 */
	String getEmailAddress();
}
