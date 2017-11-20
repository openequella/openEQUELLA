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

package com.tle.web.core.servlet;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.i18n.LocaleUtils;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class LanguageServlet extends HttpServlet
{
	@Inject
	private LanguageService languageService;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		final String path = req.getPathInfo();

		if( path.equals("/refresh") )
		{
			languageService.refreshBundles();

			resp.setContentType("text/plain");
			ServletOutputStream out = resp.getOutputStream();
			out.print("Bundles refreshed");
			out.close();
			return;
		}

		Matcher m = Pattern.compile("^/([a-zA-Z_]*)/([a-zA-Z-]+)\\.properties$").matcher(path);
		if( !m.matches() || m.groupCount() != 2 )
		{

			throw new ServletException("Bundle Group name is invalid: " + path);
		}

		final ResourceBundle resourceBundle = languageService.getResourceBundle(LocaleUtils.parseLocale(m.group(1)),
			m.group(2));

		final Properties text = new Properties();
		for( String key : resourceBundle.keySet() )
		{
			text.put(key, resourceBundle.getString(key));
		}

		resp.setContentType("text/plain");
		resp.setHeader("Content-Disposition", "inline; filename=" + path + ".properties");

		text.store(resp.getOutputStream(), null);
	}

}
