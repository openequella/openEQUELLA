package com.tle.resttests.test.workflow;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.resttests.AbstractEntityApiLockTest;

public class WorkflowApiLockTest extends AbstractEntityApiLockTest
{
	private String token;
	private String workflowUuid;
	private String workflowUri;

	private void createSimpleWorkflow() throws IOException
	{
		token = requestToken(OAUTH_CLIENT_ID);
		ObjectNode workflow = mapper.createObjectNode();
		workflow.put("name", "A workflow");
		ObjectNode root = workflow.with("root");
		root.put("name", "start");
		root.put("type", "s");

		HttpResponse response = postEntity(workflow.toString(), context.getBaseUrl() + "api/workflow", token, true);
		assertResponse(response, 201, "Expected workflow to be created");

		workflowUri = response.getFirstHeader("Location").getValue();
		ObjectNode newWorkflow = (ObjectNode) getEntity(workflowUri, token);
		workflowUuid = newWorkflow.get("uuid").asText();
	}

	@Test
	public void edit() throws Exception
	{
		createSimpleWorkflow();
		testLocks(token, workflowUri);
	}

	@AfterMethod(alwaysRun = true)
	public void cleanupSchema() throws IOException
	{
		if( workflowUuid != null )
		{
			deleteWorkflow(workflowUuid, token);
		}
	}

}
