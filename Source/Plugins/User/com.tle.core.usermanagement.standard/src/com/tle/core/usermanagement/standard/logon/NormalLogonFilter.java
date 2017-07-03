package com.tle.core.usermanagement.standard.logon;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.web.WebConstants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.security.TLEAclManager;
import com.tle.common.usermanagement.user.UserState;
import com.tle.plugins.ump.UserManagementLogonFilter;
import com.tle.web.dispatcher.FilterResult;

@Bind
public class NormalLogonFilter implements UserManagementLogonFilter
{
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private InstitutionService institutionService;

	@Override
	public boolean init(Map<Object, Object> attributes)
	{
		return true;
	}

	@Override
	public FilterResult filter(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		return null;
	}

	@Override
	public URI logoutURI(UserState state, URI loggedOutURI)
	{
		// Keep original
		return loggedOutURI;
	}

	@Override
	public URI logoutRedirect(URI loggedOutURI)
	{
		// Do needful here...
		boolean canViewDash = !aclManager.filterNonGrantedPrivileges(WebConstants.DASHBOARD_PAGE_PRIVILEGE).isEmpty();

		if( canViewDash )
		{
			return URI.create(institutionService.institutionalise(WebConstants.DASHBOARD_PAGE));
		}

		return loggedOutURI;
	}

	@Override
	public void addStateParameters(HttpServletRequest request, Map<String, String[]> params)
	{
		// Here be dragons
	}

}
