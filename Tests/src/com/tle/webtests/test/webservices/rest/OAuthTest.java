package com.tle.webtests.test.webservices.rest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.tle.common.Pair;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.oauth.OAuthDefaultRedirectPage;
import com.tle.webtests.pageobject.oauth.OAuthLogonPage;
import com.tle.webtests.pageobject.oauth.OAuthTokenRedirect;
import com.tle.webtests.test.users.TokenSecurity;

public class OAuthTest extends AbstractRestApiTest
{
	private static final String CLIENT_ID_SERVER_FLOW = "testOAuthServerSideFlowClient";
	private static final String CLIENT_ID = "testOAuthTokenLoginClient";
	private static final String REDIRECT_URI = "default";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(CLIENT_ID, "AutoTest"));
	}

	private OAuthLogonPage defaultClientTokenRequest(String... otherParams)
	{
		return OAuthLogonPage.authorise(context, CLIENT_ID, REDIRECT_URI, "token", otherParams);
	}

	@Test
	public void testOAuthWithTokenLogin() throws IOException
	{
		OAuthLogonPage oAuthLogonPage = defaultClientTokenRequest("token",
			TokenSecurity.createSecureToken("AutoTest", "token", "token", null));
		Assert.assertTrue(oAuthLogonPage.isAlreadyLoggedIn(), "Should be logged in");
		OAuthDefaultRedirectPage defaultPage = oAuthLogonPage.allowAccess(new OAuthDefaultRedirectPage(context));
		defaultPage.assertToken();

		// we should be able to make REST calls with this token as the AutoTest
		// user
	}

	@Test
	public void testOAuthLogonDialog()
	{
		context.getDriver().manage().deleteAllCookies();
		OAuthLogonPage oAuthLogon = defaultClientTokenRequest();
		Assert.assertFalse(oAuthLogon.isAlreadyLoggedIn(), "Shouldn't be logged in");
		OAuthDefaultRedirectPage defaultRedirect = oAuthLogon.denyAccess(new OAuthDefaultRedirectPage(context));
		Assert.assertEquals(defaultRedirect.getErrorReason(), "access_denied", "Should have been denied");
		assertNoCookies();

		oAuthLogon = defaultClientTokenRequest();
		Assert.assertFalse(oAuthLogon.isAlreadyLoggedIn(), "Shouldn't be logged in");
		String errorText = oAuthLogon.logonError("bad").getLogonErrorText();
		Assert.assertEquals(errorText, "Invalid username/password");
		defaultRedirect = oAuthLogon.logon("automated", new OAuthDefaultRedirectPage(context));
		defaultRedirect.assertToken();
		assertNoCookies();
	}

	private void assertNoCookies()
	{
		Assert.assertTrue(context.getDriver().manage().getCookies().isEmpty(), "Shouldn't have created any cookies");
	}

	/**
	 * Requires the redirector servlet. Also ensures that history and comments
	 * are viewable since the user is logged in
	 * 
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Test
	public void testOAuthServerSideFlow() throws IllegalStateException, IOException
	{
		logon("AutoTest", "automated");
		OAuthClient client = new OAuthClient();
		client.setName(CLIENT_ID_SERVER_FLOW);
		client.setClientId(CLIENT_ID_SERVER_FLOW);
		client.setDefaultRedirect(false);
		String redirectUrl = OAuthTokenRedirect.getRedirectUri(context);
		Assert.assertNotNull(redirectUrl, "\"oauth.redirector.url\" in localserver.properties unset?");
		client.setUrl(redirectUrl);
		OAuthUtils.createClient(context, client);
		logout();
		clients.add(client);

		OAuthTokenRedirect oauthRedirect = OAuthTokenRedirect.redirect(context, CLIENT_ID_SERVER_FLOW,
			new OAuthLogonPage(context)).logon("serverSideFlowUser", "password", new OAuthTokenRedirect(context));

		// this redirects again to the redirector URL, which displays the token
		// that it asks EQUELLA for
		// if everything runs smoothly we should get access_token=xxx
		String tokenData = oauthRedirect.getToken();

		// add token to x-auth header and make a secure API call, complete with
		// history and comments
		JsonNode comments = getEntity(context.getBaseUrl() + "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1/comment",
			tokenData);

		// assert that comments are present (only serverSideFlowUser can
		// actually see them)
		String commentUser = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";

		ApiAssertions apiAssertions = new ApiAssertions(context);
		apiAssertions.assertComment(comments.get(0), "18645d15-e67b-4d23-af84-dc64b589c06d", 0, commentUser, false,
			"Another", "2012-03-21T13:59:00.284+11:00");
		apiAssertions.assertComment(comments.get(1), "f8d8c7eb-590f-40b4-bffc-5be82e212d8e", 0, commentUser, false,
			"Comment 2222", "2012-03-21T13:58:51.585+11:00");
		apiAssertions.assertComment(comments.get(2), "117bdb9d-0ffb-40e3-9613-ad48851f9ed9", 0, commentUser, false,
			"Comment 1111", "2012-03-21T13:58:47.088+11:00");

		// do it as guest, make sure there are no visible comments
		HttpResponse response = execute(new HttpGet(context.getBaseUrl()
			+ "api/item/b5a24157-37cf-4d1f-a2e6-8382edccc7a8/1/comment"), true, null);
		assertResponse(response, 403, "Access denied not returned from comment endpoint");
	}

	@Test
	public void testUnknownClientId() throws Exception
	{
		logon("AutoTest", "automated");
		OAuthClient client = new OAuthClient();
		client.setClientId("testUnknownClientIdClient");
		client.setDefaultRedirect(false);
		client.setName("testUnknownClientIdClient");
		client.setUrl(OAuthTokenRedirect.getRedirectUri(context));
		OAuthUtils.createClient(context, client);
		logout();
		clients.add(client);

		ErrorPage errorPage = OAuthTokenRedirect.redirect(context, "testIDontKnowThisClient", new ErrorPage(context));

		Assert.assertTrue(errorPage.getDetail().contains("No OAuth client can be found with the supplied client_id"),
			"Wrong error message for invalid client");

		OAuthTokenRedirect response = OAuthTokenRedirect.redirect(context, "testUnknownClientIdClient",
			new OAuthTokenRedirect(context), "response_type", "badtype");
		Assert.assertEquals(response.getError(), "unsupported_response_type");
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
		String tokes = getToken();
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
		return execute(request, true);
	}
}
