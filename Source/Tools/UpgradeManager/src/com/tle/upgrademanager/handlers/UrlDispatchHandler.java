package com.tle.upgrademanager.handlers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.net.httpserver.HttpExchange;

public abstract class UrlDispatchHandler extends AbstractDispatchHandler
{
	private Pattern parser;
	private String context;

	private synchronized Pattern getParser(HttpExchange exchange)
	{
		if( parser == null )
		{
			context = exchange.getHttpContext().getPath();
			parser = Pattern.compile("^" + context + "([^?/]*).*?$"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return parser;
	}

	@Override
	@SuppressWarnings("nls")
	public void doHandle(HttpExchange exchange) throws Exception
	{
		Matcher m = getParser(exchange).matcher(exchange.getRequestURI().toString());
		if( !m.matches() )
		{
			// We should never get here unless the handler mapping differs from
			// the base path
			throw new Exception("Dispatch cannot be parsed");
		}

		invokeAction(exchange, m.group(1));
	}
}
