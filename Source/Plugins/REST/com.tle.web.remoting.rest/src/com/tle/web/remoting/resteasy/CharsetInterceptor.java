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

package com.tle.web.remoting.resteasy;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@SuppressWarnings("nls")
@Provider
@ServerInterceptor
public class CharsetInterceptor implements PostProcessInterceptor
{
	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	@Override
	public void postProcess(ServerResponse response)
	{
		final MultivaluedMap<String, Object> metadata = response.getMetadata();
		final Object contentType = metadata.getFirst(CONTENT_TYPE_HEADER);
		if( contentType != null )
		{
			final String ct;
			if( contentType instanceof MediaType )
			{
				MediaType mct = (MediaType) contentType;
				ct = mct.toString().toLowerCase();
			}
			else
			{
				ct = ((String) contentType).toLowerCase();
			}

			if( ct.contains("json") && !ct.contains("charset"))
			{
				metadata.putSingle(CONTENT_TYPE_HEADER, ct + "; charset=utf-8");
			}
		}
	}
}