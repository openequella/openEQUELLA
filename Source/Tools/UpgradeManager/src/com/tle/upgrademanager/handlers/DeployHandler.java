package com.tle.upgrademanager.handlers;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.helpers.AjaxState;
import com.tle.upgrademanager.helpers.Deployer;

@SuppressWarnings("nls")
public class DeployHandler extends UrlDispatchHandler
{
	private static final Log LOGGER = LogFactory.getLog(DeployHandler.class);

	private final ManagerConfig config;
	private final AjaxState ajax;

	public DeployHandler(ManagerConfig config, AjaxState ajax)
	{
		this.config = config;
		this.ajax = ajax;
	}

	public void download(HttpExchange exchange) throws IOException
	{
		HttpExchangeUtils.respondRedirect(exchange, "/pages/"); //$NON-NLS-1$
	}

	public void deploy(HttpExchange exchange) throws IOException
	{
		final String ajaxId = UUID.randomUUID().toString();
		final String path = "/deploy/deploy/"; //$NON-NLS-1$
		final String filename = exchange.getRequestURI().getPath().substring(path.length());

		new Thread()
		{
			@Override
			public void run()
			{
				LOGGER.debug("Running deployer on version " + filename);
				Deployer deploy = new Deployer(ajaxId, ajax, config);
				deploy.deploy(filename);
			}
		}.start();

		HttpExchangeUtils.respondRedirect(exchange, "/pages/progress/" + ajaxId); //$NON-NLS-1$
	}
}
