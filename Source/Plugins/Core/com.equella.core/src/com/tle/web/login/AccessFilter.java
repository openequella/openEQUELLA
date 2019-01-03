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

package com.tle.web.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.web.WebConstants;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;

@Bind
@Singleton
public class AccessFilter implements SectionFilter
{

	@Override
	public void filter(MutableSectionInfo info)
	{
		if( info.getAttribute(SectionInfo.KEY_FORWARDFROM) != null || info.getRequest() == null
			|| !info.getBooleanAttribute(SectionInfo.KEY_FROM_REQUEST) )
		{
			return;
		}
		final UserState userState = CurrentUser.getUserState();
		boolean guest = userState == null || userState.isGuest();
		String path = info.getAttribute(SectionInfo.KEY_PATH);
		if( guest )
		{
			if( path.startsWith(WebConstants.ACCESS_PATH) || path.equals(WebConstants.SIGNON_PATH) )
			{
				forwardIt(info, path, LogonSection.STANDARD_LOGON_PATH);
			}
		}
	}

	@SuppressWarnings("nls")
	protected void forwardIt(SectionInfo info, String path, String loginPath)
	{
		String url = null;

		HttpServletRequest request = info.getRequest();
		Map<Object, Object> params = new HashMap<Object, Object>(request.getParameterMap());
		params.remove("token");

		url = path.substring(1);
		List<NameValue> nvs = SectionUtils.getParameterNameValues(params, false);
		if( !Check.isEmpty(nvs) )
		{
			url += '?' + SectionUtils.getParameterString(nvs);
		}
		LogonSection.forwardToLogon(info, url, loginPath);
	}
}
