package com.tle.json.requests;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public class AclRequests extends AuthorizedRequests
{
	public AclRequests(URI baseUri, TokenProvider tokenProvider, ObjectMapper mapper, PageContext pageContext,
		TestConfig testConfig)
	{
		super(baseUri, tokenProvider, mapper, pageContext, testConfig);
	}

	@Override
	protected String getBasePath()
	{
		return "api/acl";
	}

	public ObjectNode list()
	{
		return object(successfulRequest().get(getResolvedPath()));
	}

	public void edit(ObjectNode rules)
	{
		jsonBody(successfulRequest(), rules).put(getResolvedPath());
	}
}
