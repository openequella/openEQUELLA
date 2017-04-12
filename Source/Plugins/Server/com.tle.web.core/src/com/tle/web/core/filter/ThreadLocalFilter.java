package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.language.impl.ServerSideLocaleImplementation;
import com.tle.core.services.language.impl.ServerSideTimeZoneImplementation;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.i18n.BundleCache;

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
		tracker = new PluginTracker<ThreadLocalExtension>(pluginService, ThreadLocalFilter.class, "threadLocal", "id");
		tracker.setBeanKey("bean");
	}
}
