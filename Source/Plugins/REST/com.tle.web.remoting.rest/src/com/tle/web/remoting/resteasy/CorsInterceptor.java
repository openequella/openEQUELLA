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

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

@Provider
@ServerInterceptor
@SuppressWarnings("nls")
public class CorsInterceptor implements PostProcessInterceptor
{
	@Override
	public void postProcess(ServerResponse response)
	{
		process(response);
	}

	public static void runPostProcess(ServerResponse response)
	{
		process(response);
	}

	private static void process(ServerResponse response)
	{
		final MultivaluedMap<String, Object> metadata = response.getMetadata();
		metadata.putSingle("Access-Control-Allow-Origin", "*");
		metadata.putSingle("Access-Control-Expose-Headers", "Location");
	}
}