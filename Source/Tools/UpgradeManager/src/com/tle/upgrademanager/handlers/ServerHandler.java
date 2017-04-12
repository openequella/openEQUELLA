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
