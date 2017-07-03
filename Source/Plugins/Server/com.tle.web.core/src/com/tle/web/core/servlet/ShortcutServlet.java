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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.settings.standard.ShortcutUrls;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;

@Bind
@Singleton
public class ShortcutServlet extends HttpServlet
{
	@Inject
	private ConfigurationService configService;

	public ShortcutServlet()
	{
		super();
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String shortcut = request.getPathInfo().substring(1);
		String url = configService.getProperties(new ShortcutUrls()).getShortcuts().get(shortcut);
		if( url == null )
		{
			throw new NotFoundException("Shortcut '" + shortcut + "' does not exist", true);
		}
		response.sendRedirect(url);
	}
}
