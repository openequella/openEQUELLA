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

package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.ServerSideLocaleImplementation;
import com.tle.core.i18n.ServerSideTimeZoneImplementation;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.dispatcher.FilterResult;

@Bind
@Singleton
public class ThreadLocalFilter extends OncePerRequestFilter
{
	@Inject
	private BundleCache bundleCache;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private ServerSideLocaleImplementation serverSideLocaleImplementation;
	@Inject
	private ServerSideTimeZoneImplementation serverSideTimeZoneImplementation;

	private PluginTracker<ThreadLocalExtension> tracker;

	@Override
	protected FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		CurrentUser.setUserState(null);
		CurrentInstitution.remove();
		bundleCache.reset();
		userSessionService.unbind();
		serverSideLocaleImplementation.clearThreadLocals();
		serverSideTimeZoneImplementation.clearThreadLocals();
		for( ThreadLocalExtension tle : tracker.getBeanList() )
		{
			tle.doFilter(request, response);
		}
		return FilterResult.FILTER_CONTINUE;
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<ThreadLocalExtension>(pluginService, "com.tle.web.core", "threadLocal", "id");
		tracker.setBeanKey("bean");
	}
}
