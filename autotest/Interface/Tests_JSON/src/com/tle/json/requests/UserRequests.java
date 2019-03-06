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

@SuppressWarnings("nls")
public class UserRequests extends AbstractCleanupRequests<String>
{
	public UserRequests(URI uri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig)
	{
		super(uri, tokens, mapper, pageContext, cleanupController, testConfig);
	}

	public UserRequests(URI uri, ObjectMapper mapper, PageContext pageContext, CleanupController cleanupController,
		TestConfig testConfig, String password)
	{
		super(uri, new SystemTokenProvider(password), mapper, pageContext, cleanupController, testConfig);
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
			return "api/usermanagement/local/user";
		}
		return "api/localuser";
	}

	public ObjectNode export()
	{
		RequestSpecification request = successfulRequest().queryParam("export", true);
		if( isEquella() )
		{
			return object(request.get(getResolvedPath()));
		}
		// else
		return object(request.get(getResolvedPath() + "/search"));
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

	public ObjectNode getByUsername(String username)
	{
		RequestSpecification request = successfulRequest();
		request.pathParam("username", username);
		return object(request.get(getResolvedPath() + "/username/{username}"));
	}
}
