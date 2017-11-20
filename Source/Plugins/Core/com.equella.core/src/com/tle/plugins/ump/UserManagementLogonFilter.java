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
