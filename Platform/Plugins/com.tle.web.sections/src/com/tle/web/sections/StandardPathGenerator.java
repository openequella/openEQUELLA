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

package com.tle.web.sections;

import hurl.build.UriBuilder;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

public class StandardPathGenerator implements PathGenerator
{

	@Override
	public URI getBaseHref(SectionInfo info)
	{
		URI baseHref = info.getAttribute(SectionInfo.KEY_BASE_HREF);
		if( baseHref == null )
		{
			baseHref = createFromRequest(info.getRequest());
		}
		return baseHref;
	}

	private URI createFromRequest(HttpServletRequest request)
	{
		UriBuilder uriBuilder = UriBuilder.create(request.getRequestURI());
		uriBuilder.setScheme(request.getScheme());
		uriBuilder.setHost(request.getServerName());
		uriBuilder.setPort(request.getServerPort());
		return uriBuilder.build();
	}

	@Override
	public URI getRelativeURI(SectionInfo info)
	{
		String path = info.getAttribute(SectionInfo.KEY_PATH);
		try
		{
			return new URI(null, null, path.substring(1), null);
		}
		catch( URISyntaxException e )
		{
			throw new IllegalArgumentException();
		}
	}

	@Override
	public URI getFullURI(SectionInfo info)
	{
		return getBaseHref(info).resolve(getRelativeURI(info));
	}

}
