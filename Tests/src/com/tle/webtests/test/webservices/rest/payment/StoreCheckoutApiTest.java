package com.tle.webtests.test.webservices.rest.payment;

import static org.testng.Assert.assertEquals;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.HttpResponse;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.tle.common.Pair;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.payment.global.RegisterStoreAndStoreFront;
import com.tle.webtests.test.webservices.rest.AbstractRestApiTest;

@TestInstitution("storebackend2ssl")
public class StoreCheckoutApiTest extends AbstractRestApiTest
{

	private static final String TOKEN = "763c8c11-078f-4a2e-b8b7-9aa11d99fa7d";
	private static final String CATALOGUE_UUID = "3ea6cabf-3ca2-486f-b877-66278d9ac987";
	private static final String CUSTOMER_REFERENCE = "c720fc5e-7238-4320-bda9-86a799bce027";
	private final String CREATE_DATE = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date());

	@Override
	protected void addOAuthClients(List<Pair<String, String>> clients)
	{
		// no need to create a new one
	}

	@Test
	// a quantity of non-zero is specified for a fixed rate tier
	public void testNonZeroQuantityCheckout() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 2);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items[0].quantity: Quantity must not be supplied");
	}

	@Test
	// a tier representation is missing from CheckoutItem that the store front
	// is not allowed to get for free.
	public void testMissingTierCheckout() throws Exception
	{
		ObjectNode node = createBaseNode();
		// don't add any price tier
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode,
			"items[0].purchaseTier: Item is not free.  Please specify a purchase or subscription tier.");
	}

	@Test
	public void testNonSubscriptionPeriod() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("subscriptionTier", createSubscriptionTier());
		item.put("subscriptionStartDate", CREATE_DATE);
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items[0].subscriptionPeriod: Subscription period must be supplied");
	}

	@Test
	public void testTotalPriceNotMatch() throws Exception
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("customerReference", CUSTOMER_REFERENCE);
		node.put("price", createPrice("EUR", 500));
		node.put("creationDate", CREATE_DATE);
		node.put("paidStatus", "SUBMITTED");

		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("subscriptionTier", createSubscriptionTier());
		item.put("subscriptionPeriod", createSubscriptionPeriod());
		item.put("subscriptionStartDate", CREATE_DATE);
		item.put("catalogueUuid", CATALOGUE_UUID);
		item.put("price", createPrice("EUR", 500));
		node.putArray("items").add(item);

		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode,
			"items[0].price.value: Supplied price value of 5 does not match the calculated value of 25");
	}

	@Test
	// an item is listed in the checkout more than once
	public void testDuplicatedItem() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		// add the same item twice
		ArrayNode putArray = node.putArray("items");
		putArray.add(item);
		putArray.add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items: Same item listed more than once");
	}

	@Test
	public void testWrongCurrency() throws Exception
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("customerReference", CUSTOMER_REFERENCE);
		node.put("price", createPrice("AUD", 500));
		node.put("creationDate", CREATE_DATE);
		node.put("paidStatus", "SUBMITTED");

		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		ArrayNode putArray = node.putArray("items");
		putArray.add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "currency: Store does not use this currency");
	}

	@Test
	// an item is not available in any catalogue, so the store front should
	// never have been able to see it
	public void testUnavailableItem() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		// the item is not available in any catalogue
		item.put("itemUuid", "5101e752-59bb-4e4b-8c51-6e16635f9983");
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items[0].uuid: Requested item does not exist");
	}

	@Test
	public void testNotInCatalogueItem() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "4940aea4-723d-4f82-9696-e09f51890de2");
		// Real item, not in a catalogue, has a price
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items[0].uuid: This item cannot be found in any catalogue");
	}

	@Test
	// You shouldn't be able to pass a Uuid in the sale
	public void testUuidRejected() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "f1fe6b33-ab09-463d-9cb9-3598245b9f32");
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", CATALOGUE_UUID);
		node.put("uuid", "81a95f66-d7ae-5d9f-4a31-034c8feef940"); // real
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "uuid: You cannot assign your own UUID to a checkout");
		node.remove("uuid");
		node.put("uuid", "81a95f66-d7ae-5d9f-4a31-034c8fegf940"); // bogus
		node.putArray("items").add(item);
		messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "uuid: You cannot assign your own UUID to a checkout");
	}

	@Test
	public void testExistingSale() throws Exception
	{
		ObjectNode node = createBaseNode();
		node.put("customerReference", "6839d685-8eb9-4829-a62c-1ac34dec898c");
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode,
			"customerReference: This checkout was already paid for on: 10/09/12 12:19 PM");
	}

	@Test
	public void testNoItems() throws Exception
	{
		ObjectNode node = createBaseNode();
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items: Checkout has no items");
	}

	@Test
	public void testUuidNull() throws Exception
	{
		ObjectNode node = createBaseNode();
		ObjectNode item = mapper.createObjectNode();
		node.putArray("items").add(item);
		JsonNode messageNode = returnBadRequestMessage(node);
		assertBadRequestMessage(messageNode, "items[0].uuid: Item uuid not supplied");
	}

	@DataProvider(name = "storeUrls", parallel = false)
	public Object[][] storeUrls()
	{
		return new Object[][]{{RegisterStoreAndStoreFront.INSTITUTION_STORE, RegisterStoreAndStoreFront.STORE_NAME}};
	}

	@Test(dataProvider = "storeUrls")
	// a quantity of zero is specified for a per-user tier
	public void testZeroQuantity(String storeUrl, String storeName) throws Exception
	{
		PageContext context = newContext(storeUrl);

		ObjectNode node = mapper.createObjectNode();
		node.put("customerReference", CUSTOMER_REFERENCE);
		node.put("price", createPrice("AUD", 500));
		node.put("creationDate", CREATE_DATE);
		node.put("paidStatus", "SUBMITTED");

		ObjectNode item = mapper.createObjectNode();
		item.put("itemUuid", "9dae8f63-9999-48b4-b1b4-9980ab1bbc99");
		item.put("quantity", 0);
		item.put("purchaseTier", createPurchaseTier());
		item.put("catalogueUuid", "3ea6cabf-3ca2-486f-b877-66278d9ac987");

		node.putArray("items").add(item);

		HttpResponse response = postEntity(node.toString(), context.getBaseUrl() + "api/store/checkout", TOKEN, false);
		assertResponse(response, 400, "Bad Request");
		try (InputStream content = response.getEntity().getContent())
		{
			JsonNode messageNode = mapper.readTree(content);
			assertBadRequestMessage(messageNode, "items[0].quantity: Quantity must be supplied");
		}
	}

	private ObjectNode createBaseNode()
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("customerReference", CUSTOMER_REFERENCE);
		node.put("price", createPrice("EUR", 500));
		node.put("creationDate", CREATE_DATE);
		node.put("paidStatus", "SUBMITTED");
		return node;
	}

	private ObjectNode createPrice(String currency, int cents)
	{
		ObjectNode price = mapper.createObjectNode();
		ObjectNode value = mapper.createObjectNode();
		value.put("value", cents);
		value.put("scale", 2);
		price.put("currency", currency);
		price.put("value", value);
		return price;
	}

	private ObjectNode createPurchaseTier()
	{
		ObjectNode tier = mapper.createObjectNode();
		tier.put("uuid", "15916c3e-69d2-44a5-9a90-8f82baf0db38");
		return tier;
	}

	private ObjectNode createSubscriptionTier()
	{
		ObjectNode tier = mapper.createObjectNode();
		tier.put("uuid", "c717dec1-5da0-4596-b023-299ac148085d");
		return tier;
	}

	private ObjectNode createSubscriptionPeriod()
	{
		ObjectNode period = mapper.createObjectNode();
		period.put("uuid", "5c66b0dd-c7cc-46da-8c61-4762c547810a");
		period.put("duration", 1);
		period.put("durationUnit", "WEEKS");
		period.put("name", "Week");
		ObjectNode nameString = mapper.createObjectNode();
		nameString.put("en", "Week");
		period.put("nameStrings", nameString);
		return period;
	}

	private JsonNode returnBadRequestMessage(ObjectNode node) throws Exception
	{
		HttpResponse response = postEntity(node.toString(), context.getBaseUrl() + "api/store/checkout", TOKEN, false);
		assertResponse(response, 400, "Bad Request");
		return mapper.readTree(response.getEntity().getContent());
	}

	private void assertBadRequestMessage(JsonNode node, String message)
	{
		String text = node.get("error_description").asText();
		assertEquals(text, message);
	}

}
