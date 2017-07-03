/*
 * Created on Mar 10, 2005
 */
package com.tle.plugins.ump;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.usermanagement.user.UserState;
import com.tle.web.dispatcher.FilterResult;

/**
 * @author nread
 */
public interface UserManagementLogonFilter
{

	boolean init(Map<Object, Object> attributes);

	FilterResult filter(HttpServletRequest request, HttpServletResponse response) throws IOException;

	URI logoutURI(UserState state, URI loggedOutURI);

	URI logoutRedirect(URI loggedOutURI);

	void addStateParameters(HttpServletRequest request, Map<String, String[]> params);
}
