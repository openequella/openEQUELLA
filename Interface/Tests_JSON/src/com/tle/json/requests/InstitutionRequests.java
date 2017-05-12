package com.tle.json.requests;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.Institutions;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.SystemTokenProvider;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public class InstitutionRequests extends AbstractCleanupRequests<String>
{
	private final String baseUrl;

	public InstitutionRequests(URI baseUri, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig, String password)
	{
		this(baseUri, new SystemTokenProvider(password), mapper, pageContext, cleanupController, testConfig, password);
	}

	public InstitutionRequests(URI baseUri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig, String password)
	{
		super(baseUri, tokens, mapper, pageContext, cleanupController, testConfig);
		baseUrl = baseUri.toString();
	}

	@Override
	protected String getBasePath()
	{
		return "api/institution";
	}

	@Override
	public String getId(ObjectNode json)
	{
		return json.get("uniqueId").asText();
	}

	public ArrayNode list()
	{
		return (ArrayNode) object(successfulRequest().get(getResolvedPath())).get("results");
	}

	/**
	 * 
	 * @param urlPortion http://serverurl/urlPortion
	 * @return
	 */
	public ObjectNode getByUrl(String urlPortion)
	{
		String fullUrl = baseUrl + urlPortion;
		if( !fullUrl.endsWith("/") )
		{
			fullUrl = fullUrl + "/";
		}
		ArrayNode all = list();
		for( JsonNode inst : all )
		{
			ObjectNode i = (ObjectNode) inst;
			if( i.get("url").asText().equals(fullUrl) )
			{
				return i;
			}
		}
		return null;
	}

	public ObjectNode createWithError(ObjectNode inst)
	{
		Response response = jsonBody(badRequest(), inst).post(getResolvedPath());
		return object(response);
	}

	public ObjectNode search(RequestSpecification request)
	{
		return object(request.get(getResolvedPath()));
	}

	public ObjectNode jsonAppendBaseUrl(String name, String password, String filestoreId, String url, boolean enabled)
	{
		return Institutions.json(name, password, filestoreId, baseUrl + url, enabled);
	}

}
