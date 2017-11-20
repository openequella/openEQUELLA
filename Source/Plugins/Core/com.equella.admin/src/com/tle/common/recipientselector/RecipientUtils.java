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

package com.tle.common.recipientselector;

import static com.tle.common.security.SecurityConstants.getRecipient;
import static com.tle.common.security.SecurityConstants.Recipient.GROUP;
import static com.tle.common.security.SecurityConstants.Recipient.HTTP_REFERRER;
import static com.tle.common.security.SecurityConstants.Recipient.IP_ADDRESS;
import static com.tle.common.security.SecurityConstants.Recipient.ROLE;
import static com.tle.common.security.SecurityConstants.Recipient.USER;

import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;

public final class RecipientUtils
{
	public static String convertToRecipient(Object object)
	{
		RecipientFilter filter = null;
		if( object instanceof UserBean )
		{
			filter = RecipientFilter.USERS;
		}
		else if( object instanceof GroupBean )
		{
			filter = RecipientFilter.GROUPS;
		}
		else if( object instanceof RoleBean )
		{
			filter = RecipientFilter.ROLES;
		}
		else
		{
			throw new IllegalStateException();
		}

		return convertToRecipient(filter, object);
	}

	public static String convertToRecipient(RecipientFilter filter, Object object)
	{
		switch( filter )
		{
			case USERS:
				UserBean ub = (UserBean) object;
				return getRecipient(USER, ub.getUniqueID());

			case GROUPS:
				GroupBean gb = (GroupBean) object;
				return getRecipient(GROUP, gb.getUniqueID());

			case ROLES:
				RoleBean role = (RoleBean) object;
				return getRecipient(ROLE, role.getUniqueID());

			case IP_ADDRESS:
				String address = (String) object;
				return getRecipient(IP_ADDRESS, address);

			case HOST_REFERRER:
				String referrer = (String) object;
				return getRecipient(HTTP_REFERRER, referrer);

			case EXPRESSION:
				return (String) object;

			default:
				throw new IllegalStateException();
		}
	}

	private RecipientUtils()
	{
		throw new Error();
	}
}
