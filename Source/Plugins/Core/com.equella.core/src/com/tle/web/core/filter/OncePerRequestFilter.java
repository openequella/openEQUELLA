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
