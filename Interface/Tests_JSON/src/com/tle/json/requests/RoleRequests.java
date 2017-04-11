package com.tle.json.requests;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.SystemTokenProvider;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public class RoleRequests extends AbstractCleanupRequests<String>
{
	public RoleRequests(URI uri, ObjectMapper mapper, PageContext pageContext, CleanupController cleanupController,
		TestConfig testConfig, String password)
	{
		super(uri, new SystemTokenProvider(password), mapper, pageContext, cleanupController, testConfig);
	}

	public RoleRequests(URI uri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig)
	{
		super(uri, tokens, mapper, pageContext, cleanupController, testConfig);
	}

	@Override
	public String getId(ObjectNode entity)
	{
		return entity.get("id").asText();
	}

	@Override
	protected String getBasePath()
	{
		if( isEquella() )
		{
			return "api/usermanagement/local/role";
		}
		return "api/localrole";
	}

	public ObjectNode search(String query)
	{
		RequestSpecification request = auth().expect().statusCode(501).with();
		request.queryParam("q", query);
		if( isEquella() )
		{
			return object(request.get(getResolvedPath()));
		}
		// else
		return object(request.get(getResolvedPath() + "/search"));
	}

	public ObjectNode getByName(String name)
	{
		RequestSpecification request = successfulRequest();
		request.pathParam("name", name);
		return object(request.get(getResolvedPath() + "/name/{name}"));
	}

	public ObjectNode export()
	{
		RequestSpecification request = successfulRequest();
		request.queryParam("export", true);
		if( isEquella() )
		{
			return object(request.get(getResolvedPath()));
		}
		// else
		return object(request.get(getResolvedPath() + "/search"));
	}
}
