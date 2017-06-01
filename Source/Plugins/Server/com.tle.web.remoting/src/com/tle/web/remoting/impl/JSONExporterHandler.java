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

import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.springframework.web.HttpRequestHandler;

import com.google.gson.Gson;
import com.tle.core.guice.Bind;
import com.tle.web.remoting.JSONService;

@Bind
@Singleton
public class JSONExporterHandler extends AbstractRemoteHandler<JSONService>
{
	private Gson gson = new Gson();

	@Override
	protected HttpRequestHandler createHandlerFromBean(Extension extension, JSONService handlerBean)
	{
		return new JSONExporter(gson, handlerBean);
	}

	@Override
	protected String getExtensionPointName()
	{
		return "json"; //$NON-NLS-1$
	}

}
