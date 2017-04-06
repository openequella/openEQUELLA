/*
 * Created on Feb 17, 2005
 */
package com.tle.core.user;

import java.io.Serializable;

import com.dytech.edge.common.valuebean.UserBean;

/**
 * @author Nicholas Read
 */
public class AuthenticatedUser implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final UserBean user;
	private int role;

	public AuthenticatedUser(UserBean user, int role)
	{
		this.user = user;
		this.role = role;
	}

	/**
	 * @return Returns the role.
	 */
	public int getRole()
	{
		return role;
	}

	public void setRole(int role)
	{
		this.role = role;
	}

	/**
	 * @return Returns the user.
	 */
	public UserBean getUser()
	{
		return user;
	}
}
