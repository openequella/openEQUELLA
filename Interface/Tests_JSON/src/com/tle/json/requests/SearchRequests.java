package com.tle.json.requests;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TimeoutException;
import com.tle.json.framework.TokenProvider;
import com.tle.json.framework.Waiter;

public class SearchRequests extends AuthorizedRequests
{
	public SearchRequests(URI baseUri, TokenProvider tokenProvider, ObjectMapper mapper, PageContext pageContext,
		TestConfig testConfig)
	{
		super(baseUri, tokenProvider, mapper, pageContext, testConfig);
	}

	@Override
	protected String getBasePath()
	{
		return "api/search";
	}

	public ObjectNode search(RequestSpecification request)
	{
		return object(request.get(getResolvedPath()));
	}

	public ObjectNode scan(RequestSpecification request)
	{
		return object(request.get(getResolvedPath() + "/scan"));
	}

	public RequestSpecification searchRequest(String query, String order)
	{
		RequestSpecification request = successfulRequest().param("q", query);
		if( order != null )
		{
			request = request.queryParam("order", order);
		}
		return request;
	}

	public ObjectNode search(String query)
	{
		return search(searchRequest(query, null));
	}

	public ObjectNode waitForIndex(final ItemId expectedId, final String query) throws TimeoutException
	{
		return waitUntil(searchRequest(query, null), resultAvailable(expectedId));
	}

	// Wait for an item to dissapear from the index
	public void waitForNotIndexed(ItemId expectedId, String query)
	{
		waitUntil(searchRequest(query, null), resultNotAvailable(expectedId));
	}

	public <T> T waitUntil(RequestSpecification request, final Function<ObjectNode, T> until) throws TimeoutException
	{
		Waiter<RequestSpecification> indexWaiter = new Waiter<RequestSpecification>(request).withTimeout(20,
			TimeUnit.SECONDS);
		return indexWaiter.until(new Function<RequestSpecification, T>()
		{
			@Override
			public T apply(RequestSpecification request)
			{
				ObjectNode searchResults = search(request);
				return until.apply(searchResults);
			}
		});
	}

	public Boolean waitUntilIgnoreError(RequestSpecification request, final Function<ObjectNode, Boolean> until)
		throws TimeoutException
	{
		return requestWaiter(request).until(new Function<RequestSpecification, Boolean>()
		{
			@Override
			public Boolean apply(RequestSpecification request)
			{
				try
				{
					ObjectNode searchResults = search(request);
					return until.apply(searchResults);
				}
				catch( Exception | AssertionError e )
				{
					return false;
				}
			}
		});
	}

	public static Function<ObjectNode, ObjectNode> resultAvailable(final ItemId expectedId)
	{
		return new Function<ObjectNode, ObjectNode>()
		{
			@Override
			public ObjectNode apply(ObjectNode searchResults)
			{
				JsonNode results = searchResults.get("results");
				for( JsonNode result : results )
				{
					ItemId itemId = new ItemId(result.get("uuid").asText(), result.get("version").asInt());
					if( itemId.equals(expectedId) )
					{
						return (ObjectNode) result;
					}
				}
				return null;
			}
		};
	}

	public static Function<ObjectNode, Boolean> resultUnavailable(final ItemId expectedId)
	{
		return new Function<ObjectNode, Boolean>()
		{
			@Override
			public Boolean apply(ObjectNode searchResults)
			{
				JsonNode results = searchResults.get("results");
				for( JsonNode result : results )
				{
					ItemId itemId = new ItemId(result.get("uuid").asText(), result.get("version").asInt());
					if( itemId.equals(expectedId) )
					{
						return null;
					}
				}
				return Boolean.TRUE;
			}
		};
	}

	public static Function<ObjectNode, Boolean> resultNotAvailable(final ItemId expectedId)
	{
		return new Function<ObjectNode, Boolean>()
		{
			@Override
			public Boolean apply(ObjectNode searchResults)
			{
				JsonNode results = searchResults.get("results");

				for( JsonNode result : results )
				{
					ItemId itemId = new ItemId(result.get("uuid").asText(), result.get("version").asInt());
					if( itemId.equals(expectedId) )
					{
						return false;
					}
				}
				return true;
			}
		};
	}
}