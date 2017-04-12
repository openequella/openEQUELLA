package com.tle.upgrademanager.handlers;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.io.Files;
import com.sun.net.httpserver.HttpExchange;
import com.tle.common.Pair;
import com.tle.upgrademanager.ManagerConfig;
import com.tle.upgrademanager.Utils;

public class UploadHandler extends PostDispatchHandler
{
	private final ManagerConfig config;

	public UploadHandler(ManagerConfig config)
	{
		this.config = config;
	}

	public void upload(HttpExchange exchange) throws IOException
	{
		final Map<String, Pair<String, File>> streams = getMultipartStreams(exchange);
		final Pair<String, File> file = streams.get("file"); //$NON-NLS-1$
		if( file != null )
		{
			String filename = file.getFirst();
			if( Utils.VERSION_EXTRACT.matcher(filename).matches() )
			{
				File vf = config.getUpdatesDir();
				Files.move(file.getSecond(), new File(vf, filename));
			}
		}
		HttpExchangeUtils.respondRedirect(exchange, "/pages/"); //$NON-NLS-1$
	}
}
