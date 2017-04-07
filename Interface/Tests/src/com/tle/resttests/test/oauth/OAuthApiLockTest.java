package com.tle.resttests.test.oauth;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.resttests.AbstractEntityApiLockTest;
import com.tle.resttests.util.RestTestConstants;

@SuppressWarnings("nls")
public class OAuthApiLockTest extends AbstractEntityApiLockTest
{
	private String token;
	private String oauthUri;

	private void createOAuth() throws IOException
	{
		token = requestToken(OAUTH_CLIENT_ID);
		ObjectNode oauthclient = mapper.createObjectNode();
		oauthclient.put("name", "Random OAuth");
		oauthclient.put("clientId", "clientId");
		oauthclient.put("clientSecret", "clientSecret");
		oauthclient.put("userId", RestTestConstants.USERID_AUTOTEST);
		oauthclient.put("redirectUrl", "default");

		HttpResponse response = postEntity(oauthclient.toString(), context.getBaseUrl() + "api/oauth", token, true);
		assertResponse(response, 201, "Expected oauthclient to be created");

		oauthUri = response.getFirstHeader("Location").getValue();
		getEntity(oauthUri, token);
	}

	@Test
	public void edit() throws Exception
	{
		createOAuth();
		testLocks(token, oauthUri);
	}

	@AfterMethod(alwaysRun = true)
	public void cleanupOAuth() throws IOException
	{
		if( oauthUri != null )
		{
			deleteResource(oauthUri, token);
		}
	}

}
