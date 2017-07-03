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

package com.tle.web.lti.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.WebException;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.sections.equella.annotation.PlugKey;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LtiProviderServlet extends HttpServlet
{
	@PlugKey("redirect.lti.missing.param.")
	private static String ERROR_PREFIX;

	@Inject
	private InstitutionService institutionService;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String customParam = req.getParameter("custom_equella_url");
		if( !Check.isEmpty(customParam) )
		{
			customParam = customParam.startsWith("/") ? customParam.substring(1) : customParam;
			resp.sendRedirect(institutionService.institutionalise(customParam));
		}
		else
		{
			// HTTP 400 Bad request - missing parameter
			throw new WebException(400, CurrentLocale.get(ERROR_PREFIX + "error"),
				CurrentLocale.get(ERROR_PREFIX + "message"));
		}
	}
}
