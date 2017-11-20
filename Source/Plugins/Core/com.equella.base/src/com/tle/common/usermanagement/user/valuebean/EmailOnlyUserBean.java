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

package com.tle.common.usermanagement.user.valuebean;

public class EmailOnlyUserBean implements UserBean
{
	private static final long serialVersionUID = 1L;

	private final String id;
	private final String email;

	@SuppressWarnings("nls")
	public EmailOnlyUserBean(String email)
	{
		this.id = "email:" + email;
		this.email = email;
	}

	@Override
	public String getUniqueID()
	{
		return id;
	}

	@Override
	public String getUsername()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getFirstName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLastName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getEmailAddress()
	{
		return email;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof UserBean) )
		{
			return false;
		}

		return id.equals(((UserBean) obj).getUniqueID());
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
