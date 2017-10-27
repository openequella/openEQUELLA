/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.usermanagement.user;

import java.io.Serializable;

import com.tle.common.usermanagement.user.valuebean.UserBean;

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
