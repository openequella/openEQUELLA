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

package com.tle.web.discoverability.scripting.impl;

import org.apache.commons.lang.StringEscapeUtils;

import com.tle.web.discoverability.scripting.objects.MetaScriptObject;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author aholland
 */
public class MetaScriptWrapper implements MetaScriptObject
{
	private static final long serialVersionUID = 1L;
	private PreRenderContext render;

	public MetaScriptWrapper(PreRenderContext render)
	{
		this.render = render;
	}

	@SuppressWarnings("nls")
	@Override
	public void add(String name, String content)
	{
		StringBuilder tag = new StringBuilder();
		tag.append("<meta name=\"");
		tag.append(StringEscapeUtils.escapeHtml(name));
		tag.append("\" content=\"");
		tag.append(StringEscapeUtils.escapeHtml(content));
		tag.append("\">\n");
		render.addHeaderMarkup(tag.toString());
	}

	@Override
	public void scriptEnter()
	{
		// Nothing by default
	}

	@Override
	public void scriptExit()
	{
		// Nothing by default
	}

}