/*
 * Copyright 2019 Apereo
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

package com.tle.upgrademanager.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.helpers.ServiceWrapper;

public class ServerHandler extends UrlDispatchHandler
{
	private final ServiceWrapper serviceWrapper;

	public ServerHandler(ManagerConfig config)
	{
		serviceWrapper = new ServiceWrapper(config);
	}

	public void stop(HttpExchange exchange) throws Exception
	{
		serviceWrapper.stop();
		HttpExchangeUtils.respondRedirect(exchange, "/pages/"); //$NON-NLS-1$
	}

	public void start(HttpExchange exchange) throws Exception
	{
		serviceWrapper.start();
		HttpExchangeUtils.respondRedirect(exchange, "/pages/"); //$NON-NLS-1$
	}
}
