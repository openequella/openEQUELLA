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

package com.tle.web.errors;

import com.dytech.edge.exceptions.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;

@Bind
public class ExternalErrorSection extends DefaultErrorSection implements ParametersEventListener
{
	@SuppressWarnings("nls")
	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		Throwable ex = null;
		String method = event.getParameter("method", false);
		if( method != null )
		{
			if( "notFound".equals(method) )
			{
				ex = new NotFoundException("404", true);
			}
			else if( "accessDenied".equals(method) )
			{
				ex = new AccessDeniedException("403");
			}
			else if( "throwable".equals(method) )
			{
				ex = (Throwable) info.getRequest().getAttribute("javax.servlet.error.exception");
			}
			if( ex != null )
			{
				info.setAttribute(SectionInfo.KEY_ORIGINAL_EXCEPTION, ex);
				info.setAttribute(SectionInfo.KEY_MATCHED_EXCEPTION, ex);
				info.preventGET();
			}
		}
	}
}
