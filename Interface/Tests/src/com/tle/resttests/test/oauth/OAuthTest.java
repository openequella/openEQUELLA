/**
 * This test needs Redis, if this test fails it's probably because Redis isn't
 * right
 */

package com.tle.resttests.test.oauth;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.resttests.AbstractRestApiTest;
import com.tle.resttests.util.OAuthClient;
import com.tle.resttests.util.RestTestConstants;
import com.tle.resttests.util.TokenSecurity;
import com.tle.resttests.util.URLUtils;

public class OAuthTest extends AbstractRestApiTest
{
	private static final String CLIENT_ID_SERVER_FLOW = "testOAuthServerSideFlowClient";
	private static final String CLIENT_ID = "testOAuthTokenLoginClient";
	private static final String REDIRECT_URI = "default";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
	}

	@Override
	protected void addFullOAuthClients(List<OAuthClient> clients)
	{
		String home = context.getBaseUrl() + "logon.do";
		OAuthClient client = new OAuthClient();
		client.setClientId(CLIENT_ID_SERVER_FLOW);
		client.setSecret(CLIENT_ID_SERVER_FLOW);
		client.setName(CLIENT_ID_SERVER_FLOW);
		client.setUrl(home);
		clients.add(client);

		client = new OAuthClient();
		client.setClientId("testUnknownClientIdClient");
		client.setDefaultRedirect(false);
		client.setName("testUnknownClientIdClient");
		client.setUrl("default");
		clients.add(client);

		client = new OAuthClient();
		client.setClientId("testUnknownRespTypeClient");
		client.setDefaultRedirect(false);
		client.setName("testUnknownRespTypeClient");
		client.setUrl(context.getBaseUrl() + "logon.do");
		clients.add(client);
	}

	private HttpResponse defaultClientTokenRequest(String... otherParams) throws Exception
	{
		String[] params = new String[]{"redirect_uri", REDIRECT_URI, "client_id", CLIENT_ID, "response_type", "token"};
		return getResponse(context.getBaseUrl() + "oauth/authorise", null, false,
			ArrayUtils.addAll(params, otherParams));
	}

	private HttpResponse defaultClientTokenRequestPost(String... otherParams) throws Exception
	{
		String[] params = new String[]{"redirect_uri", REDIRECT_URI, "client_id", CLIENT_ID, "response_type", "token"};
		return postUri(context.getBaseUrl() + "oauth/authorise", null, ArrayUtils.addAll(params, otherParams));
	}

	@DataProvider
	public Object[][] oauthErrors()
	{
		// @formatter:off
		return new Object[][]
			{
				{   "invalid_client", 
					"No OAuth client can be found with the supplied client_id (doesntexist) and redirect_uri (default)", 
					"grant_type=authorization_code&client_id=doesntexist&redirect_uri=default"
				},
				{   "invalid_request", 
					"Parameter 'code' must be supplied", 
					"grant_type=authorization_code&client_id=testOAuthTokenLoginClient&redirect_uri=default"
				},
				{   "invalid_grant", 
					"Unrecognised code value", 
					"grant_type=authorization_code&client_id=testOAuthTokenLoginClient&redirect_uri=default&code=1234"
				},
				{   "invalid_grant", 
					"Invalid client_secret", 
					"grant_type=client_credentials&redirect_uri=default&client_id=testOAuthTokenLoginClient&client_secret=invalid"
				}, 
			};
		// @formatter:on
	}

	@Test(dataProvider = "oauthErrors")
	public void testOAuthServletErrors(String type, String descritopn, String url) throws IOException
	{
		String tokenGetUrl = context.getBaseUrl() + "oauth/access_token?" + url;
		HttpResponse response = execute(new HttpGet(tokenGetUrl), false);
		JsonNode tokenNode = readJson(mapper, response);

		Assert.assertTrue(tokenNode.has("error"), "Should be an error");
		Assert.assertEquals(tokenNode.get("error").asText(), type);
		Assert.assertEquals(tokenNode.get("error_description").asText(), descritopn);
	}

	// No shared secret for now
	@Test(enabled = false)
	public void testOAuthWithTokenLogin() throws Exception
	{
		HttpResponse response = defaultClientTokenRequest("token",
			TokenSecurity.createSecureToken("AutoTest", "token", "token", null));
		String string = EntityUtils.toString(response.getEntity());

		Assert.assertTrue(string.contains("oal_allowButton"), "Should be logged in");

		response = defaultClientTokenRequestPost("event__", "oal.allowAccess");
		Assert.assertTrue(hasAccessToken(response));

		// we should be able to make REST calls with this token as the AutoTest
		// user
	}

	@Test
	public void testOAuthLogonDialog() throws Exception
	{
		HttpResponse response = defaultClientTokenRequest();
		String string = EntityUtils.toString(response.getEntity());

		Assert.assertFalse(string.contains("oal_allowButton"), "Shouldn't be logged in");

		response = defaultClientTokenRequestPost("event__", "oal.denyAccess");
		Assert.assertFalse(hasAccessToken(response));

		response = defaultClientTokenRequest();
		string = EntityUtils.toString(response.getEntity());
		Assert.assertFalse(string.contains("oal_allowButton"), "Shouldn't be logged in");

		response = defaultClientTokenRequest("password", "bad", "event__", "oal.authorise");
		string = EntityUtils.toString(response.getEntity());
		Assert.assertTrue(string.contains("Invalid username/password"), "Should have an error message");

		response = defaultClientTokenRequestPost("password", "automated", "event__", "oal.authorise");
		Assert.assertTrue(hasAccessToken(response));
	}

	private String getLocation(HttpResponse response)
	{
		Header lastHeader = response.getLastHeader("Location");
		if( lastHeader != null )
		{
			return lastHeader.getValue();
		}
		return null;
	}

	private boolean hasAccessToken(HttpResponse response) throws Exception
	{
		String location = getLocation(response);
		if( !Check.isEmpty(location) )
		{
			URI uri = URI.create(location);
			return !Check.isEmpty(URLUtils.parseParamString(uri.getFragment()).get("access_token"));
		}

		return false;
	}

	/**
	 * Requires the redirector servlet. Also ensures that history and comments
	 * are viewable since the user is logged in
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Test
	public void testOAuthServerSideFlow() throws Exception
	{
		String home = context.getBaseUrl() + "logon.do";

		HttpResponse response = getResponse(context.getBaseUrl() + "oauth/authorise", null, false, "redirect_uri",
			home, "client_id", CLIENT_ID_SERVER_FLOW, "response_type", "code");
		String string = EntityUtils.toString(response.getEntity());
		Assert.assertTrue(string.contains("oal_authButton"), "Should need to log in");

		response = postUri(context.getBaseUrl() + "oauth/authorise", null, "redirect_uri", home, "client_id",
			CLIENT_ID_SERVER_FLOW, "response_type", "code", "username", "AutoTest", "password", "automated", "event__",
			"oal.authorise");
		String url = getLocation(response);

		String code = url.substring(url.indexOf("code=") + 5);
		int index = code.indexOf('&') + 1;
		index = index > 0 ? index : code.length();
		code = code.substring(0, index);

		final String tokenGetUrl = context.getBaseUrl() + "oauth/access_token?grant_type=authorization_code&client_id="
			+ CLIENT_ID_SERVER_FLOW + "&redirect_uri=" + home + "&client_secret=" + CLIENT_ID_SERVER_FLOW + "&code="
			+ code;
		response = execute(new HttpGet(tokenGetUrl), false);
		final JsonNode tokenNode = readJson(mapper, response);
		String token = "access_token=" + tokenNode.get("access_token").textValue();

		HttpResponse searchResp = execute(new HttpGet(context.getBaseUrl() + "api/search?showall=true"), false);
		Assert.assertEquals(searchResp.getStatusLine().getStatusCode(), 200, "Unexpected search result status");
		JsonNode search = readJson(mapper, searchResp);
		int results = search.get("available").asInt();
		Assert.assertEquals(results, 0, "Discovered items when not logged in");

		HttpGet authorisedGet = new HttpGet(context.getBaseUrl() + "api/search?showall=true");
		authorisedGet.addHeader("X-Authorization", token);

		searchResp = execute(authorisedGet, false);
		try
		{
			Assert.assertEquals(searchResp.getStatusLine().getStatusCode(), 200, "Unable to search");
			search = readJson(mapper, searchResp);
		}
		finally
		{
			EntityUtils.consume(searchResp.getEntity());
		}
		results = search.get("available").asInt();
		Assert.assertTrue(results > 20, "Couldn't discover all items with access token");
	}

	@Test
	public void testUnknownClientId() throws Exception
	{
		HttpResponse response = getResponse(context.getBaseUrl() + "oauth/authorise", null, false, "redirect_uri",
			"default", "client_id", "bogusClientId", "response_type", "token");
		String string = EntityUtils.toString(response.getEntity());
		Assert.assertTrue(string.contains("No OAuth client can be found with the supplied client_id"),
			"Wrong error message for invalid client");

		response = postUri(context.getBaseUrl() + "oauth/authorise", null, "redirect_uri", context.getBaseUrl()
			+ "logon.do", "client_id", "testUnknownRespTypeClient", "response_type", "whatthe");
		String location = getLocation(response);

		Assert.assertTrue(location.contains("unsupported_response_type"), "Wrong error message for invalid client");
	}

	@Test
	public void testOAuthUnknownAccessToken() throws Exception
	{
		// add BAD token to x-auth header and make a secure API call, complete
		// with history and comments
		HttpResponse response = execute(new HttpGet(context.getBaseUrl()
			+ "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), true, UUID.randomUUID().toString());
		assertResponse(response, 403, "Bad token didn't give access denied");

		// test a badly formatted token (duplicate the access_token= part)
		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"),
			"access_token=access_token=" + UUID.randomUUID().toString());
		assertResponse(response, 403, "Bad format token 1 didn't give access denied");

		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), "access_token");
		assertResponse(response, 403, "Bad format token 2 didn't give access denied");

		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), "access_token=");
		assertResponse(response, 403, "Bad format token 3 didn't give access denied");

		// Valid token, bad formats
		String tokes = requestToken(CLIENT_ID).replaceAll("access_token=", "");
		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), "=" + tokes);
		assertResponse(response, 403, "Valid token, bad format 1 didn't give access denied");

		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), tokes);
		assertResponse(response, 403, "Valid token, bad format 1 didn't give access denied");

		// sanity check
		response = rawTokenExecute(
			new HttpGet(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1"), "access_token="
				+ tokes);
		assertResponse(response, 200, "Valid token, valid format didn't give 200");
	}

	private HttpResponse rawTokenExecute(HttpUriRequest request, String rawToken) throws Exception
	{
		final Header tokenHeader = new BasicHeader("X-Authorization", rawToken);
		request.setHeader(tokenHeader);
		request.setHeader("X-Autotest-Key", context.getFullName(""));
		final HttpResponse response = getClient().execute(request);
		EntityUtils.consume(response.getEntity());
		return response;
	}
}
