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

import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.DefaultOptionsMethodException;

/**
 * @author Aaron
 *
 */
@Provider
public class DefaultOptionsExceptionMapper implements ExceptionMapper<DefaultOptionsMethodException>
{
	private static final String ALL_METHODS = "GET,HEAD,POST,PUT,DELETE,OPTIONS";

	@Context
	protected HttpHeaders httpHeaders;

	@Override
	public Response toResponse(DefaultOptionsMethodException arg0)
	{
		final ResponseBuilder response = Response.ok();
		response.header("Access-Control-Allow-Origin", "*");
		response.header("Access-Control-Allow-Methods", ALL_METHODS);
		response.header("Allow", ALL_METHODS);
		response.header("Access-Control-Allow-Headers", "X-Authorization, Content-Type");
		response.header("Access-Control-Max-Age", TimeUnit.DAYS.toSeconds(1));
		response.header("Access-Control-Expose-Headers", "Location");
		return response.build();
	}
}
