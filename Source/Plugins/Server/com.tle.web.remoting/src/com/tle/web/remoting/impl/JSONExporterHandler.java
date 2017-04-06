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
