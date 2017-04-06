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
