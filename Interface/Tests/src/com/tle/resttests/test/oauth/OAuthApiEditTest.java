package com.tle.resttests.test.oauth;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.OAuthClients;
import com.tle.json.requests.BaseEntityRequests;
import com.tle.resttests.AbstractEntityApiEditTest;
import com.tle.resttests.util.RequestsBuilder;
import com.tle.resttests.util.RestTestConstants;

@SuppressWarnings("nls")
public class OAuthApiEditTest extends AbstractEntityApiEditTest
{
	@Test
	public void testCreateBad()
	{
		normalRequests.createFail(normalRequests.badRequest(), OAuthClients.json(context.getFullName("Blank clientId"),
			"", "blah", RestTestConstants.USERID_AUTOTEST, "default"));
		normalRequests.create(OAuthClients.json(context.getFullName("dupetest"), "duplicateId", "blah",
			RestTestConstants.USERID_AUTOTEST, "default"));
		normalRequests.createFail(normalRequests.badRequest(), OAuthClients.json(context.getFullName("A dupe"),
			"duplicateId", "blah", RestTestConstants.USERID_AUTOTEST, "default"));
	}

	@Test
	public void testSuccessiveEdits()
	{
		ObjectNode client = normalRequests.create(createJsonForEdit(context.getFullName("testSuccessiveEdits")));
		client.put("clientId", "testSuccessiveEdits");
		String uuid = normalRequests.editId(client);

		client = normalRequests.get(uuid);

		//List clients to try to replicate flakey autotest failures
		ObjectNode all = normalRequests.list();

		client.put("name", context.getFullName("first edit name"));
		client.remove("nameStrings");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("first edit name"),
			"Unexpect OAuth client name");
		Assert.assertNull(client.get("description"), "Description not null");

		all = normalRequests.list();

		client.put("description", "second edit desc");
		client.remove("descriptionStrings");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("first edit name"),
			"Unexpect OAuth client name");
		Assert
			.assertEquals(client.get("description").asText(), "second edit desc", "Unexpect OAuth client description");

		all = normalRequests.list();

		ObjectNode descStrings = client.objectNode();
		descStrings.put("en", "third edit desc strings");
		client.put("descriptionStrings", descStrings);
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("first edit name"),
			"Unexpect OAuth client name");
		Assert.assertEquals(client.get("description").asText(), "third edit desc strings",
			"Unexpect OAuth client description");

		all = normalRequests.list();

		descStrings = client.objectNode();
		descStrings.put("en", "fourth edit desc strings");
		client.put("descriptionStrings", descStrings);
		client.remove("description");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("first edit name"),
			"Unexpect OAuth client name");
		Assert.assertEquals(client.get("description").asText(), "fourth edit desc strings",
			"Unexpect OAuth client description");

		all = normalRequests.list();

		//Remove the description
		client.remove("description");
		client.remove("descriptionStrings");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("first edit name"),
			"Unexpect OAuth client name");
		Assert.assertNull(client.get("description"), "Description not null");
		Assert.assertNull(client.get("descriptionStrings"), "Description strings not null");

		all = normalRequests.list();

		//Add the desc back
		client.put("description", "fifth edit desc");

		descStrings = client.objectNode();
		descStrings.put("en", "fifth edit desc");
		client.put("descriptionStrings", descStrings);
		client.put("name", context.getFullName("fifth edit name"));
		client.remove("nameStrings");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("fifth edit name"),
			"Unexpect OAuth client name");
		Assert.assertEquals(client.get("description").asText(), "fifth edit desc", "Unexpect OAuth client description");
		Assert.assertEquals(client.get("descriptionStrings").get("en").asText(), "fifth edit desc",
			"Unexpect OAuth client description");

		all = normalRequests.list();

		// Make it invalid
		client.remove("name");
		client.remove("nameStrings");
		//normalRequests.e
		RequestSpecification request = normalRequests.editRequest(normalRequests.badRequest(), client);
		normalRequests.editResponse(request, uuid);
		client = normalRequests.get(uuid);
		//assert unchanged
		Assert.assertEquals(client.get("name").asText(), context.getFullName("fifth edit name"),
			"Unexpect OAuth client name");
		Assert.assertEquals(client.get("description").asText(), "fifth edit desc", "Unexpect OAuth client description");
		Assert.assertEquals(client.get("descriptionStrings").get("en").asText(), "fifth edit desc",
			"Unexpect OAuth client description");

		all = normalRequests.list();

		client.put("name", context.getFullName("sixth edit name"));
		client.remove("nameStrings");
		normalRequests.editId(client);
		client = normalRequests.get(uuid);
		Assert.assertEquals(client.get("name").asText(), context.getFullName("sixth edit name"),
			"Unexpect OAuth client name");
		Assert.assertEquals(client.get("description").asText(), "fifth edit desc", "Unexpect OAuth client description");
		Assert.assertEquals(client.get("descriptionStrings").get("en").asText(), "fifth edit desc",
			"Unexpect OAuth client description");

		all = normalRequests.list();

		int dbg = 1;
	}

	@Override
	protected String getEditPrivilege()
	{
		return "EDIT_OAUTH_CLIENT";
	}

	@Override
	protected void assertExtraEdits(ObjectNode edited)
	{
		Assert.assertEquals(edited.get("clientId").asText(), "changed");
		Assert.assertEquals(edited.get("clientSecret").asText(), "changed");
		Assert.assertEquals(edited.get("userId").asText(), "changed");
		// Equella actually validates that this is a real URL.
		Assert.assertEquals(edited.get("redirectUrl").asText(), "https://changed");
	}

	@Override
	protected void extraEdits(ObjectNode client)
	{
		client.put("clientId", "changed");
		client.put("clientSecret", "changed");
		client.put("userId", "changed");
		// Equella actually validates that this is a real URL.
		client.put("redirectUrl", "https://changed");
	}

	@Override
	protected BaseEntityRequests createRequestsWithBuilder(RequestsBuilder builder)
	{
		return builder.oauthClients();
	}

	@Override
	protected ObjectNode createJsonForPrivs(String fullName)
	{
		return OAuthClients.json(fullName, "privClientId", "blah", RestTestConstants.USERID_AUTOTEST, "default");
	}

	@Override
	protected ObjectNode createJsonForEdit(String fullName)
	{
		return OAuthClients.json(fullName, "oauthEditClientId", "blah", RestTestConstants.USERID_AUTOTEST, "default");
	}
}
