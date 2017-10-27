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

package com.tle.common.usermanagement.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author aholland
 */
public final class UserBeanUtils
{
	private static final Log LOGGER = LogFactory.getLog(UserBeanUtils.class);

	public static NameValue formatUser(UserBean details)
	{
		return new NameValue(Format.format(details), details.getUniqueID());
	}

	public static NameValue formatGroup(GroupBean details)
	{
		return new NameValue(Format.format(details), details.getUniqueID());
	}

	public static NameValue getUser(RemoteUserService userService, String uuid)
	{
		if( uuid == null || uuid.length() == 0 || uuid.equals("0") ) //$NON-NLS-1$
		{
			return new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.nouser"), "");
		}

		NameValue user = null;
		try
		{
			UserBean informationForUser = userService.getInformationForUser(uuid);
			if( informationForUser != null )
			{
				user = formatUser(informationForUser);
			}
		}
		catch( Exception ex )
		{
			LOGGER.warn("Problem looking up user " + uuid, ex);
		}

		if( user == null )
		{
			user = new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.unknownuser", uuid), uuid);
		}

		return user;

	}

	public static NameValue getGroup(RemoteUserService userService, String uuid)
	{
		if( uuid.length() == 0 || uuid.equals("0") ) //$NON-NLS-1$
		{
			return new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.nogroup"), "");
		}
		NameValue group = null;

		try
		{
			GroupBean informationForGroup = userService.getInformationForGroup(uuid);
			if( informationForGroup != null )
			{
				group = formatGroup(informationForGroup);
			}
		}
		catch( Exception ex )
		{
			LOGGER.warn("Problem looking up group " + uuid, ex);
		}

		if( group == null )
		{
			group = new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.unknowngroup", uuid), uuid);
		}

		return group;
	}

	public static NameValue getRole(RemoteUserService userService, String uuid)
	{
		if( uuid.length() == 0 || uuid.equals("0") ) //$NON-NLS-1$
		{
			return new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.norole"), "");
		}

		NameValue role = null;

		try
		{
			RoleBean informationForRole = userService.getInformationForRole(uuid);
			if( informationForRole != null )
			{
				role = new NameValue(Format.format(informationForRole), informationForRole.getUniqueID());
			}
		}
		catch( Exception ex )
		{
			LOGGER.warn("Problem looking up group " + uuid, ex);
		}

		if( role == null )
		{
			role = new NameValue(CurrentLocale.get("com.dytech.edge.admin.helper.utils.unknownrole", uuid), uuid);
		}

		return role;
	}

	private UserBeanUtils()
	{
		throw new Error();
	}
}
