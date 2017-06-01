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

package com.tle.web.remoting.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import com.google.gson.Gson;
import com.tle.web.remoting.JSONService;

public class JSONExporter implements HttpRequestHandler
{
	private final Gson gson;
	private final JSONService service;

	public JSONExporter(Gson gson, JSONService service)
	{
		this.gson = gson;
		this.service = service;
	}

	@Override
	@SuppressWarnings("nls")
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		Object result = service.getResult(request);
		String json = gson.toJsonTree(result).toString();
		response.setContentType("text/json");
		response.setContentLengthLong(json.getBytes().length);
		response.getWriter().write(json);
	}
}
