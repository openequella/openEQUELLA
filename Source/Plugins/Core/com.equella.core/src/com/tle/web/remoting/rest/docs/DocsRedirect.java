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

package com.tle.web.remoting.rest.docs;

import javax.ws.rs.core.MultivaluedMap;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.util.HttpResponseCodes;

public class DocsRedirect extends ServerResponse
{
	private Headers<Object> headers = new Headers<Object>();

	public DocsRedirect(String url)
	{
		super();
		headers.add("Location", url); //$NON-NLS-1$ 
	}

	@Override
	public Object getEntity()
	{
		return null;
	}

	@Override
	public int getStatus()
	{
		return HttpResponseCodes.SC_MOVED_TEMPORARILY;
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata()
	{
		return headers;
	}

}
