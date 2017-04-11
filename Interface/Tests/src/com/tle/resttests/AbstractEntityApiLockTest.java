package com.tle.resttests;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.apache.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import com.tle.resttests.util.RestTestConstants;

@SuppressWarnings("nls")
public class AbstractEntityApiLockTest extends AbstractEntityApiTest
{
	protected String OAUTH_CLIENT_ID = getClass().getSimpleName();

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
	}

	protected void testLocks(String token, String entityUri) throws Exception
	{
		ObjectNode entityJson = (ObjectNode) getEntity(entityUri, token);
		HttpResponse response = putEntity(entityJson.toString(), entityUri, token, true);
		assertResponse(response, 200, "Should be editable");

		JsonNode lockJson = lockEntity(token, entityUri);
		String lockId = lockJson.get("uuid").asText();

		response = putEntity(entityJson.toString(), entityUri, token, true);
		assertResponse(response, 409, "Should not be able to edit without supplying lock");

		response = putEntity(entityJson.toString(), entityUri, token, true, "lock", lockId);
		assertResponse(response, 200, "Should be editable with lock");

		lockJson = lockEntity(token, entityUri);
		lockId = lockJson.get("uuid").asText();

		assertResponse(lockEntityError(token, entityUri), 409, "Shouldn't be able to lock again");

		lockJson = getLock(token, entityUri);
		assertEquals(lockJson.get("uuid").asText(), lockId);

		unlock(token, entityUri);
		assertResponse(getLockEntityError(token, entityUri), 404, "Shouldn't be able to get a missing lock");

		lockJson = lockEntity(token, entityUri);
		lockId = lockJson.get("uuid").asText();
		response = putEntity(entityJson.toString(), entityUri, token, true, "lock", lockId, "keeplocked", true);

		assertResponse(lockEntityError(token, entityUri), 409, "Shouldn't be able to lock again");
		unlock(token, entityUri);
	}
}
