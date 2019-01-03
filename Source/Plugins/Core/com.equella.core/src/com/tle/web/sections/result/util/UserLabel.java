/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.result.util;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.LazyUserLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;

public class UserLabel implements Label
{
	private static final String USER_SYSTEM = "system"; //$NON-NLS-1$

	static
	{
		PluginResourceHandler.init(UserLabel.class);
	}

	@PlugKey("userlabel.unknown")
	private static String KEY_UNKNOWN;
	@PlugKey("userlabel.format")
	private static String KEY_FORMAT;
	@PlugKey("userlink.systemuser")
	private static String KEY_SYSTEMUSER;

	private String userid;
	private LazyUserLookup lazyLookup;

	public UserLabel(String userid, LazyUserLookup lazyLookup)
	{
		this.userid = userid;
		this.lazyLookup = lazyLookup;
		lazyLookup.addUser(userid);
	}

	@Override
	public String getText()
	{
		UserBean userBean = lazyLookup.get(userid);
		if( userBean == null )
		{
			if( userid.equals(USER_SYSTEM) )
			{
				return CurrentLocale.get(KEY_SYSTEMUSER);
			}
			return CurrentLocale.get(KEY_UNKNOWN, userid);
		}
		return CurrentLocale.get(KEY_FORMAT, userBean.getFirstName(), userBean.getLastName(), userBean.getUsername(),
			userid);
	}

	@Override
	public boolean isHtml()
	{
		return false;
	}

}
