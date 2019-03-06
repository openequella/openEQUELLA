package com.tle.resttests;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.testng.collections.Lists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("nls")
public abstract class AbstractEntityApiTest extends AbstractItemApiTest
{
	private List<String> collections = Lists.newArrayList();
	private List<String> schemas = Lists.newArrayList();
	private List<String> workflows = Lists.newArrayList();

	protected void deleteSchema(String uuid, String token) throws IOException
	{
		HttpResponse response = deleteResource(getUri("schema/", uuid), token);
		assertResponse(response, 204, "Should have deleted the schema");
	}

	protected void deleteWorkflow(String uuid, String token) throws IOException
	{
		HttpResponse response = deleteResource(getUri("workflow/", uuid), token);
		assertResponse(response, 204, "Should have deleted the workflow");
	}

	protected void deleteCollection(String uuid, String token) throws IOException
	{
		HttpResponse response = deleteResource(getUri("collection/", uuid), token);
		assertResponse(response, 204, "Should have deleted the collection");
	}

	protected ObjectNode createSchemaObject(String json, String token) throws IOException
	{
		HttpResponse response = postEntity(json, context.getBaseUrl() + "api/schema", token, true);
		assertResponse(response, 201, "Should have created the schema");
		String schemaUri = response.getFirstHeader("Location").getValue();
		return (ObjectNode) getEntity(schemaUri, token);
	}

	protected String createSchema(ObjectNode json) throws IOException
	{
		ObjectNode object = createSchemaObject(json.toString(), getToken());
		String uuid = object.get("uuid").asText();
		schemas.add(uuid);
		return uuid;
	}

	protected HttpResponse editWorkflow(ObjectNode json) throws IOException
	{
		String uuid = json.get("uuid").asText();
		return putEntity(json.toString(), context.getBaseUrl() + "api/workflow/" + uuid, getToken(), true);
	}

	protected ObjectNode createWorkflowObject(String json, String token) throws IOException
	{
		HttpResponse response = postEntity(json, context.getBaseUrl() + "api/workflow", token, true);
		assertResponse(response, 201, "Should have created the workflow");
		String workflowUri = response.getFirstHeader("Location").getValue();
		ObjectNode workflow = (ObjectNode) getEntity(workflowUri, token);
		workflows.add(getUuid(workflow));
		return workflow;
	}

	protected String getUuid(ObjectNode object)
	{
		return object.get("uuid").asText();
	}

	protected String createWorkflow(ObjectNode json) throws IOException
	{
		return getUuid(createWorkflowObject(json.toString(), getToken()));
	}

	protected ObjectNode createCollectionObject(String json, String token) throws IOException
	{
		HttpResponse response = postEntity(json, context.getBaseUrl() + "api/collection", token, true);
		assertResponse(response, 201, "Should have created the collection");
		String collectionUri = response.getFirstHeader("Location").getValue();
		return (ObjectNode) getEntity(collectionUri, token);
	}

	protected String createCollection(ObjectNode json) throws IOException
	{
		ObjectNode object = createCollectionObject(json.toString(), getToken());
		String uuid = object.get("uuid").asText();
		collections.add(uuid);
		return uuid;
	}

	@Override
	protected void deleteItems() throws IOException
	{
		super.deleteItems();
		String token = getToken();
		for( String uuid : collections )
		{
			deleteCollection(uuid, token);
		}
		collections.clear();
		for( String uuid : workflows )
		{
			deleteWorkflow(uuid, token);
		}
		workflows.clear();
		for( String uuid : schemas )
		{
			deleteSchema(uuid, token);
		}
		schemas.clear();
	}

	protected String getUri(String path, String uuid)
	{
		StringBuilder sbuf = new StringBuilder();
		sbuf.append(context.getBaseUrl());
		sbuf.append("api/");
		sbuf.append(path);
		sbuf.append(uuid);
		sbuf.append('/');
		return sbuf.toString();
	}

	protected HttpResponse lockEntityError(String token, String entityUri, Object... params) throws IOException
	{
		String lockSuffix = entityUri.endsWith("/") ? "lock" : "/lock";
		HttpPost lockPost = new HttpPost(appendQueryString(entityUri + lockSuffix, queryString(params)));
		return execute(lockPost, true, token);
	}

	protected HttpResponse getLockEntityError(String token, String entityUri, Object... params) throws IOException
	{
		String lockSuffix = entityUri.endsWith("/") ? "lock" : "/lock";
		HttpGet lockGet = new HttpGet(appendQueryString(entityUri + lockSuffix, queryString(params)));
		return execute(lockGet, true, token);
	}

	protected JsonNode lockEntity(String token, String entityUri, Object... params) throws IOException
	{
		String lockSuffix = entityUri.endsWith("/") ? "lock" : "/lock";
		HttpPost lockPost = new HttpPost(appendQueryString(entityUri + lockSuffix, queryString(params)));
		HttpResponse response = execute(lockPost, false, token);
		assertResponse(response, 201, "Should have locked the entity");
		return mapper.readTree(response.getEntity().getContent());
	}

	protected JsonNode getLock(String token, String entityUri, Object... params) throws IOException
	{
		String lockSuffix = entityUri.endsWith("/") ? "lock" : "/lock";
		HttpGet lockGet = new HttpGet(appendQueryString(entityUri + lockSuffix, queryString(params)));
		HttpResponse response = execute(lockGet, false, token);
		assertResponse(response, 200, "Should have got the lock");
		return mapper.readTree(response.getEntity().getContent());
	}

	protected void unlock(String token, String entityUri, Object... params) throws IOException
	{
		String lockSuffix = entityUri.endsWith("/") ? "lock" : "/lock";
		HttpResponse response = deleteResource(entityUri + lockSuffix, token, params);
		assertResponse(response, 204, "Should have deleted the lock");
	}

	protected ObjectNode createWorkflowTask(String name, boolean allowEditing, String... users)
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("name", name);
		node.put("type", "t");
		ArrayNode userArray = node.putArray("users");
		for( String user : users )
		{
			userArray.add(user);
		}
		node.put("allowEditing", allowEditing);
		return node;
	}

	protected ObjectNode addWorkflowChild(ObjectNode parent, ObjectNode child)
	{
		ArrayNode nodes = (ArrayNode) parent.get("nodes");
		if( nodes == null )
		{
			nodes = parent.putArray("nodes");
		}
		nodes.add(child);
		return parent;
	}

	protected ObjectNode createWorkflowJson(String name)
	{
		ObjectNode workflow = mapper.createObjectNode();
		workflow.put("name", name);
		ObjectNode root = workflow.with("root");
		root.put("name", "start");
		root.put("type", "s");
		return workflow;
	}

}
