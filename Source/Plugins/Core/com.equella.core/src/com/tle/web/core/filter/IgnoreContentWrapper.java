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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class IgnoreContentWrapper extends HttpServletResponseWrapper
{
	public IgnoreContentWrapper(HttpServletResponse resp)
	{
		super(resp);
	}

	@Override
	public void addHeader(String header, String val)
	{
		if( header.equals("Content-Location") ) //$NON-NLS-1$
		{
			return;
		}

		if( header.equals("X-Content-Location") ) //$NON-NLS-1$
		{
			header = "Content-Location"; //$NON-NLS-1$
		}

		super.addHeader(header, val);
	}

	@Override
	public void setHeader(String header, String val)
	{
		if( header.equals("Content-Location") ) //$NON-NLS-1$
		{
			return;
		}

		if( header.equals("X-Content-Location") ) //$NON-NLS-1$
		{
			header = "Content-Location"; //$NON-NLS-1$
		}

		super.setHeader(header, val);
	}

}
