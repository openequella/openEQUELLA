package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

import com.tle.common.Pair;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;

/**
 * @see Redmine: #6743
 */

@TestInstitution("storebackend2ssl")
public class StoreTransactionApiTest extends AbstractRestApiTest
{
	private static final String TOKEN = "763c8c11-078f-4a2e-b8b7-9aa11d99fa7d";
	private static final String CATALOGUE_UUID = "3ea6cabf-3ca2-486f-b877-66278d9ac987";

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// don't need to add new one
	}

	@Test
	public void testTransaction() throws Exception
	{
		HttpGet get = new HttpGet(context.getBaseUrl()
			+ "api/store/transaction?customerReference=6839d685-8eb9-4829-a62c-1ac34dec898c");
		HttpResponse response = execute(get, false, TOKEN);
		JsonNode transactions = mapper.readTree(response.getEntity().getContent());
		assertEquals(transactions.get("start").asInt(), 0);
		assertEquals(transactions.get("length").asInt(), 1);
		assertEquals(transactions.get("available").asInt(), 1);
		assertTransaction(transactions.get("transactions").get(0));

		// just get the first transaction and it won't be broken when there are
		// more transaction records
		JsonNode items = transactions.get("transactions").get(0).get("items");
		assertSaleItems(items);
	}

	private void assertTransaction(JsonNode node)
	{
		assertEquals(node.get("uuid").asText(), "30dd1d8f-dc01-cb82-a6f3-683b5e1ff9f9");
		assertEquals(node.get("price").get("currency").asText(), "EUR");
		assertEquals(node.get("price").get("value").get("value").asInt(), 10500);
		assertEquals(node.get("creationDate").asText(), "2012-09-10T12:19:25.487+10:00");
		assertEquals(node.get("paidDate").asText(), "2012-09-10T12:19:25.523+10:00");
		assertEquals(node.get("paidStatus").asText(), "PAID");
		assertEquals(node.get("receipt").asText(), "9b477c43-7a2a-440b-8c29-255559ed7efd");
	}

	private void assertSaleItems(JsonNode node)
	{
		for( JsonNode jsonNode : node )
		{
			if( jsonNode.get("uuid").asText().equals("c8a5a23f-061c-fda1-de71-c454813fbc27") )
			{
				assertEquals(jsonNode.get("item").get("uuid").asText(), "cf9034f5-cccb-49e9-bb17-ed8a603f20c4");
				assertTrue(jsonNode.get("free").asBoolean());
				assertEquals(jsonNode.get("price").get("value").asInt(), 0);
				assertEquals(jsonNode.get("quantity").asInt(), 0);
				assertEquals(jsonNode.get("catalogueUuid").asText(), CATALOGUE_UUID);
			}
			else if( jsonNode.get("uuid").asText().equals("9a652680-e67d-c200-6609-1637501e7589") )
			{
				assertEquals(jsonNode.get("item").get("uuid").asText(), "0a2890b7-aa3c-4c88-9b1d-552436b6f316");
				assertFalse(jsonNode.get("free").asBoolean());
				assertEquals(jsonNode.get("price").get("value").get("value").asInt(), 500);
				assertEquals(jsonNode.get("price").get("currency").asText(), "EUR");
				assertEquals(jsonNode.get("quantity").asInt(), 0);
				assertEquals(jsonNode.get("catalogueUuid").asText(), CATALOGUE_UUID);
			}
			else if( jsonNode.get("uuid").asText().equals("402cb04a-dbd7-e82f-f566-b23621930833") )
			{
				assertEquals(jsonNode.get("item").get("uuid").asText(), "10897ebe-aed0-4003-a925-e4b21cb4e23c");
				assertFalse(jsonNode.get("free").asBoolean());
				assertEquals(jsonNode.get("quantity").asInt(), 0);
				assertEquals(jsonNode.get("price").get("value").get("value").asInt(), 10000);
				assertEquals(jsonNode.get("catalogueUuid").asText(), CATALOGUE_UUID);
			}
		}
	}
}