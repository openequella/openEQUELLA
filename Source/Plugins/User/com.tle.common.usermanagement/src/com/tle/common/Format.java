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

package com.tle.common;

import java.util.Comparator;

import com.dytech.common.text.NumberStringComparator;
import com.tle.beans.NameId;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.user.valuebean.RoleBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;

public final class Format
{
	public static final String DEFAULT_USER_BEAN_FORMAT = "%F %L [%U]";
	public static final String UFL = "%U %F %L";
	public static final String FL = "%F %L";

	@Deprecated
	public static String format(UserBean user)
	{
		return format(user, DEFAULT_USER_BEAN_FORMAT);
	}

	@Deprecated
	public static String formatFL(UserBean user)
	{
		return format(user, FL);
	}

	@Deprecated
	public static String formatUFL(UserBean user)
	{
		return format(user, UFL);
	}

	@Deprecated
	public static String format(UserBean user, String format)
	{
		if( user == null )
		{
			return "Unknown user";
		}
		String output = format;
		if( output == null || output.trim().length() == 0 )
		{
			output = DEFAULT_USER_BEAN_FORMAT;
		}

		StringBuilder builder = new StringBuilder();
		int len = output.length();
		for( int i = 0; i < len; i++ )
		{
			char c = output.charAt(i);
			if( c == '%' )
			{
				String s;
				i++;
				if( i < len )
				{
					switch( output.charAt(i) )
					{
						case 'I':
							s = user.getUniqueID();
							break;
						case 'U':
							s = user.getUsername();
							break;
						case 'F':
							s = user.getFirstName();
							break;
						case 'L':
							s = user.getLastName();
							break;
						case 'E':
							s = user.getEmailAddress();
							break;
						case '%':
							s = "%%";
							break;
						default:
							s = "%";
					}
					builder.append(s);
				}
			}
			else
			{
				builder.append(c);
			}
		}

		return builder.toString();
	}

	public static String format(GroupBean group)
	{
		return group.getName();
	}

	public static String format(RoleBean role)
	{
		return role.getName();
	}

	public static final Comparator<String> STRING_COMPARATOR = new NumberStringComparator<String>();

	public static final Comparator<NameValue> NAME_VALUE_COMPARATOR = new NumberStringComparator<NameValue>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String convertToString(NameValue t)
		{
			return t.getName();
		}
	};

	public static final Comparator<NameId> NAME_ID_COMPARATOR = new NumberStringComparator<NameId>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String convertToString(NameId t)
		{
			return t.getName();
		}
	};

	public static final Comparator<UserBean> USER_BEAN_COMPARATOR = new NumberStringComparator<UserBean>()
	{
		private static final long serialVersionUID = 1L;
		// Nothing to do here. UserBean.toString() uses the formating method
		// above,
		// and the list is then ordered by that.
	};

	public static final Comparator<GroupBean> GROUP_BEAN_COMPARATOR = new NumberStringComparator<GroupBean>()
	{
		private static final long serialVersionUID = 1L;
		// Nothing to do here. GroupBean.toString() uses the formating method
		// above,
		// and the list is then ordered by that.
	};

	public static final Comparator<RoleBean> ROLE_BEAN_COMPARATOR = new NumberStringComparator<RoleBean>()
	{
		private static final long serialVersionUID = 1L;
		// Nothing to do here. RoleBean.toString() uses the formating method
		// above,
		// and the list is then ordered by that.
	};

	private Format()
	{
		throw new Error("Do not invoke");
	}
}
