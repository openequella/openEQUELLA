package com.tle.json.requests;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.testng.collections.Lists;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;

public class TaskRequests extends AuthorizedRequests
{
	public TaskRequests(URI baseUri, TokenProvider tokenProvider, ObjectMapper mapper, PageContext pageContext,
		TestConfig testConfig)
	{
		super(baseUri, tokenProvider, mapper, pageContext, testConfig);
	}

	@Override
	protected String getBasePath()
	{
		if( isEquella() )
		{
			return "api/task/search";
		}
		return "api/task";
	}

	public ObjectNode search(RequestSpecification request)
	{
		return object(request.get(getResolvedPath()));
	}

	public RequestSpecification searchRequest(String query, String filter, String order, String length)
	{
		RequestSpecification request = successfulRequest().param("q", query);
		if( filter != null )
		{
			request = request.queryParam("filter", filter);
		}
		if( order != null )
		{
			request = request.queryParam("order", order);
		}
		if( length != null )
		{
			request = request.queryParam("length", length);
		}
		return request;
	}

	public ObjectNode search(String query)
	{
		return search(searchRequest(query, null, null, null));
	}

	public ObjectNode findTaskToModerate(final ItemId expectedId, final String query, final String taskId)
	{
		return waitUntil(searchRequest(query, null, null, "50"), resultAvailable(expectedId, taskId));
	}

	public <T> T waitUntil(RequestSpecification request, final Function<ObjectNode, T> until)
	{
		return requestWaiter(request).until(new Function<RequestSpecification, T>()
		{
			@Override
			public T apply(RequestSpecification request)
			{
				ObjectNode taskResults = search(request);
				return until.apply(taskResults);
			}
		});
	}

	public static Function<ObjectNode, ObjectNode> resultAvailable(final ItemId expectedId, final String taskId)
	{
		return new Function<ObjectNode, ObjectNode>()
		{
			@Override
			public ObjectNode apply(ObjectNode taskResults)
			{
				JsonNode results = taskResults.get("results");
				for( JsonNode result : results )
				{
					JsonNode itemNode = result.get("item");
					ItemId itemId = new ItemId(itemNode.get("uuid").asText(), itemNode.get("version").asInt());
					if( itemId.equals(expectedId) )
					{
						if( taskId != null && !result.get("task").get("uuid").asText().equals(taskId) )
						{
							return null;
						}
						return (ObjectNode) result;
					}
				}
				return null;
			}
		};
	}

	public static Function<ObjectNode, Boolean> taskOrder(final ItemId expectedId, final String... tasks)
	{
		final List<String> expectedOrder = Arrays.asList(tasks);
		return new Function<ObjectNode, Boolean>()
		{
			@Override
			public Boolean apply(ObjectNode taskResults)
			{
				JsonNode results = taskResults.get("results");
				List<String> taskOrder = Lists.newArrayList();
				for( JsonNode result : results )
				{
					JsonNode itemNode = result.get("item");
					ItemId itemId = new ItemId(itemNode.get("uuid").asText(), itemNode.get("version").asInt());
					if( itemId.equals(expectedId) )
					{
						taskOrder.add(result.get("task").get("uuid").asText());
					}
				}
				return taskOrder.equals(expectedOrder);
			}
		};
	}

	public void accept(ItemId item, String task, String message)
	{
		actionRequest(successfulRequest(), message).post(getModUri(), getActionParams(item, task, "accept"));
	}

	public void reject(ItemId item, String task, String message, String toTask)
	{
		rejectRequest(message, toTask).post(getModUri(), getActionParams(item, task, "reject"));
	}

	public void rejectFail(ItemId item, String task, String message, String toTask)
	{
		rejectRequest(actionRequest(badRequest(), message), toTask).post(getModUri(),
			getActionParams(item, task, "reject"));
	}

	public void comment(ItemId item, String task, String message)
	{
		actionRequest(successfulRequest(), message).post(getModUri(), getActionParams(item, task, "comment"));
	}

	public void accept(ObjectNode task, String message)
	{
		actionRequest(successfulRequest(), message).post(getLink(task, "accept"));
	}

	private RequestSpecification rejectRequest(String message, String toTask)
	{
		return rejectRequest(actionRequest(successfulRequest(), message), toTask);
	}

	public RequestSpecification rejectRequest(RequestSpecification request, String toTask)
	{
		if( toTask != null )
		{
			request = request.queryParam("to", toTask);
		}
		return request;
	}

	public void reject(ObjectNode task, String message, String toTask)
	{
		rejectRequest(message, toTask).post(getLink(task, "reject"));
	}

	public void comment(ObjectNode task, String message)
	{
		actionRequest(successfulRequest(), message).post(getLink(task, "comment"));
	}

	private RequestSpecification actionRequest(RequestSpecification request, String message)
	{
		if( message != null )
		{
			request = request.queryParam("message", message);
		}
		return request;
	}

	private Object[] getActionParams(ItemId item, String task, String action)
	{
		return new Object[]{item.getUuid(), item.getVersion(), task, action};
	}

	private String getModUri()
	{
		return getBaseUri().resolve(ItemRequests.PATH).toString() + "/{uuid}/{version}/task/{task}/{action}";
	}

}
