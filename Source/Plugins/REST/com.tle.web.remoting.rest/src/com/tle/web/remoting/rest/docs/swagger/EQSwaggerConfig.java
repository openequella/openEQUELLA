package com.tle.web.remoting.rest.docs.swagger;


import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.UrlService;
import com.wordnik.swagger.config.SwaggerConfig;

@SuppressWarnings("nls")
@Bind
@Singleton
public class EQSwaggerConfig extends SwaggerConfig
{
	@Inject
	private UrlService urlService;

	@Override
	public String basePath()
	{
		return urlService.institutionalise("api");
	}
}
