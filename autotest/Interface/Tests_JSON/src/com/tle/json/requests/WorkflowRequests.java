package com.tle.json.requests;

import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public class WorkflowRequests extends BaseEntityRequests
{
	public WorkflowRequests(URI baseUri, TokenProvider tokens, ObjectMapper mapper, PageContext pageContext,
		CleanupController cleanupController, TestConfig testConfig)
	{
		super(baseUri, tokens, mapper, pageContext, cleanupController, testConfig);
	}

	@Override
	protected String getBasePath()
	{
		return "api/workflow";
	}
}
