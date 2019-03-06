package com.tle.resttests.test.search;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.json.requests.SearchRequests;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RestTestConstants;

public class WaitForIndexTest extends AbstractEntityCreatorTest
{

	private SearchRequests searches;

	@Override
	protected void customisePageContext()
	{
		searches = builder().searches();
		items = builder().items();
	}

	@Test
	public void testDelete()
	{
		for( int i = 0; i < 10; i++ )
		{
			ObjectNode jsonXml = Items.jsonXml(RestTestConstants.COLLECTION_BASIC,
				"<xml><item><name>WaitForIndexTest - delete</name></item></xml>");
			ItemId id = items.getId(items.create(items.createRequest(jsonXml, true)));
			Assert.assertEquals(1, searches.search("WaitForIndexTest - delete").get("available").asInt());
			items.delete(id, true, true);
			Assert.assertEquals(0, searches.search("WaitForIndexTest - delete").get("available").asInt());
		}
	}
}
