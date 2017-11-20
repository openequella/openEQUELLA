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

package com.tle.web.institution;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dytech.edge.web.WebConstants;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.URLUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.CurrentDataSource;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.institution.InstitutionStatus.InvalidReason;
import com.tle.core.institution.events.InstitutionEvent;
import com.tle.core.institution.events.listeners.InstitutionListener;
import com.tle.core.services.UrlService;
import com.tle.core.system.service.SchemaDataSourceService;
import com.tle.web.core.filter.OncePerRequestFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.RemappedRequest;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class InstitutionFilter extends OncePerRequestFilter implements InstitutionListener
{
	@Inject
	private InstitutionService institutionService;
	@Inject
	private SchemaDataSourceService databaseSchemaService;

	// Ugh, these should be pluginerised
	private static final String[] ALLOWED_URL_BEGINNINGS = {"progress", "configurable"};
	private static final String[] ALLOWED_URL_ENDINGS = {".css", ".js", ".jpeg", ".jpg", ".gif", ".png", ".ico", ".zip",
			".war", ".jar", ".woff",};

	private static final Logger LOGGER = Logger.getLogger(InstitutionFilter.class);

	private String adminUrl;

	private Collection<Pair<String, InstitutionStatus>> institutions;

	private Collection<Pair<String, InstitutionStatus>> getInstitutions()
	{
		if( institutions == null )
		{
			final List<Pair<String, InstitutionStatus>> pairedInsts = Lists.newArrayList();
			final Collection<InstitutionStatus> enabledInsts = institutionService.getAllInstitutions();

			for( final InstitutionStatus instStatus : enabledInsts )
			{
				Institution inst = instStatus.getInstitution();
				if( inst.isEnabled()
					&& (instStatus.isValid() || instStatus.getInvalidReason() != InvalidReason.INVALID) )
				{
					pairedInsts.add(new Pair<String, InstitutionStatus>(processUrl(inst.getUrl()), instStatus));
				}
			}

			// Add the Institution Management site in too
			pairedInsts.add(
				new Pair<String, InstitutionStatus>(processUrl(adminUrl), new InstitutionStatus(Institution.FAKE, -1)));

			// Sort largest URLs first
			Collections.sort(pairedInsts, new Comparator<Pair<String, InstitutionStatus>>()
			{
				@Override
				public int compare(Pair<String, InstitutionStatus> o1, Pair<String, InstitutionStatus> o2)
				{
					return o2.getFirst().length() - o1.getFirst().length();
				}
			});

			institutions = pairedInsts;
		}
		return institutions;
	}

	private String processUrl(String url)
	{
		// Remove ending slash to allow matching paths nicer for users
		if( url.endsWith("/") )
		{
			url = url.substring(0, url.length() - 1);
		}
		return removeProtocol(url);
	}

	private String removeProtocol(String url)
	{
		return url.substring(url.indexOf(':'));
	}

	private Pair<String, Pair<String, InstitutionStatus>> getInstitutionForUrl(Set<String> matchStrings)
	{
		for( Pair<String, InstitutionStatus> urlInst : getInstitutions() )
		{
			String instUrl = urlInst.getFirst();
			int instUrlLen = instUrl.length();
			for( String matchString : matchStrings )
			{
				if( matchString.startsWith(instUrl)
					&& (matchString.length() == instUrlLen || matchString.charAt(instUrlLen) == '/') )
				{
					return new Pair<String, Pair<String, InstitutionStatus>>(matchString, urlInst);
				}
			}
		}
		return null;
	}

	@Override
	protected FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		final Set<String> matchStrings = getMatchStrings(request);
		final Pair<String, Pair<String, InstitutionStatus>> matchInstPair = getInstitutionForUrl(matchStrings);

		// I'm not sure this is ever going to be null ...?
		if( matchInstPair == null )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("No institutions were matched for request [" + request.getRequestURL()
					+ "] where it was matching as [" + Joiner.on("] or [").join(matchStrings)
					+ "] against enabled institutions with URLs [" + Joiner.on("] and [")
						.join(Collections2.transform(getInstitutions(), new Function<Pair<String, ?>, String>()
						{
							@Override
							public String apply(Pair<String, ?> input)
							{
								return input.getFirst();
							}
						}))
					+ "]");
			}

			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return FilterResult.FILTER_CONTINUE;
		}
		else
		{
			// We're accessing the admin URL ...
			return doNormal(request, response, matchInstPair);
		}
	}

	private Set<String> getMatchStrings(HttpServletRequest request) throws MalformedURLException
	{
		URL requestedUrl = new URL(request.getRequestURL().toString());
		int port = requestedUrl.getPort();
		String portPart = port != -1 ? ":" + port : "";
		String context = request.getContextPath();

		// We need to get the requestURI (which Tomcat does not decoded) and
		// decode it ourselves so that plus symbols in paths are correctly
		// interpreted as white space. Calling getPathInfo or getServletPath for
		// /x+x.gif and /x%2Bx.gif returns the same unencoded form of /x+x.gif,
		// which is really bad!
		String requestURI = URLUtils.basicUrlDecode(request.getRequestURI().substring(context.length()));
		Set<String> matchStrings = Sets.newHashSet("://" + requestedUrl.getHost() + portPart + context + requestURI);

		// Some browsers strip off any redundant 443 if https protocol
		// specified, or 80 if http specified
		if( port == -1 )
		{
			String protocol = requestedUrl.getProtocol();
			if( protocol.equals("https") )
			{
				matchStrings.add("://" + requestedUrl.getHost() + ":443" + context + requestURI);
			}
			else
			{
				matchStrings.add("://" + requestedUrl.getHost() + ":80" + context + requestURI);
			}
		}
		return matchStrings;
	}

	private FilterResult doNormal(HttpServletRequest request, HttpServletResponse response,
		Pair<String, Pair<String, InstitutionStatus>> matchInstPair) throws IOException
	{
		String matchedUrl = matchInstPair.getFirst();
		Pair<String, InstitutionStatus> instPair = matchInstPair.getSecond();
		InstitutionStatus instStatus = instPair.getSecond();
		Institution institution = instStatus.getInstitution();
		String instUrl = instPair.getFirst();
		// add 1 for the trailing slash
		int instUrlLength = instUrl.length() + 1;
		String path = null;

		if( institution == null || Institution.FAKE.equals(institution) )
		{
			CurrentInstitution.remove();
			CurrentDataSource.remove();

			path = matchedUrl.substring(instUrlLength);

			if( path.equals("crossdomain.xml") )
			{
				response.sendError(404);
			}
			else
			{
				boolean noRedir = isValidUrlStart(path) || isValidUrlEnding(path) || path.equals("invoke.heartbeat")
					|| path.equals("migration.do") || path.startsWith("api/");
				if( !noRedir && !path.equals("institutions.do") )
				{
					response.sendRedirect(adminUrl + "institutions.do");
					return FilterResult.FILTER_CONTINUE;
				}
			}
		}
		else
		{
			// URL may be shorter than institution URL length if it doesn't
			// have a finishing slash
			path = matchedUrl.length() <= instUrlLength ? "" : matchedUrl.substring(instUrlLength);
			if( !instStatus.isValid()
				&& (!isValidUrlEnding(path) && !isValidUrlStart(path) && !path.equals("unlicensed.do")) )
			{
				response.sendRedirect(institution.getUrl() + "unlicensed.do");
				return FilterResult.FILTER_CONTINUE;
			}
			else
			{
				if( Check.isEmpty(path) )
				{
					path = WebConstants.DEFAULT_HOME_PAGE;
				}
			}
			CurrentInstitution.set(institution);
			CurrentDataSource.set(databaseSchemaService.getDataSourceForId(instStatus.getSchemaId()));
		}
		String contextPath = "";
		int contextStart = instUrl.indexOf('/', 3);
		if( contextStart != -1 )
		{
			contextPath = instUrl.substring(contextStart);
		}
		String servletPath = '/' + path;
		return new FilterResult(RemappedRequest.wrap(request, contextPath, servletPath, request.getPathInfo()));
	}

	private boolean isValidUrlStart(String path)
	{
		for( String s : ALLOWED_URL_BEGINNINGS )
		{
			if( path.startsWith(s) )
			{
				return true;
			}
		}
		return false;
	}

	private boolean isValidUrlEnding(String path)
	{
		for( String s : ALLOWED_URL_ENDINGS )
		{
			if( path.endsWith(s) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void institutionEvent(InstitutionEvent event)
	{
		institutions = null;
	}

	// // SPRING //////////////////////////////////////////////////////////////

	@Inject
	public void setUrlService(UrlService urlService)
	{
		this.adminUrl = urlService.getAdminUrl().toString();
	}
}
