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

package com.tle.web.selection.section;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.selection.SelectionExceptionHandler;

/**
 * This handler specialised in order to attend to all variety of exceptions, so
 * long as they're wrapped in RootSelectionException instance.
 * 
 * @author larry
 */
@Bind
@Singleton
public class RootSelectionExceptionHandler extends SelectionExceptionHandler
{
	private static Log LOGGER = LogFactory.getLog(RootSelectionExceptionHandler.class);

	/**
	 * @see com.tle.web.sections.errors.SectionsExceptionHandler#handle(java.lang.Throwable,
	 *      com.tle.web.sections.SectionInfo,
	 *      com.tle.web.sections.SectionsController,
	 *      com.tle.web.sections.events.SectionEvent)
	 */
	@SuppressWarnings("nls")
	@Override
	public void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event)
	{
		LOGGER.error("Error during selection session request", exception);
		super.handle(exception, info, controller, null);
		HttpServletResponse response = info.getResponse();
		if( !response.isCommitted() )
		{
			response.reset();
			response.setStatus(500);
			response.setHeader("Content-Type", "text/html");
			try
			{
				String errMsg = "An error occurred on the server when invoking the request: " + exception.getMessage();
				response.getWriter().print(errMsg);
				// Some sort of formatted error message ...?
				// response.getWriter().append("An error : " +
				// exception.getMessage());
				// Some sort of redirect ...?
				// response.sendRedirect(....);
				// Sufficient for Moodle19, but Moodle22 & 23 don't present the
				// server error page ...?
				// response.sendError(500, exception.getMessage());
				// An issue remains with getting an error message to be
				// presented to the iframe for the moodle file-picker session.
				// Flushing and closing doesn't seem to make any difference ...
				response.getWriter().flush();
				response.getWriter().close();
			}
			catch( IOException e )
			{
				throw new SectionsRuntimeException(e);
			}
		}
		info.setRendered();
	}

}
