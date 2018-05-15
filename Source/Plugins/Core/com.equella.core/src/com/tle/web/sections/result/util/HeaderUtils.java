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

package com.tle.web.sections.result.util;

import static com.tle.web.sections.render.CssInclude.include;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.CssInclude.Browser;
import com.tle.web.sections.render.CssInclude.CssIncludeBuilder;
import com.tle.web.sections.render.CssInclude.Media;
import com.tle.web.sections.render.CssInclude.Priority;

public class HeaderUtils
{
	private final PreRenderContext info;

	public HeaderUtils(RenderContext info)
	{
		this.info = info.getPreRenderContext();
	}

	public void addCss(String css)
	{
		info.addCss(include(css).make());
	}

	public void addCss(String file, String browser, String media, boolean hasRtl, String priority)
	{
		CssIncludeBuilder cib = include(file).browser(Browser.valueOf(browser)).media(Media.valueOf(media))
			.priority(Priority.valueOf(priority));
		if( hasRtl )
		{
			cib.hasRtl();
		}
		info.addCss(cib.make());
	}

	public void addJs(String file)
	{
		info.addJs(file);
	}
}
