package com.tle.web.core.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.dytech.edge.common.Constants;
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

	public LoggingContextFilter()
	{
		super();
	}

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		final long t = System.currentTimeMillis();

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
}
