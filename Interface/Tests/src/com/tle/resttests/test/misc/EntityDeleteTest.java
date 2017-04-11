package com.tle.resttests.test.misc;

import org.testng.annotations.Test;

import com.tle.json.entity.CollectionJson;
import com.tle.json.entity.ItemId;
import com.tle.json.entity.Items;
import com.tle.resttests.AbstractEntityCreatorTest;
import com.tle.resttests.util.RestTestConstants;

public class EntityDeleteTest extends AbstractEntityCreatorTest
{
	@Override
	public void customisePageContext()
	{
		collections = builder().collections();
		items = builder().items();
	}

	@Test
	public void testDelete()
	{
		String id = collections.getId(collections.create(CollectionJson.json(context.getFullName("delete me"),
			RestTestConstants.SCHEMA_BASIC, null)));
		ItemId itemId = items.getId(items.create(Items.json(id), true));
		ItemId itemId2 = items.getId(items.create(Items.json(id), true));

		collections.delete(collections.badRequest(), id);

		items.delete(itemId, true, true);
		items.delete(itemId2, true, true);
		collections.delete(id);
	}
}
