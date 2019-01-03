/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.render;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderResultListener;

public class OutputResultListener implements RenderResultListener
{
	private RenderContext info;

	public OutputResultListener(RenderContext info)
	{
		this.info = info;
	}

	@Override
	@SuppressWarnings("nls")
	public void returnResult(SectionResult result, String fromId)
	{
		if( result != null )
		{
			HttpServletResponse response = info.getResponse();
			info.setRendered();
			if( result instanceof SectionRenderable )
			{
				response.setContentType("text/html");
				response.setHeader("Cache-Control", "no-cache, no-store");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
				response.setCharacterEncoding("UTF-8");

				SectionRenderable renderable = (SectionRenderable) result;
				try( SectionWriter writer = new SectionWriter(response.getWriter(), info) )
				{
					writer.render(renderable);
				}
				catch( IOException e )
				{
					SectionUtils.throwRuntime(e);
				}
			}
		}
	}
}
