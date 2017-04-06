package com.tle.web.remoting.rest.service;

import javax.ws.rs.core.UriBuilder;

public interface UrlLinkService
{
	UriBuilder getMethodUriBuilder(Class<?> resource, String method);
}
