package com.tle.upgrademanager.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class WebRootHandler implements HttpHandler
{
	@Override
	public void handle(HttpExchange exchange) throws IOException
	{
		String uri = exchange.getRequestURI().toString();
		if( uri.equals("/") ) //$NON-NLS-1$
		{
			HttpExchangeUtils.respondRedirect(exchange, "/pages/"); //$NON-NLS-1$
			return;
		}
		URL res = getClass().getResource("/web" + uri); //$NON-NLS-1$
		if( res == null )
		{
			HttpExchangeUtils.respondFileNotFound(exchange);
			return;
		}

		URLConnection conn = res.openConnection();
		try( InputStream in = conn.getInputStream() )
		{
			HttpExchangeUtils.setContentType(exchange, HttpExchangeUtils.getContentTypeForUri(uri));
			exchange.sendResponseHeaders(200, conn.getContentLength());
			ByteStreams.copy(in, exchange.getResponseBody());
		}
		catch( Exception ex )
		{
			HttpExchangeUtils.respondApplicationError(exchange, ex);
		}
		finally
		{
			exchange.close();
		}
	}
}
