/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.DebugSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.registry.SectionsServlet;

@Bind
@MultipartConfig
public class MultipartSectionsServlet extends SectionsServlet
{
	private final Map<Object,Object> attribs;

	public MultipartSectionsServlet()
	{
		super();
		attribs = new HashMap<>();
		attribs.putAll(super.defaultAttributes());
		attribs.put(SectionInfo.KEY_MINIFIED, !DebugSettings.isDebuggingMode());
	}


	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		super.service(request, response);
	}

	@Override
	protected Map<Object, Object> defaultAttributes()
	{
		return attribs;
	}
}
