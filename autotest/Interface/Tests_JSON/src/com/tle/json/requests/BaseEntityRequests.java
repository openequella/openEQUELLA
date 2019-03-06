package com.tle.json.requests;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public abstract class BaseEntityRequests extends AbstractCleanupRequests<String>
{
	protected BaseEntityRequests(URI baseUri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig)
	{
		super(baseUri, tokens, mapper, pageContext, cleanupController, testConfig);
	}

	@Override
	public String getId(ObjectNode entity)
	{
		return entity.get("uuid").asText();
	}

	public ObjectNode list()
	{
		return object(successfulRequest().get(getResolvedPath()));
	}

	public Response listFail(RequestSpecification request)
	{
		return request.get(getResolvedPath());
	}

	public ObjectNode listForExport()
	{
		return object(successfulRequest().param("export", true).get(getResolvedPath()));
	}

	public ObjectNode listAcls()
	{
		return object(successfulRequest().get(getResolvedAclPath()));
	}

	public Response listAclsFail(RequestSpecification request)
	{
		return request.get(getResolvedAclPath());
	}

	private String getResolvedAclPath()
	{
		return getResolvedPath() + "/acl";
	}

	public Response editAcls(ObjectNode node)
	{
		return jsonBody(successfulRequest(), node).put(getResolvedAclPath());

	}

	public ObjectNode importEntity(ObjectNode entity)
	{
		RequestSpecification req = createRequest(entity).queryParam("import", true);
		return create(req);
	}
}
