package com.tle.web.core.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;

public abstract class OncePerRequestFilter extends AbstractWebFilter
{
	private String alreadyFilteredName;

	public OncePerRequestFilter()
	{
		super();
	}

	@Override
	public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException
	{
		if( request.getAttribute(getAlreadyFilteredAttributeName()) == null )
		{
			request.setAttribute(getAlreadyFilteredAttributeName(), Boolean.TRUE);
			return doFilterInternal(request, response);
		}
		return FilterResult.FILTER_CONTINUE;
	}

	protected abstract FilterResult doFilterInternal(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException;

	protected String getAlreadyFilteredAttributeName()
	{
		if( alreadyFilteredName == null )
		{
			alreadyFilteredName = getClass().getName() + ".FILTERED"; //$NON-NLS-1$
		}
		return alreadyFilteredName;
	}
}
