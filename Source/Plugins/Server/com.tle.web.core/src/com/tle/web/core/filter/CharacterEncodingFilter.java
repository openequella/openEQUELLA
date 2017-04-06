package com.tle.web.core.filter;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.FilterResult;

@Bind
@Singleton
public class CharacterEncodingFilter extends OncePerRequestFilter
{
	@Override
	protected FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		request.setCharacterEncoding(Utils.CHARSET_ENCODING);
		return FilterResult.FILTER_CONTINUE;
	}
}
