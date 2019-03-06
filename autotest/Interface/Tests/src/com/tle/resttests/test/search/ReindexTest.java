package com.tle.resttests.test.search;

import java.util.List;

import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

import com.tle.common.Pair;
import com.tle.json.framework.TestInstitution;
import com.tle.resttests.AbstractRestApiTest;
import com.tle.resttests.util.RestTestConstants;

@TestInstitution("autotest")
@Test(groups = "eps")
public class ReindexTest extends AbstractRestApiTest
{
	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		clients.add(new Pair<String, String>("ReindexTest", RestTestConstants.USERID_AUTOTEST));
	}

	@Test
	public void testReindexAll() throws Exception
	{
		HttpResponse response = getResponse(context.getBaseUrl() + "home.do", getToken(), true, "event__",
			"hp.syncIndex");
		assertResponse(response, 200, "Should be 200");
	}

	@Test
	public void testReindexMissing() throws Exception
	{
		HttpResponse response = getResponse(context.getBaseUrl() + "home.do", getToken(), true, "event__",
			"hp.syncMissing");
		assertResponse(response, 200, "Should be 200");
	}

	@Test
	public void testSyncAcls() throws Exception
	{
		HttpResponse response = getResponse(context.getBaseUrl() + "home.do", getToken(), true, "event__",
			"hp.syncAcls");
		assertResponse(response, 200, "Should be 200");
	}

}
