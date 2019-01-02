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

package com.tle.web.errors;

import javax.servlet.http.HttpServletRequest;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;

public abstract class AbstractExceptionHandler implements SectionsExceptionHandler
{
	protected Throwable getFirstCause(Throwable ex)
	{
		if( ex == null )
		{
			return null;
		}
		if( ex.getCause() == null )
		{
			return ex;
		}
		return getFirstCause(ex.getCause());
	}

	@Override
	public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		return !hasAlreadyBeenHandled(info, ex, event);
	}

	protected boolean hasAlreadyBeenHandled(SectionInfo info, Throwable ex, SectionEvent<?> event)
	{
		HttpServletRequest request = info.getRequest();
		if( request == null )
		{
			return true;
		}
		else
		{
			return (request.getAttribute(getClass().getName()) != null);
		}
	}

	protected void markHandled(SectionInfo info)
	{
		info.getRequest().setAttribute(getClass().getName(), true);
	}
}
