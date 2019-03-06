package com.tle.json.requests;

import static com.jayway.restassured.RestAssured.given;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;

public class SchedulerRequests extends AuthorizedRequests
{
	private final PageContext pageContext;

	public SchedulerRequests(URI baseUri, ObjectMapper mapper, PageContext pageContext, TestConfig testConfig)
	{
		super(baseUri, null, mapper, pageContext, testConfig);
		this.pageContext = pageContext;
	}

	@Override
	protected String getBasePath()
	{
		return "api/scheduler";
	}

	@Override
	protected RequestSpecification auth()
	{
		return given().header("X-Authorization", "admin_token=tle010").header("X-Autotest-Key",
			pageContext.getFullName(""));
	}

	public Response execute(String taskId)
	{
		return successfulRequest().post(getResolvedPath() + "/execute/{id}", taskId);
	}
}
