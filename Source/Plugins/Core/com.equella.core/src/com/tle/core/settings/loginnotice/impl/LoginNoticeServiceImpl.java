package com.tle.core.settings.loginnotice.impl;

import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.exceptions.PrivilegeRequiredException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;

@Singleton
@SuppressWarnings("nls")
@Bind(LoginNoticeService.class)
public class LoginNoticeServiceImpl implements LoginNoticeService
{
	@Inject
	TLEAclManager tleAclManager;
	@Inject
	ConfigurationService configurationService;

	private static final String PERMISSION_KEY = "EDIT_SYSTEM_SETTINGS";
	private static final String LOGIN_NOTICE_KEY = "login.notice.settings";

	@Override
	public String getNotice()
	{
//		checkPermissions();
		String loginNotice = configurationService.getProperty(LOGIN_NOTICE_KEY);
		if(loginNotice!=null){
			return loginNotice;
		}
		return "loginNotice was null";
	}

	@Override
	public void setNotice(String notice)
	{
		checkPermissions();
		configurationService.setProperty(LOGIN_NOTICE_KEY, notice);
	}

	@Override
	public void deleteNotice()
	{
		checkPermissions();
		configurationService.deleteProperty(LOGIN_NOTICE_KEY);
	}

	private void checkPermissions() {
		if (tleAclManager.filterNonGrantedPrivileges(Collections.singleton(PERMISSION_KEY), false).isEmpty()) {
			throw new PrivilegeRequiredException(PERMISSION_KEY);
		}
	}


}
