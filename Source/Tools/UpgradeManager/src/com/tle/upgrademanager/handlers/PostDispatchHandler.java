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

package com.tle.upgrademanager.handlers;

import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

public abstract class PostDispatchHandler extends UrlDispatchHandler
{
	private static final String ACTION_PREFIX = "action-"; //$NON-NLS-1$

	@Override
	public void doHandle(HttpExchange exchange) throws Exception
	{
		if( HttpExchangeUtils.isPost(exchange) )
		{
			invokeAction(exchange, determineAction(getParameters(exchange)));
		}
		else
		{
			super.doHandle(exchange);
		}
	}

	private String determineAction(Map<String, ?> fields)
	{
		for( String key : fields.keySet() )
		{
			if( key.startsWith(ACTION_PREFIX) )
			{
				return key.substring(ACTION_PREFIX.length());
			}
		}
		return null;
	}
}
