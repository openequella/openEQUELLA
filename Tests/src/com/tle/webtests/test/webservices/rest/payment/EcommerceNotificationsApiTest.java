package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.webservices.rest.AbstractItemApiTest;
import com.tle.webtests.test.webservices.rest.OAuthClient;
import com.tle.webtests.test.webservices.rest.OAuthUtils;

/**
 * @see: redmine #7415
 */

@TestInstitution("storebackendssl")
public class EcommerceNotificationsApiTest extends AbstractItemApiTest
{
	private OAuthClient backendClient;
	private OAuthClient frontendClient;
	private PageContext backend;
	private PageContext frontend;
	private String backendToken;
	private String frontendToken;
	private static final String PREFIX = "EcommerceNotificationsApiTest-";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// Will do it myself
	}

	@BeforeClass
	@Override
	public void registerClients() throws Exception
	{
		backend = context;
		frontend = newContext("storefront");
		
		backendClient = new OAuthClient();
		backendClient.setName(PREFIX + "client");
		backendClient.setClientId(PREFIX + "client");
		backendClient.setUsername("AutoTest");
		logon(backend, "AutoTest", "automated");
		OAuthUtils.createClient(context, backendClient);
		logout();
		backendToken = requestToken(backendClient);
		
		frontendClient = new OAuthClient();
		frontendClient.setName(PREFIX + "client");
		frontendClient.setClientId(PREFIX + "client");
		frontendClient.setUsername("AutoTest");
		logon(frontend, "AutoTest", "automated");
		OAuthUtils.createClient(frontend, frontendClient);
		logout();
		frontendToken = requestToken(frontendClient, frontend);
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		logon(frontend, "AutoTest", "automated");
		OAuthUtils.deleteClient(frontend, frontendClient.getClientId());
		logout();
		logon(backend, "AutoTest", "automated");
		OAuthUtils.deleteClient(backend, backendClient.getClientId());
		logout();
	}

	private JsonNode doNotificationsSearch(String query, String subsearch, Map<?, ?> otherParams, String token,
		PageContext contextToUse)
		throws Exception
	{
		List<NameValuePair> params = Lists.newArrayList();
		if( query != null )
		{
			params.add(new BasicNameValuePair("q", query));
		}
		if( query != null )
		{
			params.add(new BasicNameValuePair("type", subsearch));
		}
		if( otherParams != null )
		{
			for( Entry<?, ?> entry : otherParams.entrySet() )
			{
				params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
			}
		}
		String paramString = URLEncodedUtils.format(params, "UTF-8");
		HttpGet get = new HttpGet(contextToUse.getBaseUrl() + "api/notification?" + paramString);
		HttpResponse response = execute(get, false, token);
		JsonNode resultsNode = mapper.readTree(response.getEntity().getContent());

		return resultsNode;
	}

	@Test
	public void ecommerceNotificationsTest() throws Exception
	{
		// Backend
		JsonNode backendResultsNode = doNotificationsSearch("NotificationApiTest", "all", null, backendToken, backend);
		assertNotificationCorrect(backendResultsNode, 0, 1, 1, "itemsold", "f6a57ba3-9635-423c-a25c-0e4c25340f08");
		backendResultsNode = doNotificationsSearch("", "itemsold", null, backendToken, backend);
		assertAllReason(backendResultsNode, "itemsold");

		JsonNode frontendResultsNode = doNotificationsSearch("NotificationApiTest", "all", null, frontendToken,
			frontend);
		assertNotificationCorrect(frontendResultsNode, 0, 1, 1, "piupdate", "41ccb679-6da2-44cf-b047-bd2aff000f67");
		// frontendResultsNode = doNotificationsSearch("", "piupdate", null,
		// frontendToken, frontend);
		// Uncomment this when/if hacky pi notifications are sorted out
	}

	private void assertNotificationCorrect(JsonNode json, int start, int length, int available, String reason,
		String uuid)
	{
		JsonNode result = json.get("results").get(0);
		JsonNode item = result.get("item");
		assertEquals(json.get("start").asInt(), start);
		assertEquals(json.get("length").asInt(), length);
		assertEquals(json.get("available").asInt(), available);
		assertEquals(result.get("reason").asText(), reason);
		assertEquals(item.get("uuid").asText(), uuid);
	}

	private void assertAllReason(JsonNode json, String reason)
	{
		for( int i = 0; i < json.get("length").asInt(); i++ )
		{
			assertEquals(json.get("results").get(i).get("reason").asText(), reason);
		}
	}
}
