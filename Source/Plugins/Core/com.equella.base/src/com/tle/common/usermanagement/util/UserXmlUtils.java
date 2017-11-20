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

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean;
import com.tle.common.usermanagement.user.valuebean.UserBean;

public class UserXmlUtils
{
	/**
	 * Returns the user as legacy XML.
	 * 
	 * @param user the user's details.
	 * @return A PropBag rooted at 'user'.
	 */
	public static PropBagEx getUserAsXml(UserBean user)
	{
		PropBagEx xml = new PropBagEx().newSubtree("user"); //$NON-NLS-1$
		if( user != null )
		{
			xml.setNode("@id", user.getUniqueID()); //$NON-NLS-1$
			xml.setNode("username", user.getUsername()); //$NON-NLS-1$
			xml.setNode("givenname", user.getFirstName()); //$NON-NLS-1$
			xml.setNode("surname", user.getLastName()); //$NON-NLS-1$
			if( !Check.isEmpty(user.getEmailAddress()) )
			{
				xml.setNode("email", user.getEmailAddress()); //$NON-NLS-1$
			}
		}
		return xml;
	}

	public static UserBean getUserAsBean(PropBagEx userXml)
	{
		PropBagEx xml = userXml;
		if( xml.nodeExists("user") ) //$NON-NLS-1$
		{
			xml = userXml.getSubtree("user"); //$NON-NLS-1$
		}
		String userID = xml.getNode("@id"); //$NON-NLS-1$
		String username = xml.getNode("username"); //$NON-NLS-1$
		String surname = xml.getNode("surname"); //$NON-NLS-1$
		String givenname = xml.getNode("givenname"); //$NON-NLS-1$
		String email = xml.getNode("email"); //$NON-NLS-1$
		return new DefaultUserBean(userID, username, givenname, surname, email);
	}

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	protected UserXmlUtils()
	{
		// not to be instantiated, hence nothing to construct except a token
		// hidden constructor to silence Sonar
	}
}
