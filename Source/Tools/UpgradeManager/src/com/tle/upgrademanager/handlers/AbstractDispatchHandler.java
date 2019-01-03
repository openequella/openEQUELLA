/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.upgrademanager.handlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tle.common.Pair;
import com.tle.upgrademanager.filter.SuperDuperFilter;

public abstract class AbstractDispatchHandler implements HttpHandler
{
	@Override
	public final void handle(HttpExchange exchange) throws IOException
	{
		try
		{
			doHandle(exchange);
		}
		catch( Exception th )
		{
			HttpExchangeUtils.respondApplicationError(exchange, th);
		}
		finally
		{
			final Map<String, Pair<String, File>> streams = getMultipartStreams(exchange);
			if( streams != null )
			{
				for( Pair<String, File> file : streams.values() )
				{
					if( file.getSecond().exists() )
					{
						file.getSecond().delete();
					}
				}
			}
			exchange.close();
		}
	}

	protected abstract void doHandle(HttpExchange exchange) throws Exception;

	protected final void invokeAction(HttpExchange exchange, String action) throws IOException
	{
		if( action == null || action.trim().length() == 0 )
		{
			handleNoAction(exchange);
		}
		else
		{
			handleAction(exchange, action);
		}
	}

	private void handleAction(HttpExchange exchange, String action) throws IOException
	{
		try
		{
			Method method = getClass().getMethod(action, new Class[]{HttpExchange.class});
			method.invoke(this, new Object[]{exchange});
		}
		catch( NoSuchMethodException ex )
		{
			handleUnimplementedAction(exchange, action);
		}
		catch( Exception ex )
		{
			HttpExchangeUtils.respondApplicationError(exchange, ex);
		}
	}

	@SuppressWarnings("nls")
	private void handleNoAction(HttpExchange exchange) throws IOException
	{
		String newAction = getDefaultActionName(exchange);
		if( newAction == null )
		{
			HttpExchangeUtils.respondApplicationError(exchange, new Exception("No default action specified for "
				+ getClass().getName()));
		}
		else
		{
			handleAction(exchange, newAction);
		}
	}

	public String getDefaultActionName(HttpExchange exchange) throws IOException
	{
		return null;
	}

	public void handleUnimplementedAction(HttpExchange exchange, String action) throws IOException
	{
		HttpExchangeUtils.respondApplicationError(exchange, new NoSuchMethodException(action));
	}

	@SuppressWarnings("unchecked")
	protected final Map<String, Pair<String, File>> getMultipartStreams(HttpExchange exchange)
	{
		return (Map<String, Pair<String, File>>) exchange.getAttribute(SuperDuperFilter.MULTIPART_STREAMS_KEY);
	}

	@SuppressWarnings("unchecked")
	protected final Map<String, List<String>> getParameters(HttpExchange exchange)
	{
		return (Map<String, List<String>>) exchange.getAttribute(SuperDuperFilter.PARAMS_KEY);
	}

	protected String getParameterValue(HttpExchange exchange, String param)
	{
		final List<String> values = getParameters(exchange).get(param);
		if( values == null || values.size() == 0 )
		{
			return null;
		}
		return values.get(0);
	}

	protected int getIntParameterValue(HttpExchange exchange, String param, int defaultValue)
	{
		int ival = defaultValue;
		final String sval = getParameterValue(exchange, param);
		if( sval != null && sval.length() > 0 )
		{
			try
			{
				ival = Integer.parseInt(sval);
			}
			catch( NumberFormatException e )
			{
				// ignore
			}
		}
		return ival;
	}

	protected List<String> getParameterValues(HttpExchange exchange, String param)
	{
		final List<String> values = getParameters(exchange).get(param);
		if( values == null )
		{
			return new ArrayList<String>();
		}
		return values;
	}
}
