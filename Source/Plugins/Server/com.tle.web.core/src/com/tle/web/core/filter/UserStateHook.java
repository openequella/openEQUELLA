package com.tle.web.core.filter;

import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.exceptions.WebException;
import com.tle.core.user.UserState;

/**
 * @author Aaron
 */
public interface UserStateHook
{
	UserStateResult getUserState(HttpServletRequest request, UserState userState) throws WebException;

	// stolen from EPS - allow OAuth token to set system user for
	// Institution manipulation
	boolean isInstitutional();
}
