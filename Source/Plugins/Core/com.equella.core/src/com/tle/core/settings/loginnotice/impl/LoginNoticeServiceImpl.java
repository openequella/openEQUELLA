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

package com.tle.core.settings.loginnotice.impl;

import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;
import org.apache.cxf.common.util.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

@Singleton
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService
{
	@Inject
	TLEAclManager tleAclManager;
	@Inject
	ConfigurationService configurationService;

	private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
	private static final String PRE_LOGIN_NOTICE_KEY = "pre.login.notice";
	private static final String POST_LOGIN_NOTICE_KEY = "post.login.notice";

	@Override
	public String getPreLoginNotice()
	{
		return configurationService.getProperty(PRE_LOGIN_NOTICE_KEY);
	}

	@Override
	public void setPreLoginNotice(String notice)
	{
		checkPermissions();
		if(StringUtils.isEmpty(notice)) {
			configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
		} else {
			configurationService.setProperty(PRE_LOGIN_NOTICE_KEY, notice);
		}
	}

	@Override
	public void deletePreLoginNotice()
	{
		checkPermissions();
		configurationService.deleteProperty(PRE_LOGIN_NOTICE_KEY);
	}

	@Override
	public String getPostLoginNotice()
	{
		return configurationService.getProperty(POST_LOGIN_NOTICE_KEY);
	}

	@Override
	public void setPostLoginNotice(String notice)
	{
		checkPermissions();
		if(StringUtils.isEmpty(notice)) {
			configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
		} else {
			configurationService.setProperty(POST_LOGIN_NOTICE_KEY, notice);
		}
	}

	@Override
	public void deletePostLoginNotice()
	{
		checkPermissions();
		configurationService.deleteProperty(POST_LOGIN_NOTICE_KEY);
	}

	private void checkPermissions() {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
	}
}
