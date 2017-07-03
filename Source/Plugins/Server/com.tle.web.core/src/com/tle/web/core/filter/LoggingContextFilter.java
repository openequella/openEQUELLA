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

package com.tle.web.core.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.dytech.edge.common.Constants;
import com.google.common.base.Strings;
import com.tle.annotation.Nullable;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilterCallback;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class LoggingContextFilter extends AbstractWebFilter
{
	private static final Logger LOGGER = Logger.getLogger(LoggingContextFilter.class);
	private static final long LOG_IF_REQUEST_LONGER_THAN = TimeUnit.SECONDS.toMillis(5);

	@Nullable
	private RequestLogger requestLogger;

	public LoggingContextFilter()
	{
		if( RequestLogger.REQUEST_LOGGER.isTraceEnabled() )
		{
			requestLogger = new RequestLogger();
		}
	}

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		final long t = System.currentTimeMillis();

		if( requestLogger != null )
		{
			requestLogger.logRequest(request);
		}

		final Thread thread = Thread.currentThread();
		final String oldThreadName = thread.getName();
		final String requestURI = request.getRequestURI();
		final String qs = request.getQueryString();
		final String fullUrl = requestURI + (qs == null ? "" : "?" + qs);

		String threadName = null;
		if( LOGGER.isDebugEnabled() )
		{
			threadName = oldThreadName;
			int tilde = oldThreadName.indexOf(" ~ ");
			if( tilde >= 0 )
			{
				threadName = Utils.safeSubstring(threadName, 0, tilde);
			}

			threadName = threadName + " ~ " + requestURI;
			thread.setName(threadName);
		}

		return new FilterResult(new WebFilterCallback()
		{
			@Override
			public void afterServlet(HttpServletRequest request, HttpServletResponse response)
			{
				long t2 = System.currentTimeMillis() - t;
				if( LOG_IF_REQUEST_LONGER_THAN < t2 )
				{
					LOGGER.info("Request for " + fullUrl + " took " + t2 + "ms");
				}

				if( LOGGER.isTraceEnabled() )
				{
					LOGGER.trace("\t" + fullUrl + "\t" + t2);
				}

				if( LOGGER.isDebugEnabled() )
				{
					thread.setName(oldThreadName);
				}

				MDC.remove(Constants.MDC_SESSION_ID);
			}
		});
	}

	// This is for DEV ONLY.  Do not use in a test / staging / prod environment.  
	// There is no guarantee that all sensitive information will be blanked!
	private static class RequestLogger
	{
		private static final Logger REQUEST_LOGGER = Logger
			.getLogger(LoggingContextFilter.class.getName() + ".RequestLogger");

		public void logRequest(HttpServletRequest request)
		{
			final StringBuilder params = new StringBuilder(request.getRequestURI() + "\n");
			final Map<String, String[]> parameterMap = request.getParameterMap();
			for( Entry<String, String[]> entry : parameterMap.entrySet() )
			{
				final String key = entry.getKey();
				final String keyLower = key.toLowerCase();
				final boolean blankOut = keyLower.contains("password") || keyLower.contains("secret");
				params.append(key);
				params.append("=");

				boolean firstVal = true;
				final String[] values = entry.getValue();
				if( values != null )
				{
					for( String value : values )
					{
						if( !firstVal )
						{
							params.append(",");
						}
						if( blankOut )
						{
							params.append(Strings.padStart("", value.length(), '*'));
						}
						else
						{
							params.append(value);
						}
						firstVal = false;
					}
				}
				params.append("\n");
			}
			REQUEST_LOGGER.trace(params.toString());
		}
	}
}
