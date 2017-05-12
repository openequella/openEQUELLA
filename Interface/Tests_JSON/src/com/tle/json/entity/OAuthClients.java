package com.tle.json.entity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.BaseJSONCreator;

@SuppressWarnings("nls")
public class OAuthClients extends BaseJSONCreator
{
	public static ObjectNode json(String name, String clientId, String password, String userId, String redirectUrl)
	{
		ObjectNode oauthclient = mapper.createObjectNode();
		oauthclient.put("name", name);
		oauthclient.put("clientId", clientId);
		oauthclient.put("clientSecret", password);
		oauthclient.put("userId", userId);
		oauthclient.put("redirectUrl", redirectUrl);
		return oauthclient;
	}

}
