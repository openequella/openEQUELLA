package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.FilterResult;

/**
 * @author jmaginnis
 */
@Bind
@Singleton
public class RequestFilter extends OncePerRequestFilter
{
	@SuppressWarnings("nls")
	@Override
	protected FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		response = new IgnoreContentWrapper(response);
		response.addHeader("P3P", "CP=\"CAO PSA OUR\"");
		response.setHeader("X-Content-Type-Options", "nosniff");
		// Chrome sucks.
		// http://dev.equella.com/issues/8025
		// http://dev.equella.com/issues/5612
		String ua = request.getHeader("User-Agent");
		if( ua != null && ua.contains("Chrome") )
		{
			response.addHeader("X-XSS-Protection", "0");
		}
		else
		{
			response.setHeader("X-XSS-Protection", "1; mode=block");
		}
		return new FilterResult(response);
	}
}