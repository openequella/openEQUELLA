package com.tle.core.item.serializer.where;

import java.util.Collection;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.tle.common.Check;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.ItemSerializerWhere;

@SuppressWarnings("nls")
public class ItemIdsWhereClause implements ItemSerializerWhere
{
	private Collection<Long> itemIds;

	public ItemIdsWhereClause(Collection<Long> itemIds)
	{
		// Don't try executing this query if we have no itemIds, as it's very
		// expensive for large numbers of items
		Preconditions.checkArgument(!Check.isEmpty(itemIds), "You must supply one or more item IDs");

		this.itemIds = itemIds;
	}

	@Override
	public void addWhere(ItemSerializerState state)
	{
		DetachedCriteria itemQuery = state.getItemQuery();
		itemQuery.add(Restrictions.in("id", itemIds));
	}
}
